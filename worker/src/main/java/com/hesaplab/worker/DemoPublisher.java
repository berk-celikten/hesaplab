package com.hesaplab.worker;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("send-demo")
public class DemoPublisher implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoPublisher.class);
    private final AmqpAdmin admin;
    private final RabbitTemplate template;

    public DemoPublisher(AmqpAdmin admin, RabbitTemplate template) {
        this.admin = admin;
        this.template = template;
    }

    @Override
    public void run(String... args) throws Exception {
        admin.initialize(); // Worker kapalıyken de exchange, kuyruk ve binding hazır olsun.
        String id = UUID.randomUUID().toString();
        var properties = new MessageProperties();
        properties.setMessageId(id);
        properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        properties.setContentEncoding("UTF-8");
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        var message = new Message("HesapLab sentetik demo mesajı.".getBytes(StandardCharsets.UTF_8), properties);
        var correlation = new CorrelationData(id);

        template.send(DemoTopology.EXCHANGE, DemoTopology.ROUTING_KEY, message, correlation);
        var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
        if (!confirm.isAck() || correlation.getReturned() != null) {
            throw new IllegalStateException("Demo mesajı kuyruğa doğrulanamadı; otomatik tekrar yapılmadı.");
        }
        log.info("DEMO_SENT messageId={} persistent=true", id);
    }
}
