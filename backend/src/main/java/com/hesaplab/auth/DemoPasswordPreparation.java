package com.hesaplab.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Profile("prepare-passwords")
public class DemoPasswordPreparation implements CommandLineRunner {
    private record LocalCredential(String username, String password) { }
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final ObjectMapper mapper;
    private final TransactionTemplate transaction;

    public DemoPasswordPreparation(JdbcTemplate jdbc, PasswordEncoder encoder,
                                   ObjectMapper mapper, TransactionTemplate transaction) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.mapper = mapper;
        this.transaction = transaction;
    }

    @Override
    public void run(String... args) {
        Path file = Path.of("../.tools/demo-logins.local.json").toAbsolutePath().normalize();
        List<LocalCredential> credentials = new ArrayList<>();
        if (Files.exists(file)) {
            try {
                credentials.addAll(mapper.readValue(file.toFile(), new TypeReference<List<LocalCredential>>() { }));
            } catch (IOException exception) {
                throw new IllegalStateException("Yerel parola dosyası okunamadı; içerik çıktıya yazılmadı.");
            }
        }
        int updated = transaction.execute(status -> {
            int demoCount = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM app_users
                    WHERE username IN ('demo_customer_01', 'demo_customer_02', 'demo_admin')
                    """, Integer.class);
            if (demoCount != 3) { throw new IllegalStateException("Üç mevcut demo kullanıcı bulunmalı."); }
            List<String> missing = jdbc.queryForList("""
                    SELECT username FROM app_users WHERE password_hash IS NULL
                    AND username IN ('demo_customer_01', 'demo_customer_02', 'demo_admin') ORDER BY id
                    """, String.class);
            int count = 0;
            for (String username : missing) {
                String password = credentials.stream().filter(entry -> entry.username().equals(username))
                        .map(LocalCredential::password).findFirst().orElseGet(() -> {
                            byte[] bytes = new byte[24];
                            new SecureRandom().nextBytes(bytes);
                            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                        });
                int changed = jdbc.update("UPDATE app_users SET password_hash = ? WHERE username = ? AND password_hash IS NULL",
                        encoder.encode(password), username);
                if (changed == 1) {
                    credentials.removeIf(entry -> entry.username().equals(username));
                    credentials.add(new LocalCredential(username, password));
                    count++;
                }
            }
            if (count > 0) { writeCredentials(file, credentials); }
            return count;
        });
        if (!Files.exists(file)) {
            throw new IllegalStateException("Mevcut hashler korundu; yerel parola dosyası yok. Otomatik sıfırlama yapılmadı.");
        }
        System.out.println("Parola hazırlığı tamamlandı; yeni parola alanı doldurulan kullanıcı: " + updated
                + ". Değerler yalnız Git dışındaki .tools/demo-logins.local.json dosyasında.");
    }

    private void writeCredentials(Path file, List<LocalCredential> credentials) {
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "demo-logins-", ".local");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), credentials);
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Parola dosyası yazılamadı; veritabanı değişiklikleri geri alınacak.");
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Değerleri loglama. */ }
            }
        }
    }
}
