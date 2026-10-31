# Yol haritası ve tamamlanma ölçütleri
1. React ve gerekli JavaScript: JSX, component, props ve liste ile hesaplar;
   build/lint başarılı; kullanıcı küçük değişiklik yapıp veri akışını açıklayabilir.
2. Oracle/SQL: müşteri, hesap, hareket tabloları ve örnek sorgular doğrulanır.
3. Gerekli Java ve Spring Boot: REST API demo hesapları döndürür.
4. Entegrasyon: React API verisini gösterir; transferde bakiye ve hareketler
   tek veritabanı transaction'ında yazılır.
5. Güvenlik ve testler: CUSTOMER yalnızca kendi hesaplarını kullanır;
   ADMIN rolü ve kritik senaryolar test edilir.
6. RabbitMQ/ayrı Spring Boot worker: asenkron demo dekont/ekstre hazırlanır.
7. Güvenilirlik: idempotency, eşzamanlılık, retry, DLQ ve outbox doğrulanır.
8. Mülakat provası: kullanıcı mimariyi ve tercihlerini kendi sözleriyle anlatır.

Her aşama yalnızca kullanıcı görevlendirdiğinde açılır.

Ayrıntılı 1.1, 1.2... öğrenme adımları: docs/LEARNING_PLAN.md.
Bu dosyadaki 8 ana faz ile ayrıntılı plandaki 17 bölüm farklı ayrıntı düzeyleridir.
