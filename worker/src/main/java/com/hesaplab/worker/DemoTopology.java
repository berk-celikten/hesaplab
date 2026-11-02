package com.hesaplab.worker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoTopology {
    public static final String EXCHANGE = "hesaplab.demo.exchange";
    public static final String QUEUE = "hesaplab.demo.queue";
    public static final String ROUTING_KEY = "hesaplab.demo.message";

    @Bean
    DirectExchange demoExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue demoQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    @Bean
    Binding demoBinding(Queue demoQueue, DirectExchange demoExchange) {
        return BindingBuilder.bind(demoQueue).to(demoExchange).with(ROUTING_KEY);
    }
}
