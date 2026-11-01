package com.hesaplab.auth;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<AppUserDetails> findByUsername(String username) {
        return jdbc.query("""
                SELECT id, username, display_name, role, password_hash
                FROM app_users WHERE username = ?
                """, (row, index) -> new AppUserDetails(
                new UserDto(row.getString("id"), row.getString("username"),
                        row.getString("display_name"), row.getString("role")),
                row.getString("password_hash")), username).stream().findFirst();
    }
}
