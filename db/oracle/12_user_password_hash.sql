-- 11.2: Yalnız bir kez, ayrı bağlantıda uygulanır; eski kullanıcılar korunur.
-- Önce USER_TAB_COLUMNS üzerinden sütunun henüz bulunmadığını kontrol edin.
-- Parolaları/hash'leri bu dosyaya eklemeyin. Hazırlık komutu yalnız NULL alanları doldurur.
ALTER TABLE app_users ADD (password_hash VARCHAR2(100 CHAR));
