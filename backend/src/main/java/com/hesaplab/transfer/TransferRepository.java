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
    public void movement(BigDecimal id, String direction, BigDecimal amount, BigDecimal balance, String transferId) {
        jdbc.update("INSERT INTO account_movements (account_id, direction, amount, balance_after, description, transfer_id) VALUES (?, ?, ?, ?, ?, ?)",
                id, direction, amount, balance, "Kendi hesapları arasında transfer", new BigDecimal(transferId));
    }
    public record Stored(String id, BigDecimal source, BigDecimal target, BigDecimal amount) { }
    public java.util.Optional<Stored> find(String owner,String key) {
        return jdbc.query("SELECT id,source_account_id,target_account_id,amount FROM transfers WHERE user_id=? AND idempotency_key=?",
            (r,n)->new Stored(r.getString(1),r.getBigDecimal(2),r.getBigDecimal(3),r.getBigDecimal(4)),new BigDecimal(owner),key).stream().findFirst();
    }
    public void lockOwner(String owner) {
        jdbc.queryForObject("SELECT id FROM app_users WHERE id=? FOR UPDATE WAIT 5",String.class,new BigDecimal(owner));
    }
    public String insertTransfer(String owner,String key,BigDecimal source,BigDecimal target,BigDecimal amount) {
        var holder=new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(connection->{
            var statement=connection.prepareStatement("INSERT INTO transfers(user_id,idempotency_key,source_account_id,target_account_id,amount,currency) VALUES (?,?,?,?,?,'TRY')",new String[]{"ID"});
            statement.setBigDecimal(1,new BigDecimal(owner));statement.setString(2,key);statement.setBigDecimal(3,source);statement.setBigDecimal(4,target);statement.setBigDecimal(5,amount);return statement;
        },holder);
        return holder.getKey().toString();
    }}
