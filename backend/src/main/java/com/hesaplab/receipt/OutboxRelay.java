package com.hesaplab.receipt;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OutboxRelay {
    private final OutboxRepository repository;
    private final ReceiptPublisher publisher;
    private final TransactionTemplate transaction;
    public OutboxRelay(OutboxRepository repository, ReceiptPublisher publisher, PlatformTransactionManager manager) {
        this.repository=repository; this.publisher=publisher;
        transaction=new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.setTimeout(15);
    }
    public boolean dispatchOne() {
        return Boolean.TRUE.equals(transaction.execute(status -> {
            var event=repository.lockNext();
            if(event==null) return false;
            try { publisher.publish(event.transferId(),event.eventId()); }
            catch(Exception error) {
                if(error instanceof InterruptedException) Thread.currentThread().interrupt();
                repository.failed(event);
                LoggerFactory.getLogger(OutboxRelay.class).warn("OUTBOX_PENDING eventId={} transferId={}",event.eventId(),event.transferId());
                return true;
            }
            // Bu yazı/commit başarısızsa kilit ve işlem rollback; aynı eventId tekrar yayımlanabilir.
            repository.sent(event);
            LoggerFactory.getLogger(OutboxRelay.class).info("OUTBOX_CONFIRMED eventId={} transferId={}",event.eventId(),event.transferId());
            return true;
        }));
    }
}
