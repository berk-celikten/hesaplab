# README görselleri

- JPG dosyaları 5 Kasım 2026 tarihinde çalışan yerel HesapLab uygulamasından alındı. Mevcut demo müşteri ve yönetici oturumları kullanıldı; yeni transfer/kullanıcı/hesap veya ad değişikliği yapılmadı.
- login, accounts, account-details, transfer, recipient, receipt, admin/admin-accounts ve not-found gerçek ekran görüntüleridir. admin-full, yönetim sayfasının tamamıdır; diğer iki yönetim görseli aynı sayfanın üst ve alt bölümleridir.
- Ekran görüntüleri tarayıcının mevcut dar görünümünde alındı. Galerideki görseller tam boy dosyaya bağlanır.
- hero ve architecture açıklayıcı çizimlerdir. oracle-er altı tablonun SQL dosyalarına dayanan şema çizimidir; canlı tablo ekran görüntüsü değildir. Tablo/sütun adları canlı Oracle metadata sorgusuyla karşılaştırıldı.
- Yalnız demo veriler görünür; parola, parola hash değeri, cookie, CSRF değeri veya yerel gizli ayarlar içermez.

Şema kaynağı: db/oracle/01_schema.sql, 06_account_movements_schema.sql, 12_user_password_hash.sql, 13_transfer_idempotency.sql, 14_transfer_receipts.sql, 15_receipt_outbox.sql.
