package com.hesaplab.receipt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ReceiptService {
    public static class NotFound extends RuntimeException { }
    private final ReceiptRepository repository;
    private final OutboxRepository outbox;
    private final TransactionTemplate independent;
    public ReceiptService(ReceiptRepository repository, OutboxRepository outbox, PlatformTransactionManager manager) {
        this.repository=repository; this.outbox=outbox;
        independent=new TransactionTemplate(manager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        independent.setTimeout(10);
    }
    public void createAfterTransfer(String transferId) {
        repository.create(transferId);
        outbox.enqueue(transferId); // Transferin aynı transaction'ı; ağ çağrısı/callback yok.
    }
    public ReceiptRepository.Receipt get(String owner,String id) {
        if(id==null || !id.matches("[0-9]{1,38}") || new java.math.BigDecimal(id).signum()<=0) throw new NotFound();
        return repository.findOwned(id,owner).orElseThrow(NotFound::new);
    }
    public ReceiptRepository.Receipt requestAgain(String owner,String id) {
        return independent.execute(status -> {
            var receipt=get(owner,id);
            repository.lock(receipt.transferId());
            receipt=get(owner,receipt.transferId());
            if("READY".equals(receipt.status())) return receipt;
            // Eşzamanlı kullanıcı isteklerini receipt kilidi sıraya koyar.
            if("PENDING".equals(receipt.status()) && outbox.waiting(receipt.transferId())) return receipt;
            if(outbox.hasPending(receipt.transferId())) {
                repository.pending(receipt.transferId());
                return get(owner,receipt.transferId());
            }
            repository.pending(receipt.transferId());
            outbox.enqueue(receipt.transferId());
            return get(owner,receipt.transferId());
        });
    }
}
