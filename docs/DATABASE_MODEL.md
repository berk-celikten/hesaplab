# Oracle veri modeli — kullanıcı, hesap, hareket, transfer ve dekont

Kullanıcı, hesap ve hareket tabloları yerel Oracle AI Database 26ai Free 23.26.3.0.0 üzerinde oluşturuldu.
SQL, Oracle 19c söz dizimi hedefiyle yazıldı; Oracle 19c sunucusunda ayrıca denenmedi.
Dosyaların hazırlanması, Berk'in bu konuları öğrendiği anlamına gelmez.

## Tablo, satır ve sütun

`app_users` kullanıcıları, `accounts` hesapları tutan tablolardır.
Bir satır tek kullanıcıyı veya tek hesabı temsil eder. Sütun o kaydın bir bilgisidir:
örneğin `display_name` kullanıcı adı gösterimini, `balance` hesap bakiyesini tutar.

Her tablonun `id` sütunu primary key'dir: her satırı benzersiz tanımlar.
`NUMBER GENERATED ALWAYS AS IDENTITY` sayesinde kimliği Oracle üretir; seed kimlik vermez.
API bu sayısal Oracle kimliklerini JSON metni olarak taşır; React seçim ve URL'lerinde aynı kimlikler kullanılır.
Eski checking/savings/checking-02 ve customer-01/02 demo kimliklerine sabit eşleme yoktur.

```mermaid
erDiagram
    app_users ||--o{ accounts : owns
    accounts ||--o{ account_movements : records
    app_users ||--o{ transfers : requests
    accounts ||--o{ transfers : participates
    transfers |o--o{ account_movements : links
    transfers ||--o| transfer_receipts : has
    transfers ||--o{ receipt_outbox : emits
    app_users {
        NUMBER id PK
        VARCHAR2 username UK
        VARCHAR2 display_name
        VARCHAR2 role
        VARCHAR2 password_hash
    }
    accounts {
        NUMBER id PK
        NUMBER owner_user_id FK
        VARCHAR2 name
        NUMBER balance
        VARCHAR2 currency
    }
    transfers {
        NUMBER id PK
        NUMBER user_id FK
        VARCHAR2 idempotency_key
        NUMBER source_account_id FK
        NUMBER target_account_id FK
        NUMBER amount
        VARCHAR2 currency
        TIMESTAMP_WITH_TIME_ZONE created_at
    }
    receipt_outbox {
        VARCHAR2 event_id PK
        NUMBER transfer_id FK
        NUMBER request_no
        VARCHAR2 event_type
        VARCHAR2 status
        NUMBER attempts
        TIMESTAMP_WITH_TIME_ZONE next_attempt_at
        TIMESTAMP_WITH_TIME_ZONE created_at
        TIMESTAMP_WITH_TIME_ZONE sent_at
        VARCHAR2 last_error
    }
    transfer_receipts {
        NUMBER id PK
        NUMBER transfer_id FK,UK
        VARCHAR2 status
        CLOB content
        VARCHAR2 error_message
        TIMESTAMP_WITH_TIME_ZONE created_at
        TIMESTAMP_WITH_TIME_ZONE updated_at
    }
    account_movements {
        NUMBER id PK
        NUMBER account_id FK
        NUMBER transfer_id FK
        VARCHAR2 direction
        NUMBER amount
        NUMBER balance_after
        VARCHAR2 description
        TIMESTAMP_WITH_TIME_ZONE created_at
    }
```

`accounts.owner_user_id`, `app_users.id` sütununa foreign key ile bağlıdır.
Bir kullanıcı sıfır veya daha çok hesaba sahip olabilir; her hesap bir kullanıcıya bağlıdır.
SQL dosyasından hazırlanıp görsel olarak kontrol edilen [ER resmi](diagrams/hesaplab-er.png) yalnız önceki iki tabloyu (app_users/accounts) ve aralarındaki ilişkiyi gösterir; account_movements içermez ve canlı veritabanı görünümü değildir.
Foreign key kullanıcının varlığını sağlar; rolünün CUSTOMER olduğunu tek başına garanti etmez.
`role` kısıtı yalnız CUSTOMER/ADMIN değerlerini kabul eder. Spring Security CUSTOMER rolünü, hesap sorguları oturum sahibini SQL koşuluyla denetler. Foreign key tek başına bu yetki denetimini yapmaz.
Demo admin yalnız başlangıç ekranına erişir; müşteri hesap uçlarına ve transfere erişimi yoktur. Ayrı 12_user_password_hash.sql migration'ı nullable VARCHAR2(100 CHAR) sütunu ekler; açık hazırlık komutu yalnız eksik bcrypt hash'lerini doldurur. Normal başlangıç parola değiştirmez. Yerel parola dosyası Git dışındadır; README hazırlık adımlarını açıklar. Native dgm ve eski PNG bu yeni sütundan önceki şema görünümüdür; bu Mermaid güncel sütunu içerir.

## Para ve demo sahipliği

`balance NUMBER(15,2)` sayısal değer tutar: örneğin `10000.00`.
React'teki `10.000,00 TL` ise kullanıcıya sunulan biçimlendirilmiş metindir.
Veritabanında binlik ayırıcı veya TL metni bakiyeye eklenmez; para birimi ayrı `currency` sütunundadır.
Bakiye varsayılanı 0'dır, NULL/negatif olamaz. Para birimi varsayılan ve tek kabul edilen değer TRY'dir.
Kullanıcı adı benzersiz ve zorunlu; görünen ad, rol, hesap sahibi ve hesap adı da zorunludur.

| Kullanıcı adı | Rol | Hesap | Sayısal bakiye |
| --- | --- | --- | --- |
| demo_customer_01 | CUSTOMER | Vadesiz Hesap | 10000.00 |
| demo_customer_01 | CUSTOMER | Birikim Hesabı | 3500.00 |
| demo_customer_02 | CUSTOMER | Vadesiz Hesap | 2000.00 |
| demo_admin | ADMIN | Hesap yok | — |

## Uygulama durumu

Yerel ortam `db/oracle/compose.yaml` ile hazırlandı. Konteyner `hesaplab-oracle`,
ayrı veri alanı `hesaplab-oradata`, imaj `gvenzl/oracle-free:23.26.3-slim-faststart`.
Yalnız `127.0.0.1:1522` üzerinden erişilir; servis adı `FREEPDB1`, bağlantı kullanıcısı `HESAPLAB`.
`bankflow` konteynerine veya verisine komut uygulanmadı; onun 1521 portu kullanılmadı.

Bağlantı parolaları Git tarafından dışlanan `db/oracle/.env.local` içindedir.
Bu yerel dosya ORACLE_PASSWORD, APP_USER=HESAPLAB ve APP_USER_PASSWORD anahtarlarını içerir.
HESAPLAB, Oracle bağlantı kullanıcısıdır; app_users içindeki demo müşteriler ve ADMIN ise uygulama kayıtlarıdır.

Proje kökünden başlatma ve veri alanını koruyarak durdurma:
```sh
docker compose -p hesaplab-oracle -f db/oracle/compose.yaml up -d
docker compose -p hesaplab-oracle -f db/oracle/compose.yaml stop
```

`01_schema.sql` ve ardından `02_seed.sql` SQL*Plus ile başarıyla uygulandı; COMMIT tamamlandı.
İlk kurulum dosyaları boş şemaya bir kez uygulanır; mevcut şemada tekrar çalıştırılmamalıdır.
`03_read_queries.sql` salt okunur sorgular içerir ve tekrar çalıştırılabilir.
Gerçek sorgular üç kullanıcı ve üç hesabı; 10000.00, 3500.00 ve 2000.00 TRY tutarlarını doğruladı.
React artık salt okunur Spring Boot API üzerinden bu Oracle kayıtlarını okur; statik demo listesi kullanılmaz. API tablo, bakiye veya hareket değiştirmez.

## Grafik arayüzü: VS Code içinde Oracle SQL Developer

Resmî `Oracle.sql-developer` 26.3.0 eklentisi kuruldu; `HesapLab` bağlantısı kaydedildi.
SQL Developer kenar çubuğunda `Connections → HesapLab → Tables` yolunu açın.
`APP_USERS` ve `ACCOUNTS` tablolarının `Data` sekmesi gerçek kayıtları, `Columns` sekmesi sütun yapısını gösterir.
Yönetici APP_USERS sütunlarını/üç kullanıcıyı ve ACCOUNTS içindeki üç hesabı grafik arayüzünde doğruladı.

ER görünümü için [hesaplab.dgm](diagrams/hesaplab.dgm) dosyasını VS Code'da açın; gerekirse `Fit to Screen` kullanın.
Dosya, bağlantı adı ve üç tablonun adları/yerleşimini tutar; SQL Developer sütunları ve kısıtları Oracle'dan okur.
7.2 ile native dosyaya `ACCOUNT_MOVEMENTS` eklendi. Yönetici kaydedilmemiş asıl diyagramı koruyup ayrı bir dosya kopyasında üç tabloyu, `FK_ACCOUNTS_OWNER` ve `FK_MOVEMENTS_ACCOUNT` çizgilerini görsel olarak doğruladı.
Genel yöntem bağlantıya sağ tık → `Open Diagram`; ardından `Tables` sürüklenirken bırakmadan Shift'e basmaktır.
Bu ortamda sürükleme ve boş diyagramdaki arama sonuç vermedi; nedeni belirlenmedi, dosya açma yöntemiyle görüntüleme sağlandı.
Kaynak: [Oracle — Working with Diagrams](https://docs.oracle.com/en/database/oracle/sql-developer-vscode/26.3/sqdnx/using-schema-diagrams.html).

Kaynak: [Oracle 19c CREATE TABLE](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-TABLE.html).

## Hesap hareketleri (7.2)
Bir hesap sıfır veya çok hareket içerir; her hareket `account_id` foreign key'iyle tek hesaba bağlıdır.
`06_account_movements_schema.sql` yalnız yeni tabloyu oluşturur; mevcut şema dosyasını yeniden çalıştırmayın.

| Sütun | Kural |
| --- | --- |
| id | NUMBER, GENERATED ALWAYS AS IDENTITY, primary key |
| account_id | NUMBER, zorunlu, accounts.id foreign key |
| direction | VARCHAR2(6 CHAR), zorunlu; CREDIT veya DEBIT |
| amount | NUMBER(15,2), zorunlu; sıfırdan büyük |
| balance_after | NUMBER(15,2), zorunlu; negatif olamaz |
| description | VARCHAR2(200 CHAR), isteğe bağlı |
| created_at | TIMESTAMP WITH TIME ZONE, varsayılan SYSTIMESTAMP, zorunlu |

`accounts.balance` güncel bakiyedir. `balance_after`, ilgili hareketin hemen sonrasındaki geçmiş bakiyedir.
CREDIT giriş, DEBIT çıkış yönüdür; `amount` iki durumda da pozitif tutulur.
Bakiye ile geçmiş kaydın tutarlılığı bu sütun kısıtlarıyla tek başına sağlanmaz.
İleride transferde bakiye değişiklikleri ve hareketler aynı Oracle transaction'ında işlenecek; bu görev transfer uygulamaz.

`07_account_movements_seed.sql`, mevcut pozitif bakiyelere karşılık birer **canlandırılmış** açılış CREDIT kaydı ekler.
Tutar ve hareket sonrası bakiye, mevcut hesap bakiyesinden alınır; açıklama “Demo açılış bakiyesi”dir.
Hareketi bulunan hesap atlanır; sıfır bakiyeli hesap için kayıt üretilmez. Mevcut kullanıcı/hesap/bakiyeler değiştirilmez.
Bu basit seed ardışık tekrar çalıştırma için kontrol içerir; eşzamanlı istek idempotency çözümü değildir.
`08_account_movements_queries.sql`, hareketi hesap/sahip/güncel bakiye ile birleştirir; hesap, zaman, hareket kimliği sırasını kullanır.

Geliştirici mevcut Oracle'da 06/07/08'i SQL*Plus ile uyguladı: üç açılış kaydı, doğru sahiplik ve 10000/3500/2000 doğrulandı.
Seed tekrarında 0 kayıt eklendi; toplam 3 kaldı. Geçersiz hesap ORA-02291; geçersiz yön, sıfır/negatif tutar ve negatif balance_after ORA-02290 ile reddedildi.
Denemeler savepoint/rollback ile temizlendi; son toplam yine 3, hesap bakiyeleri değişmedi. Kimlik dizisinde boşluklar oluşabilir.
Bu uygulama ve kontroller, Berk'in konuyu öğrendiği anlamına gelmez.

Yönetici 06/07/08 ve ilgili belgeleri ayrıca inceledi. 08 sorgusunu SQL*Plus ile çalıştırıp üç açılış kaydını, tutar/sahiplik/zaman alanlarını doğruladı; seed'in COMMIT içermeyen tekrarını savepoint/rollback ile kontrol etti: 0 ek kayıt, son toplam 3, çıkış 0. Metadata'da 7 sütun ve 11 etkin/doğrulanmış kısıt görüldü; geliştiricinin negatif denemeleri ayrıca tekrarlanmadı.
Geliştirici açık diyagramın kaydedilmemiş değişiklik uyarısında yeniden yüklemeyi iptal etmişti. Yönetici asıl sekmeyi koruyarak ayrı inceleme kopyasını açtı; üç tablo ve iki FK ilişkisi Fit to Screen ile görsel doğrulandı. Kullanıcının unsaved sorgusu ve asıl diyagramı korundu. Statik PNG hâlâ eski iki tabloyu gösterir.
Kaynak: [Oracle 19c SYSTIMESTAMP](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SYSTIMESTAMP.html).
## 13.1–13.4: tamamlanmış transfer ve replay
13_transfer_idempotency.sql bir kez uygulandı. UNIQUE(user_id,idempotency_key), anahtarı kullanıcı kapsamında benzersiz tutar. Kaynak/hedef FK'leri accounts'a, user_id app_users'a bağlıdır; gerçek sahiplik/rol backend kontrolüdür. amount NUMBER(15,2)>0, para TRY, kaynak/hedef farklıdır. Transfer kaydı ancak iki bakiye/iki hareketle birlikte commit olur; başarısız işlem kayıt bırakmaz. account_movements.transfer_id nullable FK: eski açılış kayıtları NULL, yeni transferin DEBIT/CREDIT çifti aynı transferi gösterir. Kaynak/hedef ikisi de accounts'a bağlıdır; Mermaid sadeleştirilmiş çizgisi iki FK'yi temsil eder. Eski dgm/PNG bu yeni tabloyu/sütunu içermez; güncel model bu SQL ve Mermaid'dir. Kimliklerde rollback sonrası boşluk normaldir. Anahtar arşivleme/süre sonu henüz yoktur. Yeni modelin hazırlanması öğrenme kanıtı değildir.

## 14.5–14.8: asenkron metin dekontu
`14_transfer_receipts.sql`, metadata'da tablo olmadığı görüldükten sonra ayrı bağlantıda bir kez uygulandı. Eski tablo/seed değiştirilmedi. Bir transferin sıfır veya bir dekontu olabilir: eski transferler için geriye dönük kayıt yok; yeni transferde dekont aynı transaction'da zorunlu oluşturulur.

| Sütun | Kural |
| --- | --- |
| id | NUMBER GENERATED ALWAYS AS IDENTITY, primary key |
| transfer_id | NUMBER NOT NULL; transfers.id foreign key ve UNIQUE |
| status | PENDING/READY/FAILED; varsayılan PENDING, zorunlu |
| content | CLOB; yalnız READY için zorunlu, diğer durumlarda NULL |
| error_message | VARCHAR2(200 CHAR); yalnız FAILED için zorunlu |
| created_at / updated_at | TIMESTAMP WITH TIME ZONE, SYSTIMESTAMP, zorunlu |

CHECK; READY için içerik ve hatasızlık, PENDING için boş içerik/hata, FAILED için boş içerik ve hata metni sağlar. Tabloda bakiye/hareket yoktur. Yeni dekont kaydı transfer transaction'ına katılır; UNIQUE/FK ikinci kaydı ve olmayan transferi engeller. Worker dekont satırını kilitler, Oracle'dan görünen adlarla metin üretir ve READY kaydeder; yinelenen işlem READY içerik/zamanını değiştirmez. Kaynak/alıcı adları hazırlanma anındaki görünen adlardır; yasal banka belgesi veya değişmez müşteri kimlik bilgisi iddiası yoktur.

Gönderici erişimi yalnız FK ile sağlanmaz: backend transfers.user_id=principal koşulunu SQL'de uygular. Alıcı bakiyesi/hareketi dekonta eklenmez. Mesaj sadece transfer kimliği taşır. Transfer commit'i ile mesaj yayını ayrı sistemlerdedir; outbox olmadığı için aradaki süreç kesintisinde dekont PENDING kalabilir. Manuel dekont isteği bu boşluğu toparlar; kayıpsız otomatik teslim garantisi yoktur.

Geliştirici:34 backend/10 worker testi, gerçek kapalı-worker ve kapalı-broker HTTP senaryosu, tekrar mesaj/istek, snapshot temizliği geçti. İzole iki transfer için4 hareket/2 dekont; eski satırlar aynı. UI aracı URL doğrulamasında durdu; UI/mobil kontrol yapılmadı. Ayrıntı ve kontrol kaynakları README'de. Yeni modelin hazırlanması öğrenme teyidi değildir. Eski dgm/PNG bu tabloyu içermez; güncel Mermaid/SQL modelini kullanın.

## 15.6–15.8: receipt_outbox
15_receipt_outbox.sql metadata kontrolü sonrası ayrı bağlantıda bir kez uygulandı. event_id VARCHAR2(36) PK, transfer_id FK, request_no NUMBER(10), event_type PREPARE_RECEIPT, status PENDING/SENT, attempts NUMBER(10), next_attempt_at/created_at/sent_at TIMESTAMP WITH TIME ZONE, last_error VARCHAR2(100) içerir. UNIQUE(transfer_id,request_no) ilk işi ve sonraki açık dekont isteklerini ayırır. Function-based UNIQUE yalnız PENDING transfer_id'yi tek tutar; due index(status,next_attempt_at,created_at) taramayı destekler. CHECK: request_no>=1, attempts>=0, olay/durum sabitleri ve SENT için sent_at zorunluluğu/hatasızlık.

İlk olay yeni transfer/dekontla aynı transaction'dadır; eski transfer/dekontlar için otomatik backfill yoktur. Eski bekleyen dekont kullanıcı isteğiyle olay alabilir. Sahiplik yine backend'de principal+SQL ile denetlenir. Olay kişisel veri, bakiye, parola veya oturum taşımaz; uygulama UUID'si kalıcı messageId'dir. Kalıcı claim alanı yok: relay satır kilidini publish/onay/SENT commit'ine kadar tutar, SKIP LOCKED diğer relay'i ayırır. Process/DB transaction kesilirse Oracle kilidi bırakır; olay PENDING kalır. SENT worker başarısı değildir; aynı olay birden çok teslim edilebilir. Worker READY sonucunu transfer kimliğiyle idempotent tutar.

Yeni test temizliği outbox → dekont/hareket → transfer → hesap → kullanıcı sırasındadır; outbox dahil tüm eski satır/sütunlar snapshot ile karşılaştırılır. Mevcut DB verileri değiştirilmez; identity boşlukları olağandır. DGM/PNG tarihsel kapsamındadır; güncel model SQL ve Mermaid'dir. Öğrenme teyidi yoktur.
