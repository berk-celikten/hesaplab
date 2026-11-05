# Mevcut durum
## Konum
- README16.3: Başlığa kısa İngilizce proje özeti eklendi; Türkçe içerik korundu. GitHub metni ve README diff'i doğrulandı; yalnız belge değişti, test/build/migration çalıştırılmadı. Öğrenme alt adımı16.3 sürüyor.
- README16.3: HesapLab tanıtımı yeniden düzenlendi;9 galeri ekranı/tam admin görünümü,hero,mimari ve6 tablo ER eklendi. Eski README docs/DEVELOPMENT_HISTORY.md içinde korundu;RUNBOOK bağlantısı eklendi. Görsel/link/diff/sır kontrolü yapıldı;58 kaynak dosyası ve6 tablo tüm-sütun parmak izi aynı. Mevcut servisler açıldı;test/build/migration tekrarlanmadı.
- Güncel16.3: Berk önceki iki PDF yerine görsellerle mülakat çalışması istedi. Repo dışı HesapLab-Gorsel-Mulakat-Provalari.pdf hazır:51 yatay sayfa,48 açıklamalı çizim/ana soru ve96 takip sorusu;cevap boşluğu ve hemen altında örnek cevap.58 kaynak dosyası hash eşleşti;PDF metin/yerleşim ve bütün sayfaların görsel kontrolü geçti. Uygulama/test/build/DB/migration/push yok;öğrenme teyidi yok.
- Öğretim tercihi: Görseli ana anlatım yap;oklar,rol ve hata sonucu kendi başına anlaşılabilsin. Terimleri temelden tanımla,tek transfer örneğini izle. MVC ile Controller-Service-Repository ayrımını koru;Model=Oracle diye eşleme. PDF okuma veya tek tur çalışmayla genel mülakat yeterliği garantisi verme.
- Ders dönüşü16.3: ReceiptProcessor31-55/READY kaydı gösterildi;başarılı transferde dekont yazma hatası senaryosu henüz yanıtlanmadı. Tek worker kuyruk örneği doğruydu;fetch/request/varsayılan GET ve bağımsız kod yeterliği pekiştirilecek.
- 15.10 açıklama yeterliği ve16 öğrenme/mülakat hedefleri sürüyor; çalışan kod öğrenme teyidi değildir.
## Uygulama
- React/Vite,Java21 backend/relay ve ayrı worker; Oracle/RabbitMQ Docker'da.
- Oturum/CSRF/bcrypt,CUSTOMER sahipliği,sınırlı ADMIN yönetimi,atomik transfer,idempotency,hareket ve metin dekontu mevcut.
- Retry/DLQ ve transactional outbox var; SENT broker onayı,READY worker sonucudur. Tekrar teslim mümkündür.
- Ekstre/PDF/e-posta,silme/pasifleştirme/hesap kapatma,tam yönetim kapsam dışıdır.
## Önceki işletim kontrolleri
- Geliştirici: kimliği doğrulanmış üç uygulama PID'si ve iki container ile kapatma/açma; altı tablo bütün sütun fingerprint'i aynı,eski9 kayıt korundu.
- PID sonlandırması graceful Ctrl+C değildir; Docker Desktop/bilgisayar yeniden başlatılmadı. Ayrıntı docs/RUNBOOK.md.
- Yönetici: RUNBOOK/ilgili diff okundu; yalnız belge değişti. İki küçük komut açıklaması netleştirildi.
- Canlı HTTP/proxy girişinde müşteri01 yalnız2,müşteri02 yalnız1 kendi hesabını ve doğru bakiyeleri gördü; özel smoke oturumları kapandı.
- Yönetici salt-okuma smoke öncesi/sonrası altı tabloda eski9 kaydın bütün sütun fingerprint'ini doğruladı.
- Oracle/broker healthy; receipt/demo consumer1,DLQ consumer0; kuyruklar ready0/unacked0,frontend5181/backend8081 açık.
- Bu incelemede süreç yeniden başlatma,test/build/migration veya görsel UI tekrarlanmadı; diff/sır kontrolü geçti.
- Güncel öğrenme verisi API ile doğrulandı: transfer264,25.00TRY,hesap1→2;bakiyeler9975.00/3525.00,DEBIT hareket1674,balanceAfter9975.00,dekontREADY. Özel okuma oturumu kapandı.
- Bunlar Berk'in kullanıcı verisidir; test kaydı diye silme veya seed bakiyesine döndürme. Eski9 kayıt/10000-3500 varsayımı artık güncel değildir; sonraki kontroller güncel snapshot almalı.
## Önceki kanıtlar
- 15.1–15.8 yönetici:37 backend+14 worker testi/package;30 gerçek Oracle,rollback/replay/paralellik/DLQ/outbox ve canlı otomatik toparlanma.
- PENDING işli gerçek backend restart geliştirici;broker kapalı HTTP200→SENT/READY yönetici kanıtıdır. Confirm anında process kill yok.
- Önceki UI/Tab-Enter/390px ve diğer kontrol kaynakları README/LEARNING_PLAN içinde korunur.
## Öğrenme açıkları
- 16.1 dar örneklerde Berk: Spring Boot yetki kontrolü,rollback ile100TL'ye dönüş,'ya hep ya hiç',başarısız transferde outbox kalmaması ve aynı isteği A kimliğiyle tekrar doğruydu. Bağımsız/genel yetkinlik değildir.
- State/yenileme,ad-value/URL kimliği,veri kopyası,component sahipliği,effect/cleanup/StrictMode pekiştirilecek.
- Java/OOP,Oracle transaction,güvenlik,asenkron/dağıtık çalışma ve bağımsız kod yazımı doğrulanmış sayılmaz.
## Yayın ve sonraki iş
- Uygulama kodu son yayını60808e0/ecbb6d9;görselli README/RUNBOOK/geliştirme arşivi ve görseller GitHub belge yayınıdır. LEARNING_PLAN önceki yerel değişiklikleri bu yayın dışında korunur.
- 16.2: Berk bakiye düşüşünü ve asenkron worker rolünü söyledi; genel yeterlik doğrulanmadı. Sonraki16.3 başarılı transfer/başarısız dekont ayrımı ve dekontun React ekranına gelişi; yeni geliştirme yok.
