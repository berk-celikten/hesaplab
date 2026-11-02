package com.hesaplab.receipt;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReceiptRepository {
    public record Receipt(String transferId, String status, String content, String errorMessage) { }
    private final JdbcTemplate jdbc;

    public ReceiptRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void lock(String id) {
        jdbc.queryForObject("SELECT id FROM transfer_receipts WHERE transfer_id=? FOR UPDATE WAIT 5",String.class,new BigDecimal(id));
    }
    public void create(String transferId) {
        jdbc.update("INSERT INTO transfer_receipts (transfer_id) VALUES (?)", new BigDecimal(transferId));
    }

    public Optional<Receipt> findOwned(String transferId, String owner) {
        return jdbc.query("""
                SELECT r.transfer_id, r.status, r.content, r.error_message
                FROM transfer_receipts r JOIN transfers t ON t.id=r.transfer_id
                WHERE r.transfer_id=? AND t.user_id=?
                """, (row, n) -> new Receipt(row.getString(1), row.getString(2), row.getString(3), row.getString(4)),
                new BigDecimal(transferId), new BigDecimal(owner)).stream().findFirst();
    }

    public int pending(String transferId) {
        return jdbc.update("""
                UPDATE transfer_receipts SET status='PENDING', content=NULL, error_message=NULL, updated_at=SYSTIMESTAMP
                WHERE transfer_id=? AND status<>'READY'
                """, new BigDecimal(transferId));
    }

    public void failed(String transferId) {
        jdbc.update("""
                UPDATE transfer_receipts SET status='FAILED', content=NULL,
                error_message='Dekont kuyruğa iletilemedi. Dekontu tekrar isteyin.', updated_at=SYSTIMESTAMP
                WHERE transfer_id=? AND status<>'READY'
                """, new BigDecimal(transferId));
    }
}
