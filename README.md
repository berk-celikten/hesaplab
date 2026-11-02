# HesapLab
Bankacılık üzerinden yazılım geliştirmeyi öğrenmek için eğitim projesi.
Gerçek para, gerçek müşteri bilgisi veya banka bağlantısı içermez.

## Mevcut özellikler
- Kullanıcı adı/parola ile giriş, HttpOnly/SameSite=Lax oturum çerezi ve çıkış; yenilemede mevcut oturum kontrol edilir.
- CUSTOMER yalnız kendi hesaplarını görür: müşteri 01 için 10.000,00 / 3.500,00 TL, müşteri 02 için 2.000,00 TL. Müşteri seçici kaldırıldı.
- ADMIN ayrı başlangıç ekranına gider; müşteri hesap API'sini veya transfer ekranını kullanamaz. Yönetim: listeler, hesap açma, CUSTOMER oluşturma ve ad düzenleme; silme/rol-parola değiştirme/hesap kapatma yoktur.
- Hesap listesi ve ayrıntı Oracle'dan, Spring Boot üzerinden alınır. Sahiplik oturumdan belirlenir ve SQL'de filtrelenir; istemci customerId parametresine güvenilmez.
- Arama, bakiye gizliliği, yükleme, eski veriyi açıklayarak koruma ve yeniden deneme sürer; sessiz mock fallback yoktur.
- Transfer taslağı ayrı onayla Oracle üzerinde gerçek işleme dönüştürülebilir. Kaynak kendi hesabıdır; hedef kendi veya başka CUSTOMER hesabı olabilir, kaynakla aynı olamaz. Ham tutar ve boşluk/biçim/pozitiflik kontrolleri korunur; parasal metin JavaScript Number dönüşümünden geçirilmez.
- Alan düzenlemesi hata/taslağı temizler; menü geçişi, arama ve gizlilik formu korur. Çıkış veya bilinen 401 tüm kullanıcı ekran/state'ini kaldırır; geç kalan önceki yanıt kullanılmaz.
- Kendi ve başka CUSTOMER hesapları arasında transfer ve bakiye/hareket yazımı vardır; RabbitMQ/ayrı worker ile gönderen müşteriye metin dekontu hazırlanır; sentetik mesaj demosu da korunur. Tam yönetim henüz yoktur. Bu yerel eğitim uygulamasının güvenlik kavramları ayrıca çalışılacaktır.

## Sayfalar ve gezinme
- React Router 8.5.0, BrowserRouter/Routes/Route; giriş gerektiren sayfalar oturum kontrolünden sonra açılır.
- `/hesaplar`: hesap sayısı, arama ve kartlar. `/hesaplar/:accountId`: useParams kimliğiyle backend'den yalnız kendi hesabının ayrıntısı.
- Başkasının, bulunmayan veya geçersiz hesabın kimliği aynı 404 mesajını verir. URL kullanıcıyı değiştirmez; arama ayrıntı erişimini etkilemez.
- Oracle kimlikleri metindir; checking/savings/checking-02 gibi eski adresler eşleşmez. Yenileme mevcut oturumu korur; arama/form/gizlilik varsayılanına döner.
- `/transfer`: taslak ve ayrı gerçek transfer onayı. `/admin`: yönetici listeleri ve hesap açma formu. ADMIN müşteri yollarından `/admin` adresine yönlenir.
- CUSTOMER için `/`, replace ile `/hesaplar` olur; bilinmeyen yol dönüş bağlantılı 404 gösterir.
- App oturumun sahibidir; CustomerApp müşteri hesap/tercih/form state'ini tutar. Logout ile unmount, yeni kullanıcıda yeni state oluşmasını sağlar. Sayfalar props alır.
- Link/NavLink tam yenileme yapmaz; doğrudan adresleri yerel Vite sunucusu destekler. Statik yayında SPA yönlendirmesi gerekir.

## Çalıştırma
Node 24.19.0 ve npm 11.17.0 ile kurulmuştur.
Resmî Vite gereksinimi: https://vite.dev/guide/ (Node 20.19+, 22.12+).
Repo kökünden:
```sh
cd frontend
npm ci
npm run dev
```
Terminalde gösterilen yerel adresi açın.

## Asenkron JavaScript alıştırması (5.4–5.5)
`frontend` klasöründe `npm run demo` çalıştırın.
`exercises/async-demo.js`, gecikmeli bir sonuç beklenirken çağıran kodun ilerlediğini gösterir.
Beklenen sıra: 1 ve 2 hemen, 3 yaklaşık üç saniye sonra. Zamanlayıcının tam üç saniyede çalışması garanti değildir.
Bu başarı akışı örneği React'e bağlı değildir; ayrı thread, servis veya API çalıştırmaz.

### İkinci örnek: sıfır gecikmeli zamanlayıcı
`frontend` klasöründe `npm run demo:loop` çalıştırın.
`exercises/event-loop-demo.js` çıktısı 1 → 2 → 3 olur: 0ms zamanlayıcının fonksiyonu mevcut senkron koddan sonra çalışır.
Bu örnek ikinci bir JavaScript iş parçacığı açmaz; sıfır gecikme tam zamanlama garantisi değildir.
İlk örnek ve `npm run demo` komutu korunur.

### Üçüncü örnek: asenkron hata
`frontend` klasöründe `npm run demo:error` çalıştırın. Mevcut `demo` ve `demo:loop` komutları korunur.
Promise yaklaşık bir saniye sonra Error ile reddedilir; sıra 1 → 2 → hata mesajı → finally mesajıdır. Başarı mesajı yazılmaz.
Kesin süre garantisi yoktur. Çıkış kodu 0, canlandırılan işlemin başarılı olduğunu değil, hatanın catch ile karşılandığını gösterir.

## Kontroller
frontend klasöründe:
```sh
npm run build
npm run lint
```
Build önizlemesi: `npm run preview`.
package-lock.json sürümlenir; bağımlılıklar ve derleme çıktıları sürümlenmez.

Hesap yükleme Effect cleanup işlemi yerel ignore bayrağını değiştirir; eski çalışmanın sonuç ve finally state güncellemeleri atlanır. Bu, zamanlayıcıyı/isteği iptal etmez. StrictMode korunur; geliştirmede ek setup → cleanup → setup döngüsü olabilir. Normal gezinme ilk yüklemeyi yeniden başlatmaz.

5.7.1 davranışı korundu: “Demo hatasını dene (simülasyon)” açık istemci hata simülasyonudur; “Tekrar dene” ve normal yenileme gerçek API'yi çağırır. Hata erişilebilir mesajla gösterilir. Önceki veri varsa korunup eski veri olduğu açıklanır; veri yoksa ayrıntı/form/boş sonuç yerine hata gösterilir. Yeni denemede hata temizlenir; istek sürerken yükleme butonları devre dışıdır. İlk yükleme normalde başarılıdır.

## Oracle veri modeli ve yerel çalışma ortamı (6.1–6.7)
- `db/oracle/01_schema.sql`: Oracle 19c için kullanıcı/hesap tabloları, identity kimlikler ve temel kısıtlar.
- `db/oracle/02_seed.sql`: iki demo müşteri, hesapsız demo admin ve 10000.00 / 3500.00 / 2000.00 TRY hesapları; sahipler benzersiz kullanıcı adıyla bulunur.
- Model ve Mermaid ilişki diyagramı: [DATABASE_MODEL.md](docs/DATABASE_MODEL.md).
- Şema ve seed yerel Oracle AI Database 26ai Free 23.26.3.0.0 üzerinde SQL*Plus ile çalıştırıldı; üç kullanıcı ve üç hesap sorgulandı. Oracle 19c sunucusunda ayrıca denenmedi. İlk kurulum dosyaları boş şema için tek çalıştırımlıktır.
- `db/oracle/compose.yaml`: HesapLab'a özel konteyner/veri alanı; bağlantı `127.0.0.1:1522/FREEPDB1`, kullanıcı `HESAPLAB`. Parolalar Git dışında kalan `.env.local` dosyasındadır. Başlatma/durdurma ayrıntıları model belgesinde.
- `db/oracle/03_read_queries.sql`: kullanıcı ve hesap kayıtlarını gösteren salt okunur sorgular.
- Grafik arayüzü: VS Code içindeki resmî Oracle SQL Developer 26.3.0; `Connections → HesapLab → Tables → APP_USERS/ACCOUNTS → Data`. ER için [hesaplab.dgm](docs/diagrams/hesaplab.dgm) dosyasını açın; gerekirse `Fit to Screen` kullanın. Önceki iki tablo/FK görünümü doğrulandı; 7.2 ile native dosyaya üçüncü tablo eklendi. Eski PNG yalnız kullanıcı/hesap modelini gösterir. Model belgesinde ayrıca statik ER resmi bulunur.
- Şema/seed dosyaları manuel hazırlık içindir; uygulama açılışında çalıştırılmaz. React veriyi aşağıda açıklanan Spring Boot API üzerinden alır. Hazırlık, konuların öğrenildiği anlamına gelmez.

## İlk SELECT alıştırmaları (6.8.1)
`db/oracle/04_select_exercises.sql`, tek hazır SELECT ve Berk'in yazacağı üç yorum/TODO içerir; çözümler eklenmedi.
Mevcut VS Code Oracle SQL Developer ve HesapLab bağlantısında şu adımlar doğrulandı:

1. VS Code'da **File → Open File** ile `db/oracle/04_select_exercises.sql` dosyasını açın.
2. Üst araç çubuğundaki **Run Script (F5)** düğmesine basın.
3. Bağlantı seçimi açılırsa **HesapLab — HESAPLAB@//127.0.0.1:1522/FREEPDB1** seçin.
4. **Script Output** panelinde id sırasıyla üç hesabı ve 10000 / 3500 / 2000 bakiyelerini inceleyin.
Tek sorgunun sonucunu tablo halinde görmek için imleci o sorgunun `SELECT` satırına getirin ve **Run Statement (Ctrl+Enter)** kullanın. Sonuç **Query Result** panelinde görünür; bu yöntem hazır sorguda ayrıca doğrulandı.
Dosya bütünüyle çalıştırıldığında yalnız hazır örnek yürütülür; TODO satırları yorumdur.
Şema/seed dosyalarını yeniden çalıştırmayın. Alıştırmaların hazırlanması öğrenmenin doğrulandığı anlamına gelmez.

## JOIN örnekleri (7.1)
`db/oracle/05_join_examples.sql` iki salt okunur örnek içerir: hesaplardan başlayan INNER JOIN ve bütün kullanıcıları koruyan LEFT JOIN.
Dosyayı VS Code'da açıp **Run Script (F5)** ile mevcut **HesapLab** bağlantısında çalıştırın; sonuçlar **Script Output** panelindedir.
Bu bağlantıda INNER JOIN 3, LEFT JOIN 4 satırla doğrulandı: müşteri 01'in iki, müşteri 02'nin bir hesabı ve LEFT JOIN'de hesap alanları NULL olan admin.
Bakiyeler 10000 / 3500 / 2000 TRY olarak korundu. Sorgular veri değiştirmez; dosyanın hazırlanması JOIN'in öğrenildiği anlamına gelmez.

## Hesap hareketleri (7.2)
- `db/oracle/06_account_movements_schema.sql`: yalnız yeni `account_movements` tablosu; mevcut Oracle'da uygulandı, yeniden çalıştırmayın.
- `db/oracle/07_account_movements_seed.sql`: hareketi olmayan pozitif bakiyeli hesaplara canlandırılmış açılış CREDIT kaydı ekler; mevcut bakiyeleri değiştirmez. Ardışık tekrarda kayıt çoğalmaz; sıfır bakiyeli hesap atlanır.
- `db/oracle/08_account_movements_queries.sql`: hareket/hesap/sahip ve güncel bakiyeyi hesap-zaman-kimlik sırasıyla gösterir; mevcut HesapLab bağlantısında Run Script ile çalıştırılabilir.
- `accounts.balance` güncel, `balance_after` hareket sonrasındaki geçmiş bakiyedir; CREDIT giriş, DEBIT çıkış, amount her zaman pozitiftir. Uygulamada gerçek transfer yoktur; rollback ile biten SQL transaction alıştırması aşağıdadır.
- SQL*Plus kontrolü: 3 açılış kaydı, doğru sahiplik ve 10000/3500/2000; seed tekrarında 0 ek kayıt. Geçersiz hesap ORA-02291, yön/sıfır-negatif tutar/negatif balance_after ORA-02290; denemeler rollback ile temizlendi, toplam 3 kaldı.
- [Model](docs/DATABASE_MODEL.md) ve native `docs/diagrams/hesaplab.dgm` üç tabloyu içerir. Eski `hesaplab-er.png` yalnız iki tablolu önceki modeldir. Yönetici kaydedilmemiş asıl diyagramı koruyarak ayrı inceleme kopyasında üç tabloyu ve iki foreign key çizgisini görsel doğruladı; unsaved kullanıcı sorgusu da korundu.

## Transaction ve rollback alıştırması (7.3–7.5)
`db/oracle/09_transfer_transaction_demo.sql`, müşteri 01'in Vadesiz hesabından müşteri 02'nin Vadesiz hesabına 200 TRY aktarımını geçici olarak canlandırır. Hesaplar kullanıcı adı/hesap adıyla bulunur; varlık, farklılık ve yeterli bakiye kontrol edilir. Demo kullanıcı seçimi gerçek kimlik doğrulama değildir.

Proje kökünde PowerShell'den, mevcut konteynerde **yeni ve ayrı SQL*Plus bağlantısı** açarak çalıştırın:

```powershell
$demoSql = Get-Content -Raw db/oracle/09_transfer_transaction_demo.sql
($demoSql + "`nEXIT ROLLBACK`n") | docker exec -i -e NLS_LANG=.AL32UTF8 hesaplab-oracle bash -c 'sqlplus -s "$APP_USER/$APP_USER_PASSWORD@//localhost:1521/FREEPDB1"'
```

Parola konteynerin mevcut ortamından alınır; komut çıktısına yazılmaz. Açık kullanıcı worksheet'inde bekleyen işlem varken çalıştırmayın; bloğu parça parça çalıştırmak yerine dosyanın tamamını kullanın.
Başarı akışı iki UPDATE ve iki INSERT'i aynı transaction'da yürütür; geri almadan önce aynı oturumda yeni bakiyeler ve bu çalıştırmanın iki hareketi gösterilir. Ardından ROLLBACK uygulanır.
Ayrı hata senaryosu kaynak düşüldükten sonra kontrollü ORA-20010 üretir; handler kaynak hâlâ düşükken değeri gösterir ve bütün demo değişikliklerini açıkça geri alır. Tek SQL ifadesinin hata vermesi, önceki bütün başarılı ifadelerin otomatik geri alınması demek değildir.
COMMIT değişiklikleri kalıcılaştırır; bu alıştırmada çalıştırılmaz. Identity sayacı rollback ile geri sarılmaz; hareket kimliklerini sabit beklemeyin. Eşzamanlılık/kilitleme ve gerçek transfer uygulaması kapsam dışıdır.

Geliştirici mevcut Oracle'da ayrı SQL*Plus oturumunda doğruladı (çıkış 0): önce kaynak/hedef 10000/2000 ve 3 hareket; geçici 9800/2200 ve 2 yeni hareket (DEBIT/CREDIT, amount 200, balance_after 9800/2200); başarı rollback'i sonrası 10000/2000 ve 3 hareket. Hata öncesi kaynak 9800, hata rollback'i sonrası 10000/2000 ve 3 hareket. Başlangıç/son sorgularında Birikim 3500 ve mevcut hareketlerin bütün sütunları korundu. Hazırlık ve geliştirici kontrolü, Berk'in transaction/rollback'i öğrendiği anlamına gelmez.
Kaynak: [Oracle 19c — ROLLBACK](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/ROLLBACK.html), [statement-level rollback](https://docs.oracle.com/en/database/oracle/oracle-database/19/zzpre/defining-controlling-transactions.html).

## İki oturumla satır kilidi (7.6–7.7)
Kullanıcının açık worksheet'lerini kullanmayın. İki sekme, iki Oracle oturumu demek değildir; çıktılardaki SID ve SESSIONID değerlerini karşılaştırın. Bu örnek veri değiştirmez, yalnız `accounts` içindeki hedef satırı kilitler; kullanıcı alt sorgusunda kilit alınmaz.

Proje kökünde bir kez hazırlayın (kopyalar konteynerin geçici dizinindedir):

```powershell
Get-Content -Raw db/oracle/10_lock_session_a.sql | docker exec -i hesaplab-oracle bash -c 'cat > /tmp/hesaplab-lock-a.sql'
Get-Content -Raw db/oracle/11_lock_session_b.sql | docker exec -i hesaplab-oracle bash -c 'cat > /tmp/hesaplab-lock-b.sql'
```

**İki ayrı terminalde** aşağıdaki komutu ayrı ayrı çalıştırın; her çağrı yeni SQL*Plus/Oracle bağlantısı açar. Parola mevcut konteyner ortamından alınır, ekrana yazılmaz.

```powershell
docker exec -it -e NLS_LANG=.AL32UTF8 hesaplab-oracle bash -c 'exec sqlplus -s "$APP_USER/$APP_USER_PASSWORD@//localhost:1521/FREEPDB1"'
```

1. A terminalinde `@/tmp/hesaplab-lock-a.sql` çalıştırın. `A_LOCKED` ve bakiye görünür; A, PAUSE üzerinde bekler. **Henüz Enter'a basmayın.** Autocommit kapalıdır; dosya sonuna gelinip bağlantı kapanmadığı için kilit tutulur.
2. B terminalinde `@/tmp/hesaplab-lock-b.sql` çalıştırın. SID/SESSIONID'nin A'dan farklı olduğunu doğrulayın. `B_NORMAL_SELECT` hemen sonuç verir; ardından `FOR UPDATE WAIT 5` kilit bekler.
3. A'ya dokunmadan bekleyin. B yaklaşık 5 saniyede `B_EXPECTED_TIMEOUT` gösterir. Yerel Oracle 26ai, ORA-00054 ve altında ORA-40097 döndürdü; ORA-30006 dönüşü de açıkça karşılanır. Diğer hatalar yeniden yükseltilir. B sonunda ROLLBACK yapar.
4. B'de aynı dosyayı tekrar çalıştırın. `B_WAIT_START` görülünce, süre dolmadan **A terminalinde Enter'a basın**. A dosyasındaki açık `ROLLBACK;` aynı A bağlantısında çalışır ve `A_RELEASED` görünür. B kilidi alır, `B_LOCKED` gösterir ve kendi ROLLBACK'iyle bırakır.
5. İki bağlantıyı da `EXIT ROLLBACK` ile kapatın. Alıştırmadan erken çıkarsanız A'nın PAUSE'unda önce Enter'a basıp ROLLBACK'i tamamlatın; ardından yalnız demo bağlantılarını kapatın. Başka kullanıcı oturumlarını sonlandırmayın.
6. İsteğe bağlı geçici kopya temizliği: `docker exec hesaplab-oracle rm -f /tmp/hesaplab-lock-a.sql /tmp/hesaplab-lock-b.sql`.

Normal SELECT kilit istemeden tutarlı veriyi okur. SELECT FOR UPDATE satırı sonraki değişiklik için kilitler; aynı satıra kilit isteyen veya o satırı değiştirmek isteyen başka işlem bekleyebilir. Bu satır kilidi bütün okumaları veya diğer hesaplara erişimi durdurmaz. ROLLBACK kilidi bırakır; bekleme süresi tam zamanlama garantisi değildir. Kaynak: [Oracle SELECT](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html), [SQL*Plus PAUSE](https://docs.oracle.com/en/database/oracle/oracle-database/26/sqpug/PAUSE.html).

Geliştirici kontrolü: A SID=162/SESSIONID=190057, B SID=57/SESSIONID=190058. Normal SELECT bakiye=10000; ilk bekleme sunucu ölçümünde 5s, dış ölçümde 5,015s ve ORA-00054/ORA-40097. İkinci denemede A rollback'iyle B sunucu ölçümünde 1,52s, dış ölçümde 1,526s içinde kilidi aldı ve bıraktı. A_TX/B_TX=NONE; iki oturum çıkış 0 ile kapandı, geçici kopyalar temizlendi. Üç hesap ve üç hareketin tüm sütunları önce/sonra aynı; bakiyeler 10000/3500/2000. İlk deneme yalnız ORA-30006 beklediğinden sürüm farkıyla hata verdi; oturumlar kapatıldı, geçici kopyalar temizlendi ve B'nin açık hata yönetimi düzeltildikten sonra kontrol tekrarlandı. Öğrenme doğrulanmış sayılmaz.

## React → Spring Boot → Oracle (9.1–9.6 / 10.1)
Backend `backend/` içinde Java 21 hedefler. Sabit sürümler: Spring Boot **3.5.16**, Oracle **ojdbc17 23.26.4.0.0**, Maven **3.9.11** / resmî Wrapper **3.3.4**. Mevcut Microsoft OpenJDK 21.0.8 kullanıldı; yeni JDK kurulmadı ve kalıcı JAVA_HOME/PATH değiştirilmedi.
Kaynaklar: [Spring Boot kararlı sürümler](https://docs.spring.io/spring-boot/), [Oracle JDBC / Java uyumu](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/JDBC-getting-started.html), [Oracle JDBC artefaktı](https://repo.maven.apache.org/maven2/com/oracle/database/jdbc/ojdbc17/23.26.4.0.0/ojdbc17-23.26.4.0.0.pom), [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/).

### Başlatma
Mevcut `hesaplab-oracle` 127.0.0.1:1522/FREEPDB1 üzerinde çalışmalı. Git dışındaki mevcut `db/oracle/.env.local`, APP_USER ve APP_USER_PASSWORD anahtarlarını içermeli; parola ekrana veya komut argümanına yazılmaz. Spring, dosyayı properties ipucuyla içe alır; ayrıca bir dotenv bağımlılığı yoktur. Şema/seed tekrar çalıştırmayın: `spring.sql.init.mode=never`, JPA ve otomatik tablo oluşturma yoktur.

Bir PowerShell terminalinde proje kökünden:

```powershell
cd backend
$env:JAVA_HOME = 'C:/Program Files/Android/openjdk/jdk-21.0.8'
.\mvnw.cmd test package
.\mvnw.cmd spring-boot:run
```

JAVA_HOME yalnız bu terminal oturumu içindir; başka bilgisayarda kurulu JDK 21 yolunu kullanın. Sistem Maven kurulumu gerekmez. İsteğe bağlı yardımcı: proje kökünden `.\backend\start.ps1 -JavaHome 'C:/Program Files/Android/openjdk/jdk-21.0.8'`; portun boşluğunu denetler. Güven/çalıştırma ilkelerini değiştirmeyin; script çalıştırılamıyorsa yukarıdaki doğrudan Wrapper komutlarını kullanın.
Backend yalnız **http://127.0.0.1:8081** üzerinde dinler. 8081 doluysa başka uygulamayı kapatmayın; ayrı terminalde `$env:HESAPLAB_PORT='8082'` gibi boş bir port seçin veya yardımcıya `-Port 8082` verin.

İkinci terminalde proje kökünden:

```powershell
cd frontend
npm ci
npm run dev -- --host 127.0.0.1 --port 5181 --strictPort
```

Arayüz: **http://127.0.0.1:5181/hesaplar**. 5181 doluysa mevcut HesapLab sunucusunu kullanın veya başka boş port seçin; başka uygulamayı kapatmayın. Backend farklı portta çalışıyorsa Vite başlamadan `$env:HESAPLAB_API_TARGET='http://127.0.0.1:8082'` ayarlayın. Vite `/api` isteklerini backend'e yönlendirir; geniş CORS izni açılmaz. `npm run preview`, varsayılan olarak server.proxy ayarını devralır; bu görevde uçtan uca doğrulama geliştirme sunucusunda yapıldı. Kaynak: [Vite preview.proxy](https://vite.dev/config/preview-options.html#preview-proxy).

### İstek ve veri akışı
React fetch → Vite `/api` proxy → Spring Security (oturum/rol/CSRF) → AccountController → AccountService (readOnly) → AccountRepository/JdbcTemplate → Oracle.
`GET /api/accounts` ve `GET /api/accounts/{id}` doğrulanmış CUSTOMER kimliğini kullanır; SQL'de owner_user_id koşulu vardır. Eski `/api/demo/customers` ve `/api/demo/accounts` bütün kullanıcılara kapalıdır.
Kimlikler JSON metni, bakiye BigDecimal'dan iki ondalık basamaklı metindir; React yalnız gösterimde Türkçe biçimlendirir. Veritabanı hatası ayrıntısız 503'tür. Oturumsuz hesap isteği 401, ADMIN 403; başkasının/bulunmayan hesabı 404'tür. API HTML giriş sayfasına yönlendirmez.
`GET /api/auth/me` yalnız id/username/name/role döndürür. Login/logout POST öncesi `/api/auth/csrf` alınır; başarıdan sonra tekrar alınarak yeni oturum tokenı oluşturulur. Token/parola kalıcı tarayıcı deposuna yazılmaz.

### Önceki salt okunur entegrasyon kontrolleri (tarihsel kayıt)
- Maven test/package: 4 test, 0 hata/başarısızlık; metin kimlik, balance JSON ve iki uçta ayrıntısız 503 kontrol edildi. Testler Oracle'a yazmaz; JDK 21 altında Mockito'nun agent uyarısı testleri engellemedi.
- Gerçek API/proxy: 2 CUSTOMER, 3 hesap; Oracle başlangıç kayıtlarıyla id/ad/sahiplik/tutar/TRY karşılaştırıldı, ADMIN listede yok. Müşteri 01: 10000.00/3500.00; müşteri 02: 2000.00.
- Tarayıcı: müşteri değişimi, BİRİKİM araması/boş sonuç, gizlilik, Oracle kimlikli ayrıntı ve müşteri sahipliği görünümü, ham ` 125,50 ` ile Enter taslağı, aynı müşteri koruması/farklı müşteri temizliği ve hata simülasyonu geçti. Tab kaynak select'ten hedef select'e ilerledi; Enter ile form doğrulaması çalıştı.
- Yalnız bu görevde başlatılan backend kapatılıp yeniden açıldı: ilk boş açılış hatası, eski veri/arama/gizlilik/taslak koruması ve iki durumda retry ile toparlanma doğrulandı. Oracle ve mevcut frontend durdurulmadı.
- Başlangıç/son Oracle snapshot'ında 3 kullanıcı/3 hesap/3 hareketin bütün sütunları aynı. Şema/seed/transaction/lock alıştırmaları tekrar çalıştırılmadı. Frontend build/lint ve diff geçti.
- Masaüstü 1280px görünümde yatay taşma yok. **390px mobil doğrulanamadı:** tarayıcı viewport ayarını uygulamadı; ayrı Windows tarayıcı kontrolü URL doğrulanamadığı için durduruldu. Güven ayarları değiştirilmedi; bu kontrol geçmiş sayılmaz.
- Backend/target, .tools, loglar ve mevcut env dosyası Git dışında kalır. Kodun hazırlanması Java/Spring Boot/entegrasyonun öğrenildiği anlamına gelmez; commit/push yapılmadı.
- Yönetici incelemesi: Java/React/ayarlar/testler ve ilgili diff incelendi; 4 Maven testi/package, frontend build/lint ve diff geçti. Canlı API ve Vite proxy cevapları doğrudan Oracle SELECT ile aynı (2 müşteri, 3 hesap; id/ad/sahiplik/tutar/TRY); kayıt sayıları 3 kullanıcı/3 hesap/3 hareket. Tarayıcıda eski ilk hatadan retry, müşteri 01/02, BİRİKİM araması, gizlilik, simülasyon sonrası tercih koruması/retry, ayrıntı ve ham tutarla Enter taslağı doğrulandı. 390px gerçek viewport (innerWidth=390, clientWidth=scrollWidth=375) uygulandı; ayrıntı ve formda yatay taşma yok, kaynakta Tab hedefe geçti. Görsel kontrol geçti; geçici viewport sıfırlandı, test değerleri temizlendi. Gerçek backend kapatma/açma ve tüm sütun snapshot kontrolü bu tur tekrarlanmadı, geliştiriciye aittir. İşlevsel bulgu yok; iki eski README açıklaması düzeltildi. Commit/push yapılmadı.
## Oturumlu giriş ve hesap sahipliği (11.1–11.4 / 11.8)
Mevcut Java/Boot/JDBC/Wrapper sürümleri korundu; Spring Security sürümünü Boot yönetir. Kaynaklar: [CSRF](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html), [oturum yönetimi](https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/session-management.html). Yerleşik formLogin, bcrypt ve session fixation koruması kullanılır. Çerez `HESAPLAB_SESSION`, HttpOnly, SameSite=Lax; yerel HTTP kullanılır. Bu bir üretim yayını değildir.

### Tek seferlik migration ve açık parola hazırlığı
1. Mevcut Oracle'da ayrı bağlantıyla USER_TAB_COLUMNS üzerinden APP_USERS.PASSWORD_HASH var mı inceleyin. Yoksa yalnız `db/oracle/12_user_password_hash.sql` çalıştırın. Bu projede uygulandı; tekrar çalıştırmayın. Eski şema/seed çalıştırılmaz.
2. Backend klasöründe JDK 21 yolunu yalnız terminal için ayarlayın ve uygulamayı paketleyin:
   ```powershell
   $env:JAVA_HOME = 'C:/Program Files/Android/openjdk/jdk-21.0.8'
   .\mvnw.cmd clean test package
   & "$env:JAVA_HOME/bin/java.exe" -jar target/hesaplab-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prepare-passwords
   ```
   Hazırlık ayrı bağlantıda yalnız üç mevcut demo kullanıcının NULL hash alanını doldurur, ardından kapanır. Normal başlangıç parola üretmez. Tekrarda mevcut hash/parolalar korunur; dosya kayıpsa otomatik sıfırlama yapılmaz.
3. Yerel demo kullanıcı adı/parolaları yalnız Git dışındaki `.tools/demo-logins.local.json` dosyasından okuyup giriş ekranında kullanın. Dosyayı paylaşmayın veya terminale dökmeyin. Oracle bağlantı parolası bu parolalardan ayrıdır; veritabanında yalnız bcrypt hash bulunur.
4. Normal başlangıç: backend içinde `.\mvnw.cmd spring-boot:run`; frontend içinde `npm run dev -- --host 127.0.0.1 --port 5181 --strictPort`. Mevcut sunucular çalışıyorsa ikinci kopya başlatmayın. Adresler: http://127.0.0.1:8081 ve http://127.0.0.1:5181.

### Bu görevin gerçek doğrulamaları
- Maven clean test/package: Security filtre zinciri dahil 10 test, 0 başarısızlık/hata. Frontend build/lint ve diff kontrolü geçti.
- Gerçek API: anonim 401; yanlış kullanıcı/parola aynı 401; CSRF olmadan login/logout 403; taze tokenla başarı. Girişte session kimliği değişti, eski CSRF reddedildi; logout sonrası eski oturum 401. Hassas değerler kayda yazılmadı.
- Her iki CUSTOMER yalnız kendi 2/1 hesabını aldı. İstemci kimlik parametresi sahipliği değiştirmedi; yabancı/bulunmayan/geçersiz ayrıntı aynı 404. ADMIN 403; eski toplu demo uçları herkes için kapalı.
- Tarayıcı: yanlış giriş, yenilemede oturum, arama/gizlilik/ayrıntı, kendi hesaplarıyla ham tutarlı Enter taslağı; aynı tarayıcıda çıkış→diğer müşteri, admin ayrımı ve 401 sonrası eski verinin kaldırılması geçti.
- Geçici gecikmeyle eski müşterinin başarılı yanıtı bekletildi; çıkış/yeni girişten sonra eski hesap görünmedi. Test kodu kaldırıldı. 390px görünüm ve Tab/ok/Enter kontrolü, clientWidth=scrollWidth ile taşmasız form/ayrıntı doğrulandı.
- Ayrı bağlantıda migration ve hazırlık uygulandı; ikinci hazırlık 0 güncelleme yaptı, hash'ler ve yerel dosya değişmedi. Son Oracle snapshot: eski kullanıcı alanları, üç hesabın ve üç hareketin bütün sütunları başlangıçla aynı (9 kayıt); bakiyeler 10000/3500/2000.
- Oracle/diğer uygulamalar durdurulmadı; yalnız HesapLab backend'i yeniden başlatılarak geçersiz oturum kontrol edildi. Backend/frontend çalışır bırakıldı. Terminal alıştırmaları bu görevde tekrar edilmedi. Commit/push yapılmadı; kod hazırlanması öğrenme kanıtı değildir.

11.1–11.4/11.8 yönetici incelemesi: kaynaklar/ayarlar/migration/testler ve ilgili diff okundu. Maven 10 test/package, frontend build/lint geçti. Gerçek API ayrı cookie oturumlarında 401/403/404, aynı yanlış giriş cevabı, CSRF, session değişimi, eski oturumun reddi, 2/1 sahiplik, ownerId parametresi ve kapalı demo uçlarını doğruladı. API öncesi/sonrası 3 kullanıcı (hash dahil)/3 hesap/3 hareket tüm sütunları aynı; Git adaylarında yerel parolalar yok. UI müşteri01→02→admin→01, oturumla yenileme, yabancı hesap404, buton/Enter ve 390px giriş/hesap ekranı-klavye geçti; viewport sıfırlandı, test oturumları kapandı. P2: admin çıkışından sonra müşteri girişi /admin üzerinde 404 veriyordu; App.jsx başarılı login için role göre başlangıç adresine yönlendirilerek düzeltildi, aynı senaryo ve build/lint yeniden geçti. İlk genel giriş hatası temiz yüklemeden sonra tekrarlanmadı; nedeni doğrulanmadı. Gecikmiş yanıt ve parola hazırlığı tekrarı bu tur tekrarlanmadı, geliştirici kanıtıdır. Java/Security öğrenimi doğrulanmadı; commit/push yok.
## Kendi hesapları arasında transfer (10.3–10.6 / 11.5)
- Önce “Taslağı göster”, sonra ayrı “Transferi gerçekleştir”. Yalnız CUSTOMER; iki hesap da doğrulanmış oturum sahibinin olmalı. ADMIN yasak; CSRF zorunlu. Eski kontrol bölümleri tarihsel kayıttır.
- POST `/api/transfers`: `{"sourceAccountId":"<Oracle id>","targetAccountId":"<Oracle id>","amount":"25.50"}`. amount JSON metnidir; noktalı pozitif sayı, en fazla iki ondalık ve 13 tam basamak. React ham virgüllü metni trim/replace ile gönderir; Number dönüşümü veya yuvarlama yoktur.
- Java sayısal kimlikleri karşılaştırır (başına sıfır eklemek aynı hesap kuralını aşmaz). Sahiplik SQL koşuludur. Hesaplar sayısal id sırasıyla FOR UPDATE WAIT 5 kilitlenir; bakiye kilitten sonra kontrol edilir. TRY ve NUMBER(15,2) sınırları denetlenir. İki UPDATE + iki pozitif amount DEBIT/CREDIT aynı @Transactional işlemindedir; balance_after yeni bakiyedir.
- Hikari varsayılan bağlantısı yazmaya uygun, hesap okuma servisi readOnly kalır. Başlangıçta SQL/parola hazırlığı çalışmaz. Kilit süre aşımı 409, iş kuralı reddi 400; hesap yok/yabancı/geçersiz ortak mesajdır. Teknik ayrıntı istemciye verilmez.
- Gönderimde alanlar/tekrar gönderim engellenir, taslak tüketilir. Başarı sonrası hesaplar yenilenir; yenileme hatası transferin başarılı olduğundan ayrı açıklanır. Ağ/5xx durumunda sonuç kesin bilinmiyor mesajı verilir; otomatik transfer tekrarı yapılmaz. İdempotency henüz yoktur; tekrar göndermeden önce bakiyeleri kontrol edin.
- Eski GET sonuçları istek sürümüyle elenir; gönderimde ayrıntı GET'i bekletilir. Çıkışta CustomerApp bırakılır; geç gelen sonuç kullanılmaz. Hareket geçmişi aşağıdaki 10.7 adımında eklendi; başka müşteriye transfer/admin CRUD/RabbitMQ yoktur.

### Gerçek kontrol kaydı
17 Maven testi geçti: önceki 10 Security testi + gerçek Oracle kullanan 7 transfer testi. İzole kullanıcı/hesaplar oluşturulup finally/AfterEach ile temizlendi; her test sonunda eski kullanıcılar (hash dahil), hesaplar ve hareketlerin tüm sütunları karşılaştırıldı. Başarı 100/10 → 74.50/35.50, toplam110, tam iki hareket; geçersiz tutar/aynı-yabancı hesap/yetersiz bakiye/hedef taşması/401/403/CSRF ve sayısal JSON tutar reddi doğrulandı. İkinci INSERT'te test spy'ıyla gerçek Oracle CHECK hatası üretildi; iki bakiye ve ilk INSERT rollback oldu (üretimde hata anahtarı yok). Paralel 80+80 isteğinde yalnız biri tamamlandı, sonuç20/90 ve iki hareket. Ayrı bağlantının kilidinde yaklaşık5s sonra409 ve sıfır hareket doğrulandı.

Oracle testleri varsayılan çalışmada atlanır; mevcut HesapLab DB ve env dosyasıyla backend klasöründe açıkça çalıştırın:
```powershell
$env:HESAPLAB_ORACLE_TEST='true'
.\mvnw.cmd test
Remove-Item Env:HESAPLAB_ORACLE_TEST
```
Testler ayrı kayıtlarını commit eder ve temizler; eski şema/seed/migration çalıştırılmaz. Çalışan kullanıcı verileriyle manuel transfer testi yapmayın.
Tarayıcıda ayrı izole kullanıcıyla 25,50 transferi/başarı/yenilenen bakiyeler/aynı taslağın kaldırılması/Enter/Tab ve390px (clientWidth=scrollWidth375) geçti. UI kayıtları temizlendi, önce/sonra9 eski satır tüm sütunları aynı. Frontend build/lint ve diff geçti; backend paketlendi. Ağda yanıt kaybı, geç gelen GET ve başarılı transfer sonrası GET hatası bu tur tarayıcıda yapay olarak tetiklenmedi; korumaları koddan incelendi. Kod hazır olması öğrenme teyidi değildir; commit/push yok.

Yönetici incelemesi: Java/React/test/ayar/diff incelendi; 17 test/package (7 gerçek Oracle, 0 skip), frontend build/lint/diff geçti. Gönderim sırasında form yerine hesap yükleme mesajı gösteren route koşulu düzeltildi; form devre dışı ve Transfer işleniyor mesajı görünür kalır. Tarayıcıda ayrı iki geçici hesapla gerçek kilit/409 ve ardından 25,50 başarı, 74,50/35,50 bakiyeleri ve SQL iki hareket doğrulandı. Geçici kayıtlar temizlendi; eski 9 kaydın hash dahil tüm sütunları aynı. Normal iki hesap ekranı açık bırakıldı. Mobil/klavye geliştirici kontrolüne aittir; ağ sonucu kaybı/eski GET yarışı/başarı sonrası yenileme hatası yapay UI ile bu incelemede denenmedi. Öğrenme teyidi ve commit/push yok.
## Hesap hareketleri (10.7)
`GET /api/accounts/{accountId}/movements`: yalnız CUSTOMER, principal sahibine ait hesap. Geçersiz/yabancı/bulunmayan hesap aynı404; kendi hesabında geçmiş yoksa200 ve boş liste. SQL hem hesap hem owner_user_id koşulunu kullanır; created_at DESC, id DESC sırasıyla en yeni50 kayıt. DTO id/direction/amount/balanceAfter/description/createdAt içerir; id ve iki ondalıklı para metin, zaman offsetli ISO'dur. Okuma readOnly'dir.
Ayrıntı sayfası “Son 50 hareket” bölümünde Türkçe tarih, Giriş/Çıkış, tutar, işlem sonrası bakiye ve açıklamayı gösterir. Ortak gizlilik iki parasal değeri maskeler. Yüklenme/boş/hata/tekrar deneme ayrıdır; hesap-kullanıcı anahtarı ve Effect cleanup eski yanıtı bırakır,401 mevcut oturum temizliğini kullanır.
Kontroller:18 Maven testi (10 Security+8 izole gerçek Oracle),0 hata/skip; sahiplik/anonim/ADMIN/ortak404/boş geçmiş, transfer DEBIT-CREDIT25.50 ve74.50/35.50, offsetli zaman,55 aynı zamanlı kayıtta50 sınırı/id sırası geçti. Önceki transfer/rollback/paralellik/kilit testleri de geçti; test kayıtları temizlendi ve eski kullanıcı/hesap/hareket tüm sütunları karşılaştırıldı. UI mevcut açılış hareketini salt okunur gösterdi; gizlilik/Enter/390px client=scroll375 geçti. UI hata-retry/boş geçmiş ve geç yanıt yarışı bu tur yapay tetiklenmedi; koşullar koddan incelendi. Build/lint/paket/diff geçti. Şema/seed/parola hazırlığı tekrarlanmadı; commit/push yok. Öğrenme teyidi değildir.

Yönetici 10.7 incelemesi: kaynak/ilgili diff okundu; 18 test (8 gerçek Oracle,0 hata/skip), frontend build/lint/diff geçti. Normal Maven repackage çalışan backend JAR Windows kilidine takıldı; aynı pom/src ayrı inceleme klasöründe testler gereksiz tekrarlanmadan paketlendi (çıkış0). Sunucu kapatılmadı. Tarayıcıda Birikim açılış hareketi/Türkçe tarih/3500 tutar/işlem sonrası bakiye ve gizlilik doğrulandı; görünürlük eski haline alındı, ayrıntı açık kaldı. Mobil/klavye geliştirici kontrolüne aittir; boş/hata/retry/geç yanıt UI ile yapay tetiklenmedi. API testleri mevcut kayıtların tüm sütunlarını koruyup kendi verisini temizledi. İşlevsel bulgu, öğrenme teyidi ve commit/push yok.
## Yönetimin ilk parçası (11.6–11.7)
Yalnız ADMIN: GET /api/admin/users (id/username/name/role), GET /api/admin/accounts (hesap ve güvenli sahip id/ad), POST /api/admin/accounts. İstek yalnız ownerId/name belirler; sunucu CUSTOMER sahibini ve kırpılmış1–100 karakter adı denetler, parametreli SQL ve transaction kullanır. Bakiye daima0.00, para birimi TRY; sıfır hareket üretilmez. Ek bakiye/rol alanları kullanılmaz. Başarı201, doğrulama400, anonim401, yanlış rol/CSRF403. ADMIN müşteri hesap ve transfer uçlarına erişemez.
/admin ekranı kullanıcı/hesap listeleri ve CUSTOMER seçimli hesap açma formu içerir. Gönderimde alanlar/tekrar engellenir, başarı sonrası liste yenilenir; yenileme hatası başarıdan ayrı açıklanır. Sonuç belirsizse otomatik tekrar yoktur. Çıkışta yönetici ekranı bırakılır; cleanup geç yanıtları kullanmaz. Kullanıcı oluşturma/düzenleme/silme, hesap kapatma, bakiye düzenleme yoktur; yönetim tamamlanmış sayılmaz. İdempotency/RabbitMQ eklenmedi.
Gerçek kontroller:19 Maven testi (10 Security+9 izole Oracle),0 hata/skip. Admin yetkisi/CSRF, güvenli yanıtlar, geçersiz/ADMIN sahip/boş-101 karakter ad reddi, sahte bakiye/rolün yok sayılması,0.00 TRY, sahibinin erişimi/diğer müşteri404/boş hareket geçmişi geçti. Her test sonunda eski veriler tüm sütunlarıyla karşılaştırıldı. UI ayrı geçici CUSTOMER adına hesap açma/başarı/liste yenileme/boş form/Tab→ad/Enter/390px client=scroll375 geçti. UI test hesabı/müşterisi temizlendi; eski9 kaydın hash dahil tüm sütunları aynı. Liste yenileme hatası ve geç yanıt bu tur tarayıcıda yapay tetiklenmedi; kaynak korumaları incelendi. Build/lint/package/diff geçti. Şema/seed/parola hazırlığı çalıştırılmadı. Kod hazırlanması öğrenme teyidi değildir; commit/push yok.

Yönetici11.6–11.7 ilk parça incelemesi: Java/React/App/güvenlik/test/ilgili diff okundu;19 test (9 gerçek Oracle,0 hata/skip),frontend build/lint/diff geçti. Package/mobil/Tab geliştirici kontrolüne aittir, bu incelemede tekrarlanmadı. Gerçek UI demo_admin listeleri/boş form reddi/Enter ile müşteri02 adına hesap açma/başarı ve liste yenileme geçti. SQL yeni hesabı0.00 TRY ve sıfır hareket olarak doğruladı. Yalnız inceleme hesabı temizlendi, eski9 kaydın hash dahil tüm sütunları aynı. Admin ekranı normal listelerle açık bırakıldı. Yapay liste yenileme hatası/geç yanıt UI ile denenmedi; kaynak korumaları incelendi. Düzeltme gerektiren işlevsel bulgu, kaynak kod değişikliği, öğrenme teyidi ve commit/push yok.
## Yönetim ikinci parça (11.6–11.7)
- POST `/api/admin/users`: username/name/password alır, rol daima CUSTOMER; otomatik hesap açılmaz. Username kırpılır ve `[a-z0-9_]{3,50}` olur. Görünen ad kırpılmış1–100 Unicode karakter. Oracle unique kısıtı eşzamanlı tekrarı engeller,409 genel kullanıcı-adı mesajı döner.
- Parola kırpılmaz; en az15 Unicode karakter/en fazla72 UTF-8 bayt. Mevcut BCrypt PasswordEncoder yalnız hash'i DB'ye yazar. Yeni parola hiçbir giriş dosyasına/loga/DTO'ya eklenmez; React maskeler ve gönderirken alanı temizler, kalıcı depolamaz. Kaynaklar: [OWASP Authentication](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html#implement-proper-password-strength-controls), [bcrypt sınırı](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#input-limits-of-bcrypt). Bu proje mevcut bcrypt'i korur; bütün OWASP kontrolleri uygulandı anlamına gelmez.
- PATCH `/api/admin/users/{id}` yalnız CUSTOMER display_name; PATCH `/api/admin/accounts/{id}` yalnız hesap name sütununu günceller. İstek DTO'su sadece name alır, diğer alanlar kullanılmaz. Transaction/parametreli SQL/ADMIN/CSRF korunur; ADMIN kullanıcının adı bu uçla değişmez.
- Aktif müşteri oturumunun principal adı oturum açılırken yüklenir. Admin ad değiştirse bile `/me`, üst başlık ve müşteri adı tam sayfa yenilemede de eski kalır; çıkış ve yeniden giriş yeni adı alır. Hesap adı ise hesap verisi yeniden çekilince yenilenir. Bu görev oturumun güvenlik davranışını değiştirmez.
- AdminChanges üç küçük formu tutar; gönderim engeli, doğrulama/başarı, listeleri yenileme, yenileme hatasını işlem başarısından ayırma ve unmount koruması var. Belirsiz sonuçta otomatik POST tekrarı yok. Yönetim/proje tamamlanmış sayılmaz; silme/pasifleştirme/rol-parola değiştirme/hesap kapatma/idempotency/RabbitMQ eklenmedi.
Kontroller:22 Maven testi (10 Security+12 izole Oracle),0 hata/skip; gerçek yeni müşteri login/CUSTOMER/boş hesap, aynı-ad paralel201/409,14/15 ve72/73 ASCII,36/37 é (72/74 bayt),18/19 emoji (72/76 bayt), boş/uzun/geçersiz alanlar,CSRF/rol/ADMIN hedef reddi, DTO alan kurcalama ve değişmeyen diğer sütunlar/hareketler geçti. UI oluşturma/parola temizliği, müşteri ve hesap adını düzenleme, yeni müşteri girişiyle yeni ad/kendi0 TL hesabı,Enter/Tab/390px client=scroll375 geçti. Test kayıtları temiz, önceki9 kayıt hash dahil tüm sütunları aynı. Frontend build/lint, package ve diff geçti. UI liste-yenileme hatası/yanıt kaybı/geç yanıt yarışı yapay olarak tetiklenmedi, korumaları koddan incelendi. Şema/seed/parola hazırlığı çalıştırılmadı; mevcut demo parolaları ve yerel giriş dosyası değişmedi. Öğrenme teyidi veya commit/push yok.

Yönetici ikinci parça incelemesi: Java/React/ayar/test/ilgili diff okundu;22 test (12 gerçek Oracle,0 hata/skip),frontend build/lint/diff geçti. Gerçek Security login ve parola/unique/alan kurcalama backend testlerinde geçti. UI izole müşteri/hesap adlarını güncelledi; boş müşteri formu reddi ve maskeli parola input tipi doğrulandı. SQL diğer sütunları korudu; yalnız inceleme kayıtları temizlendi ve eski9 kayıt hash dahil aynı. Kod bulgusu: AdminChanges sonrası başarılı liste yenilemesi eski kök hata uyarısını kaldırmıyordu; AdminPage onChanged setError ile düzeltildi, build/lint geçti. Yapay UI yenileme hatası/yanıt kaybı/geç yanıt tetiklenmedi. Yeni müşteri oluşturma-giriş/parola temizliği UI/package/mobil/Tab geliştirici kanıtıdır, yönetici UI ile tekrarlamadı. Admin ekranı temiz bırakıldı. Öğrenme teyidi ve commit/push yok.
## Transfer idempotency (13.1–13.4, güncel davranış)
Önceki bölümlerdeki “idempotency yok” kayıtları tarihsel aşamayı anlatır. Artık POST /api/transfers zorunlu UUID `Idempotency-Key` başlığı alır. CUSTOMER/principal/CSRF ve kendi hesapları kuralı sürer. Yanıt `{transferId,message}`; başarılı replay aynı kimlik/mesajı döndürür, ikinci bakiye veya hareket çifti yazmaz. Kimlikler sayısal, tutarlar BigDecimal karşılaştırılır; baştaki sıfırlar ve25.5/25.50 eşdeğerdir. Tamamlanmış anahtarın farklı geçerli içeriği409, eksik/geçersiz anahtar400.

`db/oracle/13_transfer_idempotency.sql` metadata kontrolünden sonra ayrı bağlantıda uygulandı; tekrar çalıştırmayın. transfers kullanıcı+anahtar UNIQUE, kaynak/hedef/amount/TRY/id/zaman içerir. account_movements.transfer_id nullable FK'dir; eski3 açılış hareketi NULL kaldı. Yeni transfer kaydı, iki bakiye ve iki bağlı hareket tek transaction içinde commit/rollback olur. İlk denemede hesaplar sayısal sırayla kilitlenir. Aynı müşteri için app_users satırı WAIT5 kilidi kullanılır; bu basit çözüm farklı anahtarları da müşteri başına sıraya alır. Transaction timeout15s; UNIQUE INSERT beklemesi de transaction JDBC timeout'una tabidir. Replay bakiye kontrolünden önce aranır. UNIQUE hatası callback dışında, rollback tamamlanınca yeni transaction ile okunur; rollback-only işlemde devam edilmez.

React her yeni taslakta bir crypto.randomUUID üretir. Belirsiz sonuçta taslağın değişmez kopyası ve anahtarı tutulur; form alanları değişse bile “Aynı isteği tekrar dene” aynı isteği gönderir. Bekleyen istek çözülmeden yeni taslak oluşturulmaz. Otomatik POST tekrarı yoktur. Başarıdan sonra retry kaldırılır; hesap yenileme hatası transfer başarısından ayrı kalır. Anahtar/içerik yalnız bellektedir: sayfa yenileme/sekme kapatma/çıkışta kaybolur, localStorage/sessionStorage kullanılmaz. Bu durumda eski anahtar olmadan güvenli retry garanti edilmez; yeni anahtarla yeniden göndermeden önce bakiyeleri/hareketleri kontrol edin. Sunucu kayıtları kalıcıdır; anahtar süresi dolumu/temizlik politikası henüz yoktur. Eski GET/çıkış korumaları korunur.

Gerçek kontroller:25 Maven testi (10 Security+15 gerçek Oracle),0 hata/skip; peş peşe/eşzamanlı replay aynı kimlik/tek transfer/iki bağlı hareket, farklı içerik409, kayıp ilk yanıt sonrası tekrar, gerçek Oracle UNIQUE hatasından yeni transaction ile toparlanma, gerçek ikinci hareket CHECK hatasında transfer/bakiye/hareket rollback ve aynı anahtarla sonraki başarı geçti. Önceki güvenlik/yönetim/farklı anahtarlı paralel transfer testleri korundu. İlk test turunda geçersiz hesap FK hatası bulundu; INSERT hesap sahiplik kilidinden sonraya alınarak düzeltildi ve25 test yeniden geçti. Testler transfers dahil kendi kayıtlarını temizler ve eski verilerin snapshot'ını doğrular.

UI'de geçici wrapper ilk gerçek başarılı yanıtı kullanmayıp kontrollü hata üretti. Bekleyen25,50 TL istek, alan99'a değiştirilse de aynı kaldı; Enter ile retry başarı, bakiyeler74,50/35,50; SQL tek transfer/iki bağlı hareket doğruladı. Bu gerçek ağ kaybı değildir; gerçek ağ kaybı sınanmadı. Simülasyon kaldırıldı, servis diskten yeniden okundu; frontend build/lint/package/diff geçti. UI kayıtları transfers dahil temizlendi; eski9 satır tüm sütunları aynı, üç açılış hareketinin transfer_id alanı NULL. AdminPage setError düzeltmesi korundu. Kod hazırlanması öğrenme teyidi değildir; commit/push yok.

Yönetici13.1–13.4: migration/kaynak/ilgili diff/test/model incelendi;25 test (15 gerçek Oracle,0 skip),frontend build/lint/diff geçti. Gerçek HTTP/proxy izole ilk25.5 transferinden sonra çıkış/yeni giriş ve aynı UUID/eşdeğer id-tutar replay aynı sonucu döndürdü; farklı içerik409, eksik anahtar400; SQL tek transfer/iki bağlı hareket ve74.50/35.50 doğruladı. Test kayıtları transfers dahil temizlendi, eski9 kayıt tüm sütunları aynı. UI kontrollü hata/retry/package/mobil geliştirici kanıtıdır; bu incelemede UI veya gerçek ağ kaybı tekrarlanmadı. İşlevsel bulgu yok. Sonraki iş incelenen yönetim ikinci parça/idempotency GitHub ara kaydı; öğrenme teyidi yok.
## Başka müşteriye transfer (10.3–10.7 / 11.5 genişletmesi)
Önceki bölümlerdeki yalnız-kendi-hedef kuralı tarihsel aşamadır. Şimdi CUSTOMER, tam Oracle hesap kimliğiyle `GET /api/recipients/{accountId}` çağırır. DTO yalnız id/name/customerName/currency içerir; bakiye/hareket/kullanıcı adı/rol bilgisi yoktur. Geçersiz/bulunmayan/ADMIN hedef404; toplu alıcı araması yoktur. Bu numara “Demo hesap no”dur; gerçek IBAN değildir. Kendi hesap kartlarında görünür.
Transfer ekranında mevcut kendi-hesap select'i korunur; “Demo hesap no ile alıcı doğrula” işaretlenince numara ve doğrulama düğmesi açılır. Doğrulanan alıcı/hesap adıyla taslak ve ayrı transfer onayı gerekir. Numara/mod/kaynak değişimi doğrulamayı ve taslağı kaldırır; sürüm/cleanup eski yanıtı atlar. Tutar metni/UUID idempotency ve bekleyen isteğin değişmez içeriği korunur. Başarı yalnız gönderenin hesaplarını yeniler, alıcı bakiyesi gösterilmez.
GET doğrulaması yetki kanıtı değildir: POST sırasında kaynak sahipliği ve her iki hesap sahibinin CUSTOMER rolü SQL'de tekrar denetlenir. Kilit sorgusu dışta yalnız accounts kullanır; CUSTOMER denetimi EXISTS alt sorgusudur, FOR UPDATE OF a.balance WAIT5 kullanıcı satırlarını JOIN ile kilitlemez. İki hesap sayısal id sırasıyla kilitlenir; BigDecimal/TRY/sınırlar, tek transaction/iki hareket ve idempotency korunur. Kaynak müşteri için mevcut owner kilidi sürer; hedef kullanıcı satırı kilitlenmez. Başka kişinin hesap/ayrıntı/hareket uçları hâlâ404, ADMIN transfer/alıcı uçları403.
Gerçek kontroller:28 Maven testi (10 Security+18 gerçek Oracle),0 hata/skip; iki müşteri25.5 transferinde74.50/45.50/toplam120/tek transfer/iki bağlı hareket; eşdeğer replay ve farklı içerik409; yabancı kaynak/geçersiz-ADMIN-aynı hedef reddi, dört alanlı alıcı DTO'su ve yabancı hesap/hareket404. Ters yönlü eşzamanlı5 ve3 transferi98/22 ve iki transfer/dört hareketle geçti. Önceki yabancı hedef reddi testi yabancı kaynak reddine çevrildi; eski güvenlik/yönetim/rollback/idempotency testleri geçti. Her test eski veriyi karşılaştırıp yalnız kendi kayıtlarını temizler.
UI izole iki müşteri: demo no kartta, tek alıcı doğrulaması, numara düzenlemesinde eski taslak0,25,50 taslağı/ayrı Enter onayı, yalnız gönderen74,50 gösterimi,390px client=scroll375 geçti. SQL alıcı45,50/toplam120/tek transfer/iki hareket doğruladı. İzole kayıtlar transfers dahil temiz; eski9 kayıt tüm sütunları aynı. Geç alıcı yanıtı yarışı/gerçek ağ kaybı bu tur yapay olarak tetiklenmedi; sürüm ve immutable retry korumaları kaynakta incelendi. Build/lint/package/diff geçti. Yeni migration/bağımlılık, eski SQL/seed/parola hazırlığı yok. AdminPage setError korundu. Öğrenme teyidi veya commit/push yok.

Yönetici10.3–10.7/11.5 çapraz müşteri incelemesi: ilgili Java/React kaynakları, güvenlik/testler ve diff okundu; 28 Maven testi (10 Security+18 gerçek Oracle,0 hata/skip),frontend build/lint/diff geçti. Gerçek UI tam demo no→alıcı doğrulama→numara değişiminde doğrulama/taslak iptali→yeniden doğrulama→25,50 taslak→ayrı POST akışını doğruladı. İzole100/20 hesapları74,50/45,50 oldu; gönderen yalnız kendi hesaplarını gördü, alıcı bakiyesi gösterilmedi. SQL tek transfer/iki bağlı DEBIT-CREDIT hareketi doğruladı. Yalnız bu inceleme kayıtları temizlendi; eski9 kayıt bütün sütunlarıyla aynı. Normal iki hesaplı müşteri01 ekranı açık bırakıldı. İşlevsel düzeltme bulgusu yok. Package/390px/Enter geliştirici kanıtıdır; bu tur tekrarlanmadı. Geç alıcı yanıtı/gerçek ağ kaybı yapay tetiklenmedi; kaynak korumaları incelendi. Öğrenme teyidi ve commit/push yok.
## RabbitMQ ve ayrı worker (14.1–14.4)
Yalnız sentetik metin taşınır: açık gönderim → direct exchange → routing key → durable kuyruk → ayrı worker günlüğü. Backend/frontend/Oracle ile bağlantı eklenmedi; normal başlangıç mesaj göndermez. Worker web sunucusu veya veritabanı bağlantısı açmaz.

- `infra/rabbitmq/compose.yaml`: HesapLab'a özel container/volume/vhost. Resmî `rabbitmq:4.4.0-management`, doğrulanan digest ile sabit; yalnız `127.0.0.1:5673` AMQP ve `127.0.0.1:15673` yönetim arayüzü.
- `worker/`: Java21, backend ile aynı Boot3.5.16, yönetilen Spring AMQP3.2.12; Wrapper3.3.4/Maven3.9.11. Bağımlılıklar yalnız Boot AMQP starter ve test starter.
- Exchange `hesaplab.demo.exchange`, kuyruk `hesaplab.demo.queue`, routing key `hesaplab.demo.message`; vhost `hesaplab`. Durable exchange/kuyruk, PERSISTENT mesaj. Gönderici publisher confirm ve yönlendirilemeyen mesaj dönüşünü kontrol eder; en fazla5 saniye onay bekler. Belirsiz sonuçta otomatik tekrar yoktur.
- Kimlik bilgileri yalnız Git dışındaki `infra/rabbitmq/.env.local` dosyasında; Compose/worker aynı dosyayı okur. `worker/target`, `.tools` ve loglar da Git dışında.

### Başlatma (PowerShell)
Docker Desktop çalışırken proje köküne geçin. Yerel kimlik dosyası bu çalışmada oluşturuldu. Yeni klonda, dosya yoksa bir kez üretin; var olan kimlikleri değiştirmeyin:
```powershell
Set-Location -LiteralPath 'C:/Users/berkc/Documents/ChatGPT/1react Proje ÖZet/HesapLab'
if (-not (Test-Path infra/rabbitmq/.env.local)) {
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes); $rng.Dispose()
    $rabbitPassword = -join ($bytes | ForEach-Object { $_.ToString('x2') })
    [IO.File]::WriteAllText((Join-Path (Get-Location) 'infra/rabbitmq/.env.local'), "RABBITMQ_USER=hesaplab_demo`nRABBITMQ_PASSWORD=$rabbitPassword`n")
    Remove-Variable rabbitPassword, bytes
}
docker compose --env-file infra/rabbitmq/.env.local -f infra/rabbitmq/compose.yaml up -d --wait --wait-timeout 180
```
Kimlik bilgileri yalnız boş broker volume'ünün ilk açılışında uygulanır; env dosyasını değiştirmek mevcut broker parolasını değiştirmez. Yönetim adresi: http://127.0.0.1:15673 (yerel dosyadaki kimlikleri kullanın, paylaşmayın).

Worker terminali (çalışma dizini `worker` olmalı; env yolu buna göredir):
```powershell
Set-Location worker
$env:JAVA_HOME = 'C:/Program Files/Android/openjdk/jdk-21.0.8'
./mvnw.cmd clean test package
& "$env:JAVA_HOME/bin/java.exe" -jar target/hesaplab-worker-0.0.1-SNAPSHOT.jar
```
Java yolu bu makinede doğrulandı; başka makinede kendi JDK21 yolunuzu kullanın. Değişken yalnız bu terminal içindir. Normal worker Ctrl+C ile durdurulur; backend/Oracle kapatılmaz.

Başka bir terminalde, aynı `worker` dizininde **açıkça** gönderin:
```powershell
& 'C:/Program Files/Android/openjdk/jdk-21.0.8/bin/java.exe' -jar target/hesaplab-worker-0.0.1-SNAPSHOT.jar --spring.profiles.active=send-demo
```
Tek seferlik profil listener açmaz; UUID `messageId` ve `HesapLab sentetik demo mesajı.` gönderip çıkar. Her yeni komut yeni mesajdır. `DEMO_SENT messageId=...` ile worker'daki `DEMO_PROCESSED messageId=...` aynı olmalı. Burada işleme yalnız log üretir; listener başarıyla dönünce mesaj onaylanır. Gerçek müşteri verisi veya parola mesaj yapılmaz.

### Worker kapalıyken gözlem ve gerçek kontrol kaydı
1. Yalnız worker'ı Ctrl+C ile durdurun; RabbitMQ açık kalsın. Gönderim komutunu çalıştırın.
2. Proje kökünde kuyruğu okuyun:
   ```powershell
   docker exec hesaplab-rabbitmq rabbitmqctl list_queues -p hesaplab name durable messages_ready messages_unacknowledged consumers
   ```
3. Worker kapalıyken ready1/consumers0; worker açılınca aynı kimlik logda görünür ve ready0/unacked0/consumers1 olur. Başka mesaj/worker varsa sayılar değişebilir.

2026-11-02 geliştirici kontrolü: resmî4.4.0 manifest/digest, Compose config/health doğrulandı. Kapalı-worker mesajı `1695e320-9a80-4d16-a7cf-73fc7b5a7c15`: ready1/unacked0/consumers0. Yönetim API'sinden yalnız sentetik mesaj requeue edilerek delivery_mode2/kimlik/metin doğrulandı. Yalnız yeni RabbitMQ container'ı yeniden başlatıldı, ready1 korundu. Worker açılınca aynı kimlik işlendi; ready0/unacked0/consumers1. Son temiz paketle normal başlangıç mesaj üretmedi; ikinci açık gönderim `553f49d9-5ba5-4e6b-bb98-0d4ee804cf8d` aynı kimlikle işlendi. İki gönderici çıkış0. Beş worker birim testi/0 hata/skip, clean test package ve diff kontrolü geçti. Broker/worker çalışır bırakıldı. Backend/frontend/Oracle testleri bu bağımsız adımda tekrarlanmadı; DB sorgusu/yazımı veya UI testi yapılmadı.

Retry/DLQ/outbox ve tüketici idempotency yoktur; yeniden teslim olabilir. İşleme hatasında requeue kapalıdır; hatalı mesaj DLQ olmadan kaybolabilir. Gerçek transfer/dekont, Oracle yazımı ve hata senaryoları sonraki görevdir. Kod hazırlanması öğrenme teyidi değildir.

Resmî kaynaklar: [RabbitMQ imajı](https://hub.docker.com/_/rabbitmq), [sürümler](https://www.rabbitmq.com/release-information), [publisher onayları](https://www.rabbitmq.com/docs/publishers), [Spring AMQP gönderim](https://docs.spring.io/spring-amqp/reference/amqp/template.html). İmaj digest'i Compose dosyasında; AMQP sürümünü Boot yönetir.

Yönetici14.1–14.4: dört Java sınıfı/ayarlar/POM/Compose/Wrapper/testler ve ilgili diff incelendi. Wrapper dosyaları mevcut backend kopyasıyla eşleşir. Beş worker testi0 hata/skip,Compose config/health/localhost5673-15673/volume ve diff geçti; yerel RabbitMQ parolası yeni özellik Git adaylarında yok,env/target/.tools ignored. Gerçek offline mesaj737c146d-e3c0-4b8d-a395-3a8e18ae0914 worker kapalıyken ready1/consumers0; yalnız RabbitMQ yeniden başlatılınca ready1 korundu; worker açılınca aynı kimlik işlendi,ready0/unacked0/consumers1. Canlı fdd37e38-18f0-4a99-af21-0fa8356f89d3 aynı kimlikle işlendi; normal başlangıç gönderim yapmadı. Worker yeniden çalışır bırakıldı (.tools/worker.pid güncel); backend5181/8081 dinliyor,Oracle healthy. Bu incelemede Oracle sorgusu/yazımı veya UI testi yok; clean/package geliştirici kanıtıdır. Kodda işlevsel düzeltme bulgusu yok. Şimdiki tüketici yalnız log üretir; retry/DLQ/idempotency/transfer entegrasyonu henüz yok. Öğrenme teyidi ve commit/push yok.

## Asenkron metin dekontu (14.5–14.8, güncel)
Yeni transferde tek `transfer_receipts` kaydı, transfer/bakiyeler/iki hareketle **aynı Oracle transaction'ında** oluşur. Rollback olursa dekont kaydı da yoktur ve mesaj gönderilmez. Idempotency replay mevcut transferi döndürür; ikinci dekont yaratmaz.

Commit tamamlanınca backend `hesaplab.receipt.exchange` → `hesaplab.receipt.prepare` → durable `hesaplab.receipt.queue` yoluna PERSISTENT mesaj gönderir. Gövde yalnız transfer kimliğidir; mesajda parola, oturum, kişi adı veya bakiye yoktur. Önceki sentetik demo exchange/kuyruğu korunur. Normal başlangıç transfer/dekont/demo mesajı üretmez.

Worker Oracle'dan transferi ve görünen adları okur; dekont satırını `FOR UPDATE WAIT 5` ile kilitleyip metni kaydeder. `READY` sonucu tekrar mesajla veya geç hata güncellemesiyle değişmez. Listener'ın AUTO onayı, işleyicinin Oracle transaction'ı commit olup metot döndükten sonradır. Worker bakiyelere/hareketlere yazmaz. Metin; transfer no/tarih, gönderen-alıcı adları, demo hesap numaraları, tutar/TRY içerir; alıcı bakiyesi veya hareketleri içermez. PDF/e-posta yoktur.

| Durum | Anlamı |
| --- | --- |
| PENDING | Dekont bekliyor/hazırlanıyor; manuel olarak yeniden istenebilir. |
| READY | Metin kaydedildi; tekrar istek mevcut sonucu döndürür. |
| FAILED | Yayın veya hazırlamada sorun var; para transferinin sonucundan ayrıdır. |

Yayın hatası tamamlanmış transferin HTTP başarısını bozmaz. Yayın hata kaydı, commit sonrası eski transaction'a katılmadan `REQUIRES_NEW` ile kalıcı kaydedilir. İşleme hatasında worker da ayrı transaction ile FAILED yazmayı dener; Oracle erişilemiyorsa durum PENDING kalabilir. Hata içeriği SQL/stack trace içermez.

- GET `/api/transfers/{id}/receipt`: transferId/status/content/errorMessage.
- POST aynı adres: yalnız PENDING/FAILED dekontu yeniden kuyruğa ister; yeni para transferi çalıştırmaz. READY ise mesaj üretmez.
- Yalnız gönderen CUSTOMER; sahiplik SQL'de principal kimliğinden gelir. Alıcı/yabancı/bulunmayan/geçersiz aynı404; ADMIN403, anonim401. POST CSRF korumalıdır.
- Başarılı transferden sonra React dekont durumunu ayrıca getirir. “Dekont durumunu yenile” manuel GET, “Dekontu tekrar iste” ayrı POST'tur; otomatik transfer veya dekont tekrarı yoktur.
- Tam yenilemeden sonra kendi hesap ayrıntısındaki giden hareket → “Dekontu görüntüle” ile tekrar ulaşılır. Hareket DTO'sundaki nullable receiptTransferId yalnız gönderenin DEBIT hareketinde gelir; eski açılış hareketleri ve eski dekontsuz transferler NULL kalır.
- Bakiye gizliliğinde metin gizlenir. Çıkış/kullanıcı değişiminde bileşen bırakılır; sürüm/cleanup geç yanıtları kullanmaz.

### Kurulum ve çalıştırma
`db/oracle/14_transfer_receipts.sql` metadata kontrolü sonrası ayrı SQL*Plus bağlantısında bu makinede **bir kez uygulandı**; tekrar çalıştırmayın. Eski şema/seed/parola hazırlığı çalıştırılmaz. Yeni ortamda 13 sonrası yalnız yeni migration ayrıca uygulanmalıdır; uygulamalar otomatik SQL çalıştırmaz.

RabbitMQ Compose ve portları önceki bölümle aynıdır. Backend artık yönetilen AMQP starter kullanır; worker JDBC starter ve backend ile aynı ojdbc17 sürümünü kullanır. Her ikisi Git dışındaki Oracle/RabbitMQ env dosyalarını okur. Önceki 14.1–14.4 kaydı tarihsel olarak yalnız sentetik log demosunu anlatır; güncel worker Oracle dekont tablosuna da bağlanır. Proje kökünden iki ayrı terminalde:
```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/openjdk/jdk-21.0.8'
Set-Location backend
./mvnw.cmd test package
& "$env:JAVA_HOME/bin/java.exe" -jar target/hesaplab-backend-0.0.1-SNAPSHOT.jar
```
Diğer terminalde aynı JDK değişkenini ayarlayıp `worker` dizininde `./mvnw.cmd test package`, ardından `& "$env:JAVA_HOME/bin/java.exe" -jar target/hesaplab-worker-0.0.1-SNAPSHOT.jar` çalıştırılır. Maven için hedef JAR çalışıyorsa yalnız o projeye ait süreci durdurun; başka Java/Oracle süreçlerini kapatmayın. Gerçek Oracle testleri için paketleme öncesinde yalnız o terminalde `$env:HESAPLAB_ORACLE_TEST='true'` gerekir; testler kendi izole verilerini FK sırasıyla temizler. Frontend önceki gibi localhost5181'de çalışır; backend8081, broker5673/15673.

### Teslim sınırı
**Outbox yoktur; kayıpsız teslim garantisi verilmez.** Oracle commit'i ile RabbitMQ yayını arasında süreç durursa transfer tamamlanmış, dekont PENDING ve mesaj gönderilmemiş olabilir. Yayın onayının kaybında mesaj işlenmiş de olabilir. Bekleyen/başarısız dekontu manuel tekrar istemek yalnız aynı dekontu hazırlar; transfer yeniden çalışmaz. Tüketici tekrarı aynı READY kaydını korur; bu, DB ile broker arasında atomik teslim sağlamaz. Otomatik retry/DLQ yoktur; reddedilen mesaj kaybolabilir, kullanıcı dekontu tekrar isteyebilir.

### Gerçek kontrol kaydı — geliştirici, 2026-11-02
- Son backend:34 test (10 Security,22 gerçek Oracle,2 publisher),0 hata/skip ve package geçti. Son worker:10 test (5 önceki demo,3 consumer,2 gerçek Oracle),0 hata/skip, clean test/package geçti. Frontend build/lint uyarısız; diff kontrolü geçti.
- Backend testleri: receipt CHECK yazma hatasında tüm transfer/iki bakiye/hareket/dekont rollback ve sıfır yayın; commit'in ayrı bağlantıda görünmesinden sonra yayın; yayın hatasında kalıcı FAILED; kanonik kimlikle tekrar istek; replay; yalnız gönderici/ortak404/ADMIN/anonim/CSRF ve alıcı hareket DTO'sunda dekont kimliği olmaması. Önceki güvenlik/yönetim/paralel transfer testleri korundu.
- Worker testleri: gerçek Oracle yazma hatasında yarım READY yok; FAILED sonrası hazırlanma; eşzamanlı/tekrar işlemde tek READY ve değişmeyen içerik/zaman. AUTO onay sırası koddan ve metot sınırından incelendi; commit sonrası/ack öncesi süreç öldürme yapılmadı.
- Gerçek HTTP + RabbitMQ + ayrı worker: worker kapalı transfer105 başarılı/PENDING, receipt kuyruğunda ready1/consumers0; worker açılınca READY. Replay eşdeğer sıfırlı kimlik/25.5 ile aynı transfer; broker üzerinden tekrar mesaj ve dekont isteği tek sonucu korudu.
- Yalnız HesapLab broker kapalıyken transfer106 başarılı kaldı, dekont FAILED kalıcıydı. Broker açıldıktan sonra yalnız dekont POST'u ile READY oldu. İzole iki transfer sonunda kaynak69.50, alıcı50.50, diğer hesap10.00; toplam130 korundu,2 transfer/4 hareket/2 dekont vardı.
- İzole HTTP kayıtları dekont→hareket→transfer→hesap→kullanıcı sırasıyla temizlendi. Önce/sonra bütün mevcut satır fingerprint'leri eşleşti; testler de tam snapshot karşılaştırdı. Eski3 hesap/3 hareket ve kullanıcılar korundu. Migration dışında eski SQL/parola hazırlığı yoktu.
- **UI doğrulanmadı:** Windows computer-use aracı mevcut browser URL'sini güvenilir doğrulayamadığı için kontrolü durdurdu. İzole UI fixture oluşturulmuştu ancak kullanılmadan temizlendi. Tarayıcıda yeni dekont akışı, yenilemeden hareketten erişim, Tab/Enter ve390px bu tur çalıştırılmadı; JSX/geç yanıt/gizlilik ve dar ekran stilleri koddan incelendi. Gerçek ağ kaybı veya commit→yayın arasında süreç kesintisi simüle edilmedi.
- Backend/frontend/worker/broker/Oracle çalışır bırakılır. Bu kayıt geliştiriciye aittir; yönetici incelemesi ve Berk'in öğrenme teyidi henüz yok. Commit/push yok.

Resmî dayanak: [Spring afterCommit ve REQUIRES_NEW](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html), [Spring AMQP listener onayı](https://docs.spring.io/spring-amqp/reference/amqp/containerAttributes.html). Eski kontrol kayıtları yukarıda kaynaklarıyla korunur.

Yönetici14.5–14.8: migration14/Java-React/ayarlar/testler ve ilgili diff incelendi; 34 backend (22 gerçek Oracle)+10 worker (2 gerçek Oracle) testi0 hata/skip,frontend build/lint/diff geçti. Gerçek IAB kontrolü geliştiricinin eksik UI doğrulamasını tamamladı: izole kaynak955/hedef956 ile25,50 transfer163,worker kapalıyken PENDING/iki hareket; worker açılınca READY/metin,gizlilik ve Enter ile durum yenileme doğrulandı. Yalnız broker kapalıyken5 TL transfer164 başarılı/kalıcı FAILED; broker geri açılıp yalnız dekont tekrar isteğiyle READY oldu. Son69,50/50,50,iki transfer/dört hareket/iki READY dekont; tekrar dekont isteği para işlemi üretmedi. Tam sayfa yenileme→giden hareket→iki dekontu açma,Tab/Enter,alıcıda dekont düğmesi olmaması geçti. 390px client=scroll375 ve1280px client=scroll1265; mobil görsel kontrol geçti. Viewport geri alındı. İnceleme kayıtları dekont→hareket→transfer→hesap sırasıyla temizlendi; beş tabloda eski9 kaydın bütün sütun fingerprinti aynı. Müşteri01 normal10.000/3.500 ekranı açık; backend/frontend/worker/broker/Oracle çalışır. Yerel Oracle/RabbitMQ/demo parolaları Git adaylarında yok. Package geliştirici kanıtıdır; bu tur tekrarlanmadı. Gerçek ağ yanıt kaybı,geç UI yanıtı ve commit/ack öncesi süreç kesintisi yapay tetiklenmedi; kaynak korumaları incelendi. İşlevsel düzeltme bulgusu yok. Outbox/otomatik retry/DLQ yok; teslim boşluğu belgeli. Öğrenme teyidi ve commit/push yok.
