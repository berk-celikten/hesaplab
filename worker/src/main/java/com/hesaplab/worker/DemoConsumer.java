package com.hesaplab.worker;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DemoConsumer {
    private static final Logger log = LoggerFactory.getLogger(DemoConsumer.class);

    @RabbitListener(queues = DemoTopology.QUEUE)
    public void consume(Message message) {
        String id = UUID.fromString(message.getMessageProperties().getMessageId()).toString();
        String text = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("DEMO_PROCESSED messageId={} text={}", id, text);
    }
}
