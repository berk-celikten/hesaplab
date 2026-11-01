package com.hesaplab.admin;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import com.hesaplab.auth.UserDto;
@Repository
public class AdminRepository {
    public record Account(String id, String name, String balance, String currency, String ownerId, String ownerName) { }
    private final JdbcTemplate jdbc;
    public AdminRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<UserDto> users() {
        return jdbc.query("SELECT id, username, display_name, role FROM app_users ORDER BY id",
            (r,n)->new UserDto(r.getString(1),r.getString(2),r.getString(3),r.getString(4)));
    }
    public List<Account> accounts() {
        return jdbc.query("SELECT a.id,a.name,a.balance,a.currency,u.id,u.display_name FROM accounts a JOIN app_users u ON u.id=a.owner_user_id ORDER BY a.id",
            (r,n)->new Account(r.getString(1),r.getString(2),r.getBigDecimal(3).setScale(2).toPlainString(),r.getString(4),r.getString(5),r.getString(6)));
    }
    public String create(BigDecimal owner, String name) {
        var owners=jdbc.queryForList("SELECT id FROM app_users WHERE id=? AND role='CUSTOMER' FOR UPDATE WAIT 5",BigDecimal.class,owner);
        if(owners.isEmpty()) throw new AdminService.InvalidAccount("Geçerli bir müşteri seçin.");
        var key=new GeneratedKeyHolder();
        jdbc.update(connection->{
            var statement=connection.prepareStatement("INSERT INTO accounts(owner_user_id,name,balance,currency) VALUES (?,?,0,'TRY')",new String[]{"ID"});
            statement.setBigDecimal(1,owner); statement.setString(2,name); return statement;
        },key);
        return key.getKey().toString();
    }
}
