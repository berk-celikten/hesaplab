package com.hesaplab.worker;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptConsumerTest {
    private Message message(String id) { return new Message(id.getBytes(java.nio.charset.StandardCharsets.US_ASCII),new MessageProperties()); }

    @Test void returnsOnlyAfterProcessorCompletes() {
        var processor=mock(ReceiptProcessor.class);
        new ReceiptConsumer(processor).consume(message("123"));
        verify(processor).prepare("123");
        verify(processor,never()).markFailed(anyString());
    }

    @Test void failedProcessingMarksStatusAndRejectsWithoutAutomaticRetry() {
        var processor=mock(ReceiptProcessor.class);
        doThrow(new IllegalStateException("test")).when(processor).prepare("123");
        assertThatThrownBy(()->new ReceiptConsumer(processor).consume(message("123"))).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verify(processor).markFailed("123");
    }

    @Test void invalidMessageCannotSelectArbitrarySql() {
        var processor=mock(ReceiptProcessor.class);
        assertThatThrownBy(()->new ReceiptConsumer(processor).consume(message("1 OR 1=1"))).isInstanceOf(AmqpRejectAndDontRequeueException.class);
        verifyNoInteractions(processor);
    }
}
