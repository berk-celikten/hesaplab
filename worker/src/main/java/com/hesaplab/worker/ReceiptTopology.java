package com.hesaplab.worker;

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
    @Bean DirectExchange receiptDeadExchange() { return new DirectExchange("hesaplab.receipt.dlx", true, false); }
    @Bean Queue receiptDeadQueue() { return new Queue("hesaplab.receipt.dlq", true, false, false); }
    @Bean Binding receiptDeadBinding(Queue receiptDeadQueue, DirectExchange receiptDeadExchange) {
        return BindingBuilder.bind(receiptDeadQueue).to(receiptDeadExchange).with("hesaplab.receipt.dead");
    }
}
