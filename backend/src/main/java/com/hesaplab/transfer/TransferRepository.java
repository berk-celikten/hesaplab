package com.hesaplab.transfer;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransferRepository {
    public record LockedAccount(BigDecimal id, BigDecimal balance, String currency) { }
    private final JdbcTemplate jdbc;
    public TransferRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public LockedAccount lock(BigDecimal id, String owner) {
        return jdbc.query("SELECT id, balance, currency FROM accounts WHERE id = ? AND owner_user_id = ? FOR UPDATE WAIT 5",
                (row, n) -> new LockedAccount(row.getBigDecimal(1), row.getBigDecimal(2), row.getString(3)),
                id, new BigDecimal(owner)).stream().findFirst().orElseThrow(() -> new TransferService.Rejected("Hesaplar kullanılamıyor."));
    }
    public void update(BigDecimal id, BigDecimal balance) {
        if (jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?", balance, id) != 1) {
            throw new IllegalStateException("Hesap güncellenemedi.");
        }
    }
    public void movement(BigDecimal id, String direction, BigDecimal amount, BigDecimal balance) {
        jdbc.update("INSERT INTO account_movements (account_id, direction, amount, balance_after, description) VALUES (?, ?, ?, ?, ?)",
                id, direction, amount, balance, "Kendi hesapları arasında transfer");
    }
}
