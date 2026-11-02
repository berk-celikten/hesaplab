package com.hesaplab.receipt;

import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="hesaplab.outbox.enabled",havingValue="true",matchIfMissing=true)
public class OutboxScheduler {
    private final OutboxRelay relay;
    public OutboxScheduler(OutboxRelay relay) { this.relay=relay; }
    @Scheduled(fixedDelayString="${hesaplab.outbox.delay-ms:1000}")
    public void dispatch() {
        try { relay.dispatchOne(); }
        catch(RuntimeException failure) {
            LoggerFactory.getLogger(OutboxScheduler.class).warn("OUTBOX_ROLLBACK_PENDING");
        }
    }
}
