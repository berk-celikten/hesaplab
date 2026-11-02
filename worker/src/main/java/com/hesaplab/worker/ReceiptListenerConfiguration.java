package com.hesaplab.worker;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceiptListenerConfiguration {
    @Bean
    SimpleRabbitListenerContainerFactory receiptListenerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setDefaultRequeueRejected(false);
        // Varsayılan error handler mesaj gövdesini/stack trace'i loglayabilir.
        factory.setErrorHandler(error -> LoggerFactory.getLogger(ReceiptListenerConfiguration.class)
                .warn("RECEIPT_DELIVERY_REJECTED"));
        return factory;
    }
}
