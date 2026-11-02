package com.hesaplab.receipt;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceiptTopology {
    public static final String EXCHANGE = "hesaplab.receipt.exchange";
    public static final String QUEUE = "hesaplab.receipt.queue";
    public static final String ROUTING_KEY = "hesaplab.receipt.prepare";

    @Bean DirectExchange receiptExchange() { return new DirectExchange(EXCHANGE, true, false); }
    @Bean Queue receiptQueue() { return new Queue(QUEUE, true, false, false); }
    @Bean Binding receiptBinding(Queue receiptQueue, DirectExchange receiptExchange) {
        return BindingBuilder.bind(receiptQueue).to(receiptExchange).with(ROUTING_KEY);
    }
    @Bean
    org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer boundedReceiptRpc() {
        return factory -> {
            factory.setChannelRpcTimeout(3000);
            factory.setHandshakeTimeout(3000);
        };
    }
}
