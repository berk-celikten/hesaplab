package com.hesaplab.receipt;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxRepository {
    public record Event(String eventId, String transferId, int attempts) { }
    private final JdbcTemplate jdbc;
    public OutboxRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    public void enqueue(String transferId) {
        jdbc.update("""
                INSERT INTO receipt_outbox(event_id,transfer_id,request_no)
                SELECT ?,?,NVL(MAX(request_no),0)+1 FROM receipt_outbox WHERE transfer_id=?
                """, UUID.randomUUID().toString(),new BigDecimal(transferId),new BigDecimal(transferId));
    }
    public boolean waiting(String transferId) {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM receipt_outbox WHERE transfer_id=? AND
                (status='PENDING' OR sent_at>SYSTIMESTAMP-INTERVAL '30' SECOND)
                """,Integer.class,new BigDecimal(transferId))>0;
    }
    public boolean hasPending(String id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM receipt_outbox WHERE transfer_id=? AND status='PENDING'",Integer.class,new BigDecimal(id))>0;
    }
    public Event lockNext() {
        // FETCH FIRST, Oracle19c FOR UPDATE ile birlikte kullanılamaz.
        // Tek satır prefetch/maxRows; kilit publish ve SENT transaction'ı boyunca tutulur.
        return jdbc.query(connection -> {
            var statement=connection.prepareStatement("""
                    SELECT event_id,transfer_id,attempts FROM receipt_outbox
                    WHERE status='PENDING' AND next_attempt_at<=SYSTIMESTAMP
                    ORDER BY next_attempt_at,created_at,event_id FOR UPDATE SKIP LOCKED
                    """);
            statement.setFetchSize(1);
            statement.setMaxRows(1);
            return statement;
        }, (org.springframework.jdbc.core.ResultSetExtractor<Event>) rows -> rows.next()
                ? new Event(rows.getString(1),rows.getString(2),rows.getInt(3)) : null);
    }
    public void sent(Event event) {
        jdbc.update("""
                UPDATE receipt_outbox SET status='SENT',attempts=LEAST(attempts+1,999999999),
                sent_at=SYSTIMESTAMP,last_error=NULL WHERE event_id=?
                """,event.eventId());
    }
    public void failed(Event event) {
        int delay=Math.min(60,1 << Math.min(event.attempts()+1,6));
        jdbc.update("""
                UPDATE receipt_outbox SET attempts=LEAST(attempts+1,999999999),
                next_attempt_at=SYSTIMESTAMP+NUMTODSINTERVAL(?,'SECOND'),
                last_error='BROKER_UNCONFIRMED' WHERE event_id=?
                """,delay,event.eventId());
    }
}
