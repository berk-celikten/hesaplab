package com.hesaplab.transfer;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    public static class Rejected extends RuntimeException {
        public Rejected(String message) { super(message); }
    }
    private static final BigDecimal MAX = new BigDecimal("9999999999999.99");
    private final TransferRepository repository;
    public TransferService(TransferRepository repository) { this.repository = repository; }
    private BigDecimal id(String value) {
        if (value == null || !value.matches("[0-9]{1,38}")) throw new Rejected("Hesaplar kullanılamıyor.");
        BigDecimal id = new BigDecimal(value);
        if (id.signum() <= 0) throw new Rejected("Hesaplar kullanılamıyor.");
        return id;
    }
    @Transactional
    public void transfer(String owner, TransferRequest request) {
        BigDecimal source = id(request.sourceAccountId());
        BigDecimal target = id(request.targetAccountId());
        if (source.compareTo(target) == 0) throw new Rejected("Kaynak ve hedef farklı olmalı.");
        if (request.amount() == null || !request.amount().matches("[0-9]{1,13}(\\.[0-9]{1,2})?")) {
            throw new Rejected("Pozitif, en fazla iki ondalıklı noktalı tutar girin.");
        }
        BigDecimal amount = new BigDecimal(request.amount());
        if (amount.signum() <= 0 || amount.compareTo(MAX) > 0) throw new Rejected("Tutar sınır dışında.");
        // Her istekte aynı sayısal kilit sırası; bakiye kontrolü kilitten sonra.
        var first = repository.lock(source.min(target), owner);
        var second = repository.lock(source.max(target), owner);
        var from = first.id().compareTo(source) == 0 ? first : second;
        var to = first.id().compareTo(target) == 0 ? first : second;
        if (!"TRY".equals(from.currency()) || !"TRY".equals(to.currency())) throw new Rejected("Yalnız TRY desteklenir.");
        if (from.balance().compareTo(amount) < 0) throw new Rejected("Yetersiz bakiye.");
        BigDecimal afterSource = from.balance().subtract(amount);
        BigDecimal afterTarget = to.balance().add(amount);
        if (afterTarget.compareTo(MAX) > 0) throw new Rejected("Hedef bakiye sınırı aşılır.");
        repository.update(source, afterSource);
        repository.update(target, afterTarget);
        repository.movement(source, "DEBIT", amount, afterSource);
        repository.movement(target, "CREDIT", amount, afterTarget);
    }
}
