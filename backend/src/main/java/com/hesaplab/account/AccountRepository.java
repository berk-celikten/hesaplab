package com.hesaplab.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<AccountDto> mapper = (row, index) -> new AccountDto(
            row.getString("id"), row.getString("owner_user_id"), row.getString("name"),
            row.getBigDecimal("balance").setScale(2).toPlainString(), row.getString("currency"));
    public AccountRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<AccountDto> findByOwner(String ownerId) {
        return jdbc.query("""
                SELECT id, owner_user_id, name, balance, currency FROM accounts
                WHERE owner_user_id = ? ORDER BY id
                """, mapper, new BigDecimal(ownerId));
    }
    public Optional<AccountDto> findOwnedAccount(String accountId, String ownerId) {
        return jdbc.query("""
                SELECT id, owner_user_id, name, balance, currency FROM accounts
                WHERE id = ? AND owner_user_id = ?
                """, mapper, new BigDecimal(accountId), new BigDecimal(ownerId)).stream().findFirst();
    }
    public List<MovementDto> findMovements(String accountId, String ownerId) {
        return jdbc.query("""
                SELECT m.id, m.direction, m.amount, m.balance_after, m.description, m.created_at,
                       CASE WHEN m.direction='DEBIT' AND t.user_id=? THEN r.transfer_id END AS receipt_transfer_id
                FROM account_movements m JOIN accounts a ON a.id = m.account_id
                LEFT JOIN transfers t ON t.id=m.transfer_id
                LEFT JOIN transfer_receipts r ON r.transfer_id=t.id
                WHERE a.id = ? AND a.owner_user_id = ?
                ORDER BY m.created_at DESC, m.id DESC FETCH FIRST 50 ROWS ONLY
                """, (row, index) -> new MovementDto(row.getString("id"), row.getString("direction"),
                row.getBigDecimal("amount").setScale(2).toPlainString(),
                row.getBigDecimal("balance_after").setScale(2).toPlainString(), row.getString("description"),
                row.getObject("created_at", java.time.OffsetDateTime.class).toString(), row.getString("receipt_transfer_id")),
                new BigDecimal(ownerId), new BigDecimal(accountId), new BigDecimal(ownerId));
    }}
