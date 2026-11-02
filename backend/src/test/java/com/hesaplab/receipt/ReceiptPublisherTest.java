package com.hesaplab.receipt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptPublisherTest {
    private RabbitTemplate template(boolean ack) {
        var template = mock(RabbitTemplate.class);
        doAnswer(call -> {
            Message message = call.getArgument(2);
            CorrelationData correlation = call.getArgument(3);
            assertThat(new String(message.getBody(), StandardCharsets.US_ASCII)).isEqualTo("123");
            assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
            assertThat(UUID.fromString(message.getMessageProperties().getMessageId()).toString()).isEqualTo(correlation.getId());
            assertThat(message.getMessageProperties().getHeaders()).isEmpty();
            correlation.getFuture().complete(new CorrelationData.Confirm(ack, null));
            return null;
        }).when(template).send(eq(ReceiptTopology.EXCHANGE),eq(ReceiptTopology.ROUTING_KEY),any(Message.class),any(CorrelationData.class));
        return template;
    }

    @Test void publishesOnlyIdAsPersistentMessageWithConfirm() throws Exception {
        var admin = mock(AmqpAdmin.class);
        new ReceiptPublisher(admin, template(true)).publish("123");
        verify(admin).initialize();
    }

    @Test void nackIsNotReportedAsSuccessfulPublication() {
        assertThatThrownBy(() -> new ReceiptPublisher(mock(AmqpAdmin.class),template(false)).publish("123"))
            .isInstanceOf(IllegalStateException.class);
    }
}
