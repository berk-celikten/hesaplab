package com.hesaplab.worker;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ReceiptProcessor {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public ReceiptProcessor(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.setTimeout(10);
    }

    public void prepare(String id) {
        BigDecimal transferId = new BigDecimal(id);
        transaction.executeWithoutResult(tx -> {
            String status = jdbc.queryForObject("""
                    SELECT status FROM transfer_receipts WHERE transfer_id=? FOR UPDATE WAIT 5
                    """, String.class, transferId);
            if ("READY".equals(status)) return;
            String content = jdbc.queryForObject("""
                    SELECT t.id,t.created_at,t.source_account_id,t.target_account_id,t.amount,t.currency,
                           sender.display_name AS sender_name,recipient.display_name AS recipient_name
                    FROM transfers t JOIN app_users sender ON sender.id=t.user_id
                    JOIN accounts target ON target.id=t.target_account_id
                    JOIN app_users recipient ON recipient.id=target.owner_user_id
                    WHERE t.id=?
                    """, (row, n) -> """
                    HesapLab demo metin dekontu
                    Transfer no: %s
                    Tarih: %s
                    Gönderen: %s
                    Alıcı: %s
                    Kaynak demo hesap no: %s
                    Hedef demo hesap no: %s
                    Tutar: %s %s
                    Gerçek banka dekontu değildir.
                    """.formatted(row.getString("id"), row.getObject("created_at", OffsetDateTime.class),
                    row.getString("sender_name"), row.getString("recipient_name"),
                    row.getString("source_account_id"), row.getString("target_account_id"),
                    row.getBigDecimal("amount").setScale(2).toPlainString(), row.getString("currency")), transferId);
            jdbc.update("""
                    UPDATE transfer_receipts SET status='READY',content=?,error_message=NULL,updated_at=SYSTIMESTAMP
                    WHERE transfer_id=? AND status<>'READY'
                    """, content, transferId);
        }); // Bu dönüşten önce Oracle commit tamamlanmıştır.
    }

    public void markFailed(String id) {
        transaction.executeWithoutResult(tx -> jdbc.update("""
                UPDATE transfer_receipts SET status='FAILED',content=NULL,
                error_message='Dekont hazırlanamadı. Dekontu tekrar isteyin.',updated_at=SYSTIMESTAMP
                WHERE transfer_id=? AND status<>'READY'
                """, new BigDecimal(id)));
    }
}
