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
}
