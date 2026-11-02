package com.hesaplab.receipt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReceiptPublisher {
    private final AmqpAdmin admin;
    private final RabbitTemplate template;

    public ReceiptPublisher(AmqpAdmin admin, RabbitTemplate template) {
        this.admin = admin;
        this.template = template;
    }

    public void publish(String transferId, String eventId) throws Exception {
        admin.initialize();
        var properties = new MessageProperties();
        properties.setMessageId(eventId);
        properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        // Gövdede yalnız transfer kimliği; kişi/bakiye/oturum bilgisi yoktur.
        var message = new Message(transferId.getBytes(StandardCharsets.US_ASCII), properties);
        var correlation = new CorrelationData(properties.getMessageId());
        template.send(ReceiptTopology.EXCHANGE, ReceiptTopology.ROUTING_KEY, message, correlation);
        var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
        if (!confirm.isAck() || correlation.getReturned() != null) {
            throw new IllegalStateException("Dekont yayın onayı alınamadı.");
        }
    }
}
