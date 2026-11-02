package com.hesaplab.receipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.*;

@Service
public class ReceiptService {
    public static class NotFound extends RuntimeException { }
    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
    private final ReceiptRepository repository;
    private final ReceiptPublisher publisher;
    private final TransactionTemplate independent;

    public ReceiptService(ReceiptRepository repository, ReceiptPublisher publisher, PlatformTransactionManager manager) {
        this.repository = repository;
        this.publisher = publisher;
        independent = new TransactionTemplate(manager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        independent.setTimeout(10);
    }

    // Transfer transaction'ının içinden çağrılır; rollback olursa callback çalışmaz.
    public void createAfterTransfer(String transferId) {
        repository.create(transferId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { dispatch(transferId); }
        });
    }

    public ReceiptRepository.Receipt get(String owner, String id) {
        if (id == null || !id.matches("[0-9]{1,38}") || new java.math.BigDecimal(id).signum() <= 0) throw new NotFound();
        return repository.findOwned(id, owner).orElseThrow(NotFound::new);
    }

    public ReceiptRepository.Receipt requestAgain(String owner, String id) {
        var receipt = get(owner, id);
        if ("READY".equals(receipt.status())) return receipt;
        // Transaction bitince yayınla; READY sonucu geç gelen istekle bozulamaz.
        String transferId = receipt.transferId();
        int changed = independent.execute(status -> repository.pending(transferId));
        if (changed != 0) dispatch(transferId);
        return get(owner, transferId);
    }

    private void dispatch(String transferId) {
        try {
            publisher.publish(transferId);
        } catch (Exception error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            // afterCommit içinde eski transaction'a yazmak kalıcı olmayabilir.
            // Ayrı transaction gerekir; bu hatanın transfer HTTP sonucunu bozmasına izin verilmez.
            try {
                independent.executeWithoutResult(status -> repository.failed(transferId));
            } catch (RuntimeException unavailable) {
                log.warn("Dekont hata durumu kaydedilemedi; transferId={}", transferId);
            }
            log.warn("Dekont yayını doğrulanamadı; transferId={}", transferId);
        }
    }
}
