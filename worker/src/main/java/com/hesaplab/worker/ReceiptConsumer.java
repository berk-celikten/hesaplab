package com.hesaplab.worker;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiptConsumer {
    private static final Logger log = LoggerFactory.getLogger(ReceiptConsumer.class);
    private final ReceiptProcessor processor;
    public ReceiptConsumer(ReceiptProcessor processor) { this.processor = processor; }

    @RabbitListener(queues = ReceiptTopology.QUEUE)
    public void consume(Message message) {
        String id = new String(message.getBody(), StandardCharsets.US_ASCII);
        if (!id.matches("[1-9][0-9]{0,37}")) {
            throw new AmqpRejectAndDontRequeueException("Geçersiz dekont mesajı.");
        }
        try {
            processor.prepare(id);
            log.info("RECEIPT_READY transferId={}", id);
        } catch (RuntimeException error) {
            try { processor.markFailed(id); }
            catch (RuntimeException unavailable) { log.warn("Dekont hata durumu kaydedilemedi; transferId={}", id); }
            throw new AmqpRejectAndDontRequeueException("Dekont hazırlanamadı.");
        }
        // AUTO ack ancak prepare transaction'ı commit olup listener döndükten sonra gönderilir.
    }
}
