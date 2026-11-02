package com.hesaplab.worker;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties="spring.rabbitmq.listener.simple.auto-startup=false")
@EnabledIfEnvironmentVariable(named="HESAPLAB_ORACLE_TEST", matches="true")
class ReceiptOracleTest {
    @MockitoSpyBean JdbcTemplate jdbc;
    @MockitoSpyBean ReceiptProcessor processor;
    List<Map<String,Object>> outboxBefore;
    String owner, source, target, transfer;
    List<Map<String,Object>> usersBefore, accountsBefore, movementsBefore, transfersBefore, receiptsBefore;

    @BeforeEach void setup() {
        outboxBefore=jdbc.queryForList("SELECT * FROM receipt_outbox ORDER BY event_id");
        usersBefore=jdbc.queryForList("SELECT * FROM app_users ORDER BY id");
        accountsBefore=jdbc.queryForList("SELECT * FROM accounts ORDER BY id");
        movementsBefore=jdbc.queryForList("SELECT * FROM account_movements ORDER BY id");
        transfersBefore=jdbc.queryForList("SELECT * FROM transfers ORDER BY id");
        receiptsBefore=jdbc.queryForList("SELECT id,transfer_id,status,TO_CHAR(content) AS content,error_message,created_at,updated_at FROM transfer_receipts ORDER BY id");
        String username="receipt_test_"+UUID.randomUUID().toString().substring(0,8);
        jdbc.update("INSERT INTO app_users(username,display_name,role) VALUES (?,'Sentetik worker testi','CUSTOMER')",username);
        owner=jdbc.queryForObject("SELECT id FROM app_users WHERE username=?",String.class,username);
        jdbc.update("INSERT INTO accounts(owner_user_id,name,balance) VALUES (?,'Source',100)",owner);
        jdbc.update("INSERT INTO accounts(owner_user_id,name,balance) VALUES (?,'Target',20)",owner);
        source=jdbc.queryForObject("SELECT id FROM accounts WHERE owner_user_id=? AND name='Source'",String.class,owner);
        target=jdbc.queryForObject("SELECT id FROM accounts WHERE owner_user_id=? AND name='Target'",String.class,owner);
        jdbc.update("INSERT INTO transfers(user_id,idempotency_key,source_account_id,target_account_id,amount) VALUES (?,?,?,?,25.5)",owner,UUID.randomUUID().toString(),source,target);
        transfer=jdbc.queryForObject("SELECT id FROM transfers WHERE user_id=?",String.class,owner);
        jdbc.update("INSERT INTO transfer_receipts(transfer_id) VALUES (?)",transfer);
    }

    @AfterEach void cleanup() {
        reset(jdbc);
        jdbc.update("DELETE FROM receipt_outbox WHERE transfer_id=?",transfer);
        jdbc.update("DELETE FROM transfer_receipts WHERE transfer_id=?",transfer);
        jdbc.update("DELETE FROM transfers WHERE user_id=?",owner);
        jdbc.update("DELETE FROM accounts WHERE owner_user_id=?",owner);
        jdbc.update("DELETE FROM app_users WHERE id=?",owner);
        assertThat(jdbc.queryForList("SELECT * FROM receipt_outbox ORDER BY event_id")).isEqualTo(outboxBefore);
        assertThat(jdbc.queryForList("SELECT * FROM app_users ORDER BY id")).isEqualTo(usersBefore);
        assertThat(jdbc.queryForList("SELECT * FROM accounts ORDER BY id")).isEqualTo(accountsBefore);
        assertThat(jdbc.queryForList("SELECT * FROM account_movements ORDER BY id")).isEqualTo(movementsBefore);
        assertThat(jdbc.queryForList("SELECT * FROM transfers ORDER BY id")).isEqualTo(transfersBefore);
        assertThat(jdbc.queryForList("SELECT id,transfer_id,status,TO_CHAR(content) AS content,error_message,created_at,updated_at FROM transfer_receipts ORDER BY id")).isEqualTo(receiptsBefore);
    }

    @Test void duplicateAndParallelPreparationKeepOneReadyResult() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            for(var result:pool.invokeAll(List.<Callable<Void>>of(()->{processor.prepare(transfer);return null;},()->{processor.prepare(transfer);return null;}))) result.get();
        } finally {pool.shutdownNow();}
        var ready=jdbc.queryForMap("SELECT status,TO_CHAR(content) AS content,updated_at FROM transfer_receipts WHERE transfer_id=?",transfer);
        assertThat(ready.get("STATUS")).isEqualTo("READY");
        assertThat(ready.get("CONTENT").toString()).contains("25.50 TRY","Sentetik worker testi","Kaynak demo hesap no: "+source,"Hedef demo hesap no: "+target).doesNotContain("balance","100.00","20.00");
        processor.prepare(transfer);
        processor.markFailed(transfer); // Eski hata READY sonucunu bozamaz.
        assertThat(jdbc.queryForMap("SELECT status,TO_CHAR(content) AS content,updated_at FROM transfer_receipts WHERE transfer_id=?",transfer)).isEqualTo(ready);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfer_receipts WHERE transfer_id=?",Integer.class,transfer)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT SUM(balance) FROM accounts WHERE owner_user_id=?",Integer.class,owner)).isEqualTo(120);
    }

    @Test void actualOracleWriteFailureCannotLeaveReadyAndLaterRequestCanPrepare() {
        doAnswer(call -> {
            // Üretim hata anahtarı/DDL yok: yalnız test spy'ı gerçek CHECK hatası üretir.
            return jdbc.update("UPDATE transfer_receipts SET status='INVALID' WHERE transfer_id=?",transfer);
        }).when(jdbc).update(startsWith("UPDATE transfer_receipts SET status='READY'"),any(Object[].class));
        assertThatThrownBy(()->processor.prepare(transfer)).isInstanceOf(org.springframework.dao.DataAccessException.class);
        reset(jdbc);
        assertThat(jdbc.queryForObject("SELECT status FROM transfer_receipts WHERE transfer_id=?",String.class,transfer)).isEqualTo("PENDING");
        processor.markFailed(transfer);
        assertThat(jdbc.queryForObject("SELECT status FROM transfer_receipts WHERE transfer_id=?",String.class,transfer)).isEqualTo("FAILED");
        processor.prepare(transfer);
        assertThat(jdbc.queryForObject("SELECT status FROM transfer_receipts WHERE transfer_id=?",String.class,transfer)).isEqualTo("READY");
    }

    @Autowired ReceiptConsumer consumer;
    @org.springframework.beans.factory.annotation.Qualifier("receiptListenerFactory")
    @Autowired org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory listenerFactory;
    @Autowired org.springframework.amqp.rabbit.core.RabbitTemplate rabbit;

    private org.springframework.amqp.core.Message receiptMessage(String body) {
        var properties=new org.springframework.amqp.core.MessageProperties();
        properties.setMessageId(UUID.randomUUID().toString());
        properties.setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
        return new org.springframework.amqp.core.Message(body.getBytes(java.nio.charset.StandardCharsets.US_ASCII),properties);
    }

    private void consuming(Runnable action) {
        int deadCount = rabbit.execute(channel -> channel.queueDeclarePassive("hesaplab.receipt.dlq").getMessageCount());
        assertThat(deadCount).isZero();
        var container=listenerFactory.createListenerContainer();
        container.setQueueNames(ReceiptTopology.QUEUE);
        container.setMessageListener((org.springframework.amqp.core.MessageListener) consumer::consume);
        container.start();
        try { action.run(); } finally { container.stop(); }
    }

    private void send(org.springframework.amqp.core.Message message) {
        rabbit.send(ReceiptTopology.EXCHANGE,ReceiptTopology.ROUTING_KEY,message);
    }

    private void ready() {
        for(int n=0;n<100;n++) {
            if("READY".equals(jdbc.queryForObject("SELECT status FROM transfer_receipts WHERE transfer_id=?",String.class,transfer))) return;
            try {Thread.sleep(100);} catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}
        }
        throw new AssertionError("READY bekleniyordu");
    }

    @Test void realBrokerTwoTransientFailuresThenReadyWithoutDlq() {
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("test"))
            .doThrow(new org.springframework.dao.DataAccessResourceFailureException("test"))
            .doCallRealMethod().when(processor).prepare(transfer);
        consuming(() -> {
            long start=System.nanoTime();
            send(receiptMessage(transfer));
            ready();
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)).isGreaterThanOrEqualTo(2900);
            verify(processor,times(3)).prepare(transfer);
            verify(processor,never()).markFailed(anyString());
            assertThat(rabbit.receive("hesaplab.receipt.dlq",200)).isNull();
        });
    }

    @Test void realBrokerExhaustionOneDeadLetterAndSelectedRedrive() {
        doThrow(new org.springframework.dao.DataAccessResourceFailureException("test")).when(processor).prepare(transfer);
        consuming(() -> {
            var message=receiptMessage(transfer);
            long start=System.nanoTime();
            send(message);
            var dead=rabbit.receive("hesaplab.receipt.dlq",10000);
            assertThat(dead).isNotNull();
            assertThat(dead.getMessageProperties().getMessageId()).isEqualTo(message.getMessageProperties().getMessageId());
            assertThat(dead.getMessageProperties().getHeaders().get("x-death").toString()).contains("rejected","hesaplab.receipt.queue");
            verify(processor,times(3)).prepare(transfer);
            verify(processor).markFailed(transfer);
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)).isGreaterThanOrEqualTo(2900);
            assertThat(jdbc.queryForObject("SELECT status FROM transfer_receipts WHERE transfer_id=?",String.class,transfer)).isEqualTo("FAILED");
            assertThat(rabbit.receive("hesaplab.receipt.dlq",200)).isNull();
            reset(processor);
            // Yalnız yukarıda kimliği doğrulanan tek sentetik mesaj geri gönderilir.
            var properties=new org.springframework.amqp.core.MessageProperties();
            properties.setMessageId(dead.getMessageProperties().getMessageId());
            properties.setDeliveryMode(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
            send(new org.springframework.amqp.core.Message(dead.getBody(),properties));
            ready();
            var snapshot=jdbc.queryForMap("SELECT TO_CHAR(content) AS content,updated_at FROM transfer_receipts WHERE transfer_id=?",transfer);
            send(message);
            try {Thread.sleep(400);}catch(InterruptedException e){throw new AssertionError(e);}
            assertThat(jdbc.queryForMap("SELECT TO_CHAR(content) AS content,updated_at FROM transfer_receipts WHERE transfer_id=?",transfer)).isEqualTo(snapshot);
            assertThat(rabbit.receive("hesaplab.receipt.dlq",200)).isNull();
        });
    }

    @Test void realBrokerPermanentInvalidAndMissingDoNotRetry() {
        consuming(() -> {
            doThrow(new org.springframework.dao.DataIntegrityViolationException("test")).when(processor).prepare(transfer);
            for(String body:List.of(transfer,"not-a-transfer","999999999999999999")) {
                var message=receiptMessage(body);
                send(message);
                var dead=rabbit.receive("hesaplab.receipt.dlq",5000);
                assertThat(dead).isNotNull();
                assertThat(dead.getMessageProperties().getMessageId()).isEqualTo(message.getMessageProperties().getMessageId());
            }
            verify(processor,times(1)).prepare(transfer);
            verify(processor,times(1)).prepare("999999999999999999");
            assertThat(rabbit.receive("hesaplab.receipt.dlq",200)).isNull();
        });
    }

}
