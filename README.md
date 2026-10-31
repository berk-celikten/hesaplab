# HesapLab
Bankacılık üzerinden yazılım geliştirmeyi öğrenmek için eğitim projesi.
Gerçek para, gerçek müşteri bilgisi veya banka bağlantısı içermez.

## Mevcut özellikler
- Hesaplar ilk açılışta ve tam yenilemede useEffect([]) ile yaklaşık 1000ms gecikmeli demo servisinden yüklenir; buton ayrıca manuel yenileme başlatır.
- İlk veri yokken hesaplar/ayrıntı/transfer yüklenme mesajı gösterir; 404 beklemez. Tek hesap veri kaynağı `frontend/src/services/accountService.js` içindedir. Servis yeni liste/nesne kopyaları döndürür; App bunları accounts state'ine alır.
- Yüklenirken buton devre dışıdır; kart/sayı/boş sonuç yerine erişilebilir yükleniyor mesajı gösterilir. Arama ve müşteri seçimi kullanılabilir.
- Yenileme gezinme, arama, gizlilik veya transfer durumunu sıfırlamaz; müşterinin hiç hesabı olmaması ile arama eşleşmemesi ayrı açıklanır. Gerçek ağ isteği yoktur; kontrollü demo hatası ve başarılı tekrar deneme vardır.
- Vite + React + JavaScript ile mobil uyumlu hesap özeti.
- App, Header, AccountsPage, AccountDetailsPage, TransferPage, AccountList ve AccountCard component'leri.
- Demo Müşteri 01: Vadesiz Hesap (10.000,00 TL) ve Birikim Hesabı (3.500,00 TL).
- Demo Müşteri 02: Vadesiz Hesap (2.000,00 TL).
- Butonlarla demo müşteri seçimi; seçilen müşterinin adı, hesap sayısı ve hesapları gösterilir.
- Seçili müşterinin hesaplarında Türkçe büyük/küçük harf uyumlu hesap adı araması; sonuç sayısı ve boş sonuç açıklaması.
- Arama metni ve gizlilik tercihi müşteri değişiminde korunur; yenilemede arama boş başlar.
- Kaynak hesap, seçili müşterinin bütün hesaplarından açılır listeyle seçilir; arama sonuçlarından bağımsızdır.
- Kaynak seçimi müşteri gerçekten değişince ve yenilemede boşalır; aynı müşteri, arama ve gizlilik değişiminde korunur.
- Tutar alanı ham metni korur; gönderimde tam sayı veya virgül/noktayla bir-iki ondalık basamak, sonlu sayı ve sıfırdan büyük değer aranır.
- Tutar kaynak/arama/gizlilik ve aynı müşteri seçiminde korunur; farklı müşteri ve yenilemede boşalır.
- Kaynak, hedef ve ham tutar metni form gönderiminde yalnızca tarayıcıda transfer taslağı olarak önizlenir.
- Hedefler kaynak dışındaki bütün demo hesaplardır. Alan değişince taslak kaldırılır; farklı müşteri formu temizler.
- Gönderimde boş kaynak/hedef ve boş veya yalnız boşluk tutar için alan hataları birlikte gösterilir; eksik alanlarla taslak oluşmaz.
- Alan düzenlenince hatalar ve taslak temizlenir; farklı müşteri formu da temizler. Arama/gizlilik ve aynı müşteri mevcut durumu korur.
- Kenar boşlukları yalnız doğrulamada yok sayılır; binlik ayırıcı, işaret, üslü gösterim, “0” ve “abc” reddedilir. “1.00” bir ondalık yazımdır ve 1 anlamına gelir.
- Yenilemede taslak ve hatalar yoktur. Bakiye yeterliliği veya transfer hesabı yapılmaz.
- Bakiyeler tek butonla gizlenir/gösterilir; müşteri değişiminde görünürlük tercihi korunur.
- Seçim ve görünürlük App state'inde tutulur; yenilemede müşteri 01 ve görünür bakiyelerle başlar.
- Sabit örnek veriler props ve liste gösterimi ile aktarılır.

Demo müşteri seçimi gerçek giriş değildir. Henüz gerçek giriş, transfer, API, React'in Oracle bağlantısı veya RabbitMQ yoktur.
Gelecek aşamalar docs/ROADMAP.md içindedir.

## Sayfalar ve gezinme
- React Router 8.5.0, Declarative yaklaşım: BrowserRouter, Routes ve Route.
- `/hesaplar`: hesap sayısı, arama, sonuç açıklaması ve kartlar.
- `/hesaplar/:accountId`: listedeki “Ayrıntıları gör” bağlantısıyla hesap adı/sahibi/bakiyesi; gizlilik korunur.
- Ayrıntı kimliği useParams ile okunur ve yalnız seçili müşterinin bütün hesaplarında aranır; arama filtresinden bağımsızdır.
- Geçersiz veya diğer müşterinin kimliği aynı bulunamadı açıklamasını gösterir. Bu demo görünüm kontrolüdür, gerçek yetkilendirme değildir.
- URL müşteriyi değiştirmez: customer-02 seçiliyken açılan checking-02 adresi yenilenirse başlangıç müşterisi 01 olur ve hesap bulunamaz.
- `/transfer`: kaynak/hedef/tutar formu, doğrulama ve taslak önizlemesi.
- `/`, replace ile `/hesaplar` adresine yönlenir; bilinmeyen adres dönüş bağlantılı hata sayfası gösterir.
- Menü ve marka bağlantıları tam yenileme yapmaz. Ortak state ve form handler'ları App'te kalır; sayfalar props alır. Ayrıntı hesabı AccountDetailsPage içinde URL kimliği ve customerAccounts üzerinden bulunur.
- Menü ve tarayıcı geri/ileri gezinmesi mevcut değerleri korur. Doğrudan açma/yenileme, aynı adreste başlangıç state'lerini yükler.
- Vite yerel sunucusu doğrudan adresleri destekler. İleride statik sunucuda bilinmeyen yolların index.html'e yönlendirilmesi gerekir.

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

İlk yükleme Effect cleanup işlemi yerel ignore bayrağını değiştirir; eski çalışmanın sonuç ve finally state güncellemeleri atlanır. Bu, zamanlayıcıyı/isteği iptal etmez. StrictMode korunur; geliştirmede ek setup → cleanup → setup döngüsü olabilir. Normal gezinme ilk yüklemeyi yeniden başlatmaz.

5.7.1: Hesaplarım ekranındaki “Demo hatasını dene” deterministik hata üretir; “Tekrar dene” ve normal yenileme başarı yolunu çağırır. Hata erişilebilir mesajla gösterilir. Önceki veri varsa korunup eski veri olduğu açıklanır; veri yoksa ayrıntı/form/boş sonuç yerine hata gösterilir. Yeni denemede hata temizlenir; istek sürerken yükleme butonları devre dışıdır. İlk yükleme normalde başarılıdır.

## Oracle veri modeli ve yerel çalışma ortamı (6.1–6.7)
- `db/oracle/01_schema.sql`: Oracle 19c için kullanıcı/hesap tabloları, identity kimlikler ve temel kısıtlar.
- `db/oracle/02_seed.sql`: iki demo müşteri, hesapsız demo admin ve 10000.00 / 3500.00 / 2000.00 TRY hesapları; sahipler benzersiz kullanıcı adıyla bulunur.
- Model ve Mermaid ilişki diyagramı: [DATABASE_MODEL.md](docs/DATABASE_MODEL.md).
- Şema ve seed yerel Oracle AI Database 26ai Free 23.26.3.0.0 üzerinde SQL*Plus ile çalıştırıldı; üç kullanıcı ve üç hesap sorgulandı. Oracle 19c sunucusunda ayrıca denenmedi. İlk kurulum dosyaları boş şema için tek çalıştırımlıktır.
- `db/oracle/compose.yaml`: HesapLab'a özel konteyner/veri alanı; bağlantı `127.0.0.1:1522/FREEPDB1`, kullanıcı `HESAPLAB`. Parolalar Git dışında kalan `.env.local` dosyasındadır. Başlatma/durdurma ayrıntıları model belgesinde.
- `db/oracle/03_read_queries.sql`: kullanıcı ve hesap kayıtlarını gösteren salt okunur sorgular.
- Grafik arayüzü: VS Code içindeki resmî Oracle SQL Developer 26.3.0; `Connections → HesapLab → Tables → APP_USERS/ACCOUNTS → Data`. ER için [hesaplab.dgm](docs/diagrams/hesaplab.dgm) dosyasını açın; gerekirse `Fit to Screen` kullanın. Önceki iki tablo/FK görünümü doğrulandı; 7.2 ile native dosyaya üçüncü tablo eklendi. Eski PNG yalnız kullanıcı/hesap modelini gösterir. Model belgesinde ayrıca statik ER resmi bulunur.
- React demo kimlikleri ve verileri korunur; bu dosyalar arayüze bağlı değildir. Hazırlık, konuların öğrenildiği anlamına gelmez.

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
