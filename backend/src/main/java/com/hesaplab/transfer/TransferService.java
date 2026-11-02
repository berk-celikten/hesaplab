package com.hesaplab.transfer;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;


@Service
public class TransferService {
    public static class Rejected extends RuntimeException {
        public Rejected(String message) { super(message); }
    }
    private static final BigDecimal MAX = new BigDecimal("9999999999999.99");
    private final TransferRepository repository;
    private final com.hesaplab.receipt.ReceiptService receipts;
    private final org.springframework.transaction.support.TransactionTemplate transaction;
    public static class Conflict extends RuntimeException { public Conflict(){super("Anahtar farklı içerikle kullanılmış veya sonucu henüz doğrulanamıyor.");} }
    public record Result(String transferId,String message) { }
    public TransferService(TransferRepository repository,org.springframework.transaction.PlatformTransactionManager manager, com.hesaplab.receipt.ReceiptService receipts) {
        this.receipts = receipts;
        this.repository=repository;
        transaction=new org.springframework.transaction.support.TransactionTemplate(manager);
        transaction.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.setTimeout(15);
    }
    private Result replay(TransferRepository.Stored stored,BigDecimal source,BigDecimal target,BigDecimal amount) {
        if(stored.source().compareTo(source)!=0 || stored.target().compareTo(target)!=0 || stored.amount().compareTo(amount)!=0) throw new Conflict();
        return new Result(stored.id(),"Transfer tamamlandı.");
    }
    private BigDecimal id(String value) {
        if (value == null || !value.matches("[0-9]{1,38}")) throw new Rejected("Hesaplar kullanılamıyor.");
        BigDecimal id = new BigDecimal(value);
        if (id.signum() <= 0) throw new Rejected("Hesaplar kullanılamıyor.");
        return id;
    }
    public Result transfer(String owner,String key,TransferRequest request) {
        if(key==null || !key.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) throw new Rejected("UUID Idempotency-Key gerekli.");
        String normalizedKey=java.util.UUID.fromString(key).toString();
        BigDecimal source = id(request.sourceAccountId());
        BigDecimal target = id(request.targetAccountId());
        if (source.compareTo(target) == 0) throw new Rejected("Kaynak ve hedef farklı olmalı.");
        if (request.amount() == null || !request.amount().matches("[0-9]{1,13}(\\.[0-9]{1,2})?")) {
            throw new Rejected("Pozitif, en fazla iki ondalıklı noktalı tutar girin.");
        }
        BigDecimal amount = new BigDecimal(request.amount());
        if (amount.signum() <= 0 || amount.compareTo(MAX) > 0) throw new Rejected("Tutar sınır dışında.");
        try {
            return transaction.execute(status->{
                // Aynı müşterinin tüm yazarları DB kilidiyle sınırlı bekleyerek sıraya girer.
                repository.lockOwner(owner);
                var previous=repository.find(owner,normalizedKey);
                if(previous.isPresent()) return replay(previous.get(),source,target,amount);

                var first = repository.lock(source.min(target), owner, source);
                var second = repository.lock(source.max(target), owner, source);
                var from = first.id().compareTo(source) == 0 ? first : second;
                var to = first.id().compareTo(target) == 0 ? first : second;
                String transferId=repository.insertTransfer(owner,normalizedKey,source,target,amount);
                if (!"TRY".equals(from.currency()) || !"TRY".equals(to.currency())) throw new Rejected("Yalnız TRY desteklenir.");
                if (from.balance().compareTo(amount) < 0) throw new Rejected("Yetersiz bakiye.");
                BigDecimal afterSource = from.balance().subtract(amount);
                BigDecimal afterTarget = to.balance().add(amount);
                if (afterTarget.compareTo(MAX) > 0) throw new Rejected("Hedef bakiye sınırı aşılır.");
                repository.update(source, afterSource);
                repository.update(target, afterTarget);
                repository.movement(source, "DEBIT", amount, afterSource, transferId);
                repository.movement(target, "CREDIT", amount, afterTarget, transferId);
                receipts.createAfterTransfer(transferId);
                return new Result(transferId,"Transfer tamamlandı.");
            });
        } catch(org.springframework.dao.DuplicateKeyException duplicate) {
            // Çakışan transaction bitti; rollback-only transaction içinde devam edilmez.
            return transaction.execute(status->replay(repository.find(owner,normalizedKey).orElseThrow(Conflict::new),source,target,amount));
        }
    }
}
