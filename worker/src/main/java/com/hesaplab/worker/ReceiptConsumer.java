package com.hesaplab.worker;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.*;
import org.springframework.stereotype.Component;

@Component
public class ReceiptConsumer {
    private static final Logger log = LoggerFactory.getLogger(ReceiptConsumer.class);
    private final ReceiptProcessor processor;
    public ReceiptConsumer(ReceiptProcessor processor) { this.processor = processor; }

    static boolean transientFailure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof TransientDataAccessException || cause instanceof DataAccessResourceFailureException
                    || cause instanceof SQLTransientException || cause instanceof SQLRecoverableException) return true;
            if (cause instanceof SQLException sql && switch (sql.getErrorCode()) {
                case 54, 60, 30006, 8177, 3113, 3114, 17002, 17008, 12514, 12541 -> true;
                default -> false;
            }) return true;
        }
        return false;
    }

    @RabbitListener(queues = ReceiptTopology.QUEUE, containerFactory = "receiptListenerFactory")
    public void consume(Message message) {
        String id = new String(message.getBody(), StandardCharsets.US_ASCII);
        String messageId;
        try { messageId = UUID.fromString(message.getMessageProperties().getMessageId()).toString(); }
        catch (RuntimeException invalid) { messageId = "missing-or-invalid"; }
        if (!id.matches("[1-9][0-9]{0,37}")) {
            log.warn("RECEIPT_DLQ messageId={} transferId=invalid attempts=0 reason=invalid", messageId);
            throw new AmqpRejectAndDontRequeueException("Geçersiz dekont mesajı.");
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("RECEIPT_ATTEMPT messageId={} transferId={} attempt={}", messageId, id, attempt);
            try {
                processor.prepare(id); // Her deneme bağımsız Oracle transaction'ıdır.
                log.info("RECEIPT_READY messageId={} transferId={} attempt={}", messageId, id, attempt);
                return; // AUTO ack ancak Oracle commit'inden sonra.
            } catch (RuntimeException error) {
                boolean retry = transientFailure(error) && attempt < 3;
                if (retry) {
                    try { Thread.sleep(attempt * 1000L); }
                    catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        // Kesintide bütçe kalıcı değildir; bu teslim henüz ack edilmedi.
                        throw new IllegalStateException("Dekont beklemesi kesildi.");
                    }
                } else {
                    try { processor.markFailed(id); }
                    catch (RuntimeException unavailable) {
                        log.warn("RECEIPT_STATUS_UNAVAILABLE messageId={} transferId={}", messageId, id);
                    }
                    log.warn("RECEIPT_DLQ messageId={} transferId={} attempts={} reason={}",
                            messageId, id, attempt, transientFailure(error) ? "exhausted" : "permanent");
                    throw new AmqpRejectAndDontRequeueException("Dekont son denemede başarısız.");
                }
            }
        }
    }
}
