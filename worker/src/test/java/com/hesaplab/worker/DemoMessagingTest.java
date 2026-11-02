package com.hesaplab.worker;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class DemoMessagingTest {
    @Test
    void topologyIsDurableAndDedicated() {
        var topology = new DemoTopology();
        var queue = topology.demoQueue();
        var exchange = topology.demoExchange();
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(queue.isExclusive()).isFalse();
        assertThat(exchange.isDurable()).isTrue();
        var binding = topology.demoBinding(queue, exchange);
        assertThat(binding.getExchange()).isEqualTo(DemoTopology.EXCHANGE);
        assertThat(binding.getDestination()).isEqualTo(DemoTopology.QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo(DemoTopology.ROUTING_KEY);
    }

    @Test
    void consumerProcessesSameMessageId(CapturedOutput output) {
        String id = UUID.randomUUID().toString();
        var properties = new MessageProperties();
        properties.setMessageId(id);
        new DemoConsumer().consume(new Message("Sentetik test".getBytes(StandardCharsets.UTF_8), properties));
        assertThat(output).contains("DEMO_PROCESSED messageId=" + id, "Sentetik test");
    }

    @Test
    void publisherUsesPersistentMessageAndConfirms(CapturedOutput output) throws Exception {
        var admin = mock(AmqpAdmin.class);
        var template = mock(RabbitTemplate.class);
        doAnswer(call -> {
            Message message = call.getArgument(2);
            CorrelationData data = call.getArgument(3);
            assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
            assertThat(UUID.fromString(message.getMessageProperties().getMessageId()).toString()).isEqualTo(data.getId());
            data.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(template).send(eq(DemoTopology.EXCHANGE), eq(DemoTopology.ROUTING_KEY), any(Message.class), any(CorrelationData.class));
        new DemoPublisher(admin, template).run();
        verify(admin).initialize();
        assertThat(output).contains("DEMO_SENT messageId=", "persistent=true");
    }

    @Test
    void rejectedPublishIsNotAutomaticallyRetried() {
        var admin = mock(AmqpAdmin.class);
        var template = mock(RabbitTemplate.class);
        doAnswer(call -> {
            CorrelationData data = call.getArgument(3);
            data.getFuture().complete(new CorrelationData.Confirm(false, "test"));
            return null;
        }).when(template).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        assertThatThrownBy(() -> new DemoPublisher(admin, template).run()).isInstanceOf(IllegalStateException.class);
        verify(template, times(1)).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
    }

    @Test
    void normalStartupDoesNotRegisterPublisher() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(DemoPublisher.class);
            context.refresh();
            assertThat(context.getBeansOfType(DemoPublisher.class)).isEmpty();
        }
    }
}
