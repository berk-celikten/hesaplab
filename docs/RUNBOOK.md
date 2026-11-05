# HesapLab çalıştırma ve toparlanma rehberi

Bu rehber mevcut kuruluma aittir. Komutlar PowerShell içindir; uygulama gerçek para/banka bağlantısı içermez. Normal açılış şema/seed/migration/parola hazırlığı betiklerini çalıştırmaz.

## Ön koşullar ve parçalar
Docker Desktop açık ve motoru erişilebilir olmalı; Docker Desktop/bilgisayar bu prova sırasında yeniden başlatılmaz. Bu makinede JDK21.0.8, Node24.19.0/npm11.17.0, Docker29.9.2 doğrulandı. Maven3.9.11, repodaki Wrapper3.3.4 ile çalıştırılır; sistem Maven/PATH değişikliği gerekmez.

| Parça | Çalışma biçimi | Yerel adres / sağlık |
| --- | --- | --- |
| Oracle | hesaplab-oracle Docker container; kalıcı hesaplab-oradata volume | 127.0.0.1:1522/FREEPDB1; healthy |
| RabbitMQ | hesaplab-rabbitmq Docker container; kalıcı ayrı volume | AMQP127.0.0.1:5673; yönetim http://127.0.0.1:15673; healthy |
| Backend/relay | ayrı Java programı; backend/ çalışma dizini | http://127.0.0.1:8081/api/auth/csrf →200; anonim /api/auth/me →401 normal |
| Worker | ayrı Java programı; worker/ çalışma dizini; web portu yok | receipt ve demo kuyruklarında birer consumer |
| Frontend | ayrı Node/Vite programı; frontend/ çalışma dizini | http://127.0.0.1:5181 →200; /api proxy backend8081 |

Sırlar yalnız db/oracle/.env.local, infra/rabbitmq/.env.local; demo girişleri .tools/demo-logins.local.json içindedir. Değerleri terminale/loga/sohbete yazmayın. .tools PID/log dosyaları da yereldir.

## Her normal açılışta
Her terminalde kökü tanımlayın:
```powershell
$root = 'C:/Users/berkc/Documents/ChatGPT/1react Proje ÖZet/HesapLab'
$java = 'C:/Program Files/Android/openjdk/jdk-21.0.8/bin/java.exe'
Set-Location -LiteralPath $root
```
Önce zaten çalışan parçaları inceleyin:
```powershell
docker ps -a --filter name=hesaplab --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
Get-NetTCPConnection -State Listen -LocalPort 1522,5673,15673,8081,5181 -ErrorAction SilentlyContinue |
  Select-Object LocalAddress,LocalPort,OwningProcess
```
Port sahibi bilinmiyorsa onu kapatmayın. PID için Get-CimInstance Win32_Process -Filter "ProcessId=NUMARA" ile ExecutablePath/CommandLine okuyun; tam Java yolu, HesapLab JAR adı/proje yolu veya bu projenin Vite yolu eşleşmeli. Parola içerebilen başka süreçlerin komutlarını topluca dökmeyin.

### 1. Oracle
Mevcut container'ı açın (yoksa aşağıdaki ilk kurulum bölümünde durun):
```powershell
docker start hesaplab-oracle
docker inspect -f '{{.State.Health.Status}}' hesaplab-oracle
```
starting iken bekleyin; healthy olmadan backend'i açmayın. Otomatik bekleme, bu projede kullanılan aynı health alanını okur:
```powershell
function Wait-HesapLabHealthy([string]$name) {
  if ($name -notin @('hesaplab-oracle','hesaplab-rabbitmq')) { throw 'Hedef dışı container' }
  for ($i=0; $i -lt 90; $i++) {
    if ((docker inspect -f '{{.State.Health.Status}}' $name) -eq 'healthy') { return }
    Start-Sleep -Seconds 2
  }
  throw "Container healthy olmadı: $name"
}
Wait-HesapLabHealthy hesaplab-oracle
```

### 2. RabbitMQ ve policy
```powershell
docker start hesaplab-rabbitmq
Wait-HesapLabHealthy hesaplab-rabbitmq
./infra/rabbitmq/configure-receipt-dlq.ps1
docker exec hesaplab-rabbitmq rabbitmqctl list_policies -p hesaplab
```
Policy yalnız ^hesaplab\.receipt\.queue$ içindir; başka kuyruk/policy/volume değiştirilmez.

### 3. Backend (ayrı terminal)
```powershell
Set-Location -LiteralPath "$root/backend"
& $java -jar "$root/backend/target/hesaplab-backend-0.0.1-SNAPSHOT.jar"
```
Varsayılan relay açıktır. 8081 zaten bu projeye aitse ikinci kopya başlatmayın. Ayrı terminalden:
```powershell
(Invoke-WebRequest 'http://127.0.0.1:8081/api/auth/csrf').StatusCode
```
Beklenen200; CSRF gövdesi/oturum çerezi paylaşılmaz.

### 4. Worker (ayrı terminal)
```powershell
Set-Location -LiteralPath "$root/worker"
& $java -jar "$root/worker/target/hesaplab-worker-0.0.1-SNAPSHOT.jar"
```
send-demo profili normal açılışta kullanılmaz. Normal açılış yeni demo mesajı/transfer üretmez; varsa bekleyen gerçek outbox/dekont işi işlenebilir.

### 5. Frontend (ayrı terminal)
```powershell
Set-Location -LiteralPath "$root/frontend"
npm run dev -- --host 127.0.0.1 --port 5181 --strictPort
```
Prova arka planda aynı Vite girişini doğrudan node.exe ile başlattı; npm komutunun çalıştırdığı giriş aynıdır:
```powershell
& 'C:/Program Files/nodejs/node.exe' "$root/frontend/node_modules/vite/bin/vite.js" --host 127.0.0.1 --port 5181 --strictPort
```
Son kontrol, proje kökünden:
```powershell
(Invoke-WebRequest 'http://127.0.0.1:5181').StatusCode
docker exec hesaplab-rabbitmq rabbitmqctl list_queues -p hesaplab name durable messages_ready messages_unacknowledged consumers
```
Sakin durumda receipt.queue/demo.queue consumer1, DLQ consumer0; üç kuyrukta ready0/unacked0. İş varken sayılar değişebilir; sıfır görünmesi tek başına tüm dekontların READY olduğunu kanıtlamaz.

## İlk kurulum / yeniden derleme normal açılış değildir
Container/JAR/node_modules yoksa normal açılış komutunu tekrarlamak çözüm değildir. Önce README'deki ilgili kurulumu inceleyin. Yeni ortamda container oluşturmak için db/oracle/compose.yaml ve infra/rabbitmq/compose.yaml kullanılır; mevcut volume/container'ın üzerine körlemesine kurulum yapılmaz. Env dosyaları yetkili kullanıcı tarafından yerel hazırlanır.
- Frontend bağımlılığı kilit dosyasıyla npm ci; build/lint frontend dizinindedir.
- JDK terminal değişkeni $env:JAVA_HOME='C:/Program Files/Android/openjdk/jdk-21.0.8'; backend/worker dizinlerinde ./mvnw.cmd test package. Çalışan JAR'a yazmak için önce yalnız o uygulamayı durdurun.
- Gerçek Oracle testleri HESAPLAB_ORACLE_TEST=true gerektirir; normal scheduler/worker test kayıtlarına rakip olmamalı. Bunları her açılışta çalıştırmayın.
- SQL01–15 ve parola hazırlama profili bu makinede uygulanmıştır. Eksik migration yalnız metadata kontrolü ve ayrı bağlantıyla, açık görev kapsamında uygulanır. Hiçbiri normal başlangıç parçası değildir.
- down -v, volume/container silme, purge, toplu redrive veya Oracle schema/seed tekrarı bu rehberin parçası değildir.

## Güvenli durdurma ve arka plan programları
Yeni kullanıcı işlemi başlatmayın; aktif transfer varsa bitmesini bekleyin. Outbox PENDING/queue unacked durumunu kontrol edin. Normal terminalde frontend → worker → backend için Ctrl+C; ardından:
```powershell
docker stop hesaplab-rabbitmq
docker stop --time 60 hesaplab-oracle
```
Arka plan PID bilgisini tam alanlarla görmek için (yalnız backend rolü):
```powershell
$roleId = [int](Get-Content "$root/.tools/backend.pid")
Get-CimInstance Win32_Process -Filter "ProcessId=$roleId" |
  Format-List ProcessId,ExecutablePath,CommandLine
```

Arka planda başlatılmış bu makineye ait PID'ler .tools/backend.pid, .tools/worker.pid, .tools/frontend.pid içindedir; dosyadaki sayı tek başına yeterli kanıt değildir. ExecutablePath, CommandLine ve ilgili port sahibini karşılaştırın. Prova Windows Stop-Process kullandı; bu Ctrl+C/graceful shutdown eşdeğeri değildir. Sakin durumda yalnız kimliği doğrulanan bu üç PID sonlandırıldı. Stop-Process -Name java/node veya toplu container durdurma kullanmayın.

Provanın arka plan başlatma biçimi (proje kökünde; her rol için aynı kalıp, PID kayıtları Git dışında):
```powershell
$jar = Join-Path $root 'backend/target/hesaplab-backend-0.0.1-SNAPSHOT.jar'
$p = Start-Process -FilePath $java -ArgumentList @('-jar',('"' + $jar + '"')) -WorkingDirectory "$root/backend" -WindowStyle Hidden -RedirectStandardOutput "$root/.tools/backend.log" -RedirectStandardError "$root/.tools/backend-error.log" -PassThru
Set-Content -LiteralPath "$root/.tools/backend.pid" -Value $p.Id
```
Worker için aynı kalıpta backend yerine worker kullanılır; frontend aynı Node/Vite yoluyla çalışır. Başlatmadan önce port/PID kontrolü yapılır. Başka uygulamanın portunu boşaltmayın; strictPort başarısızsa önce doğru uygulamayı teşhis edin.

## Kısa sorun giderme
| Belirti | Kontrol / eylem |
| --- | --- |
| Oracle bağlantısı yok | Container healthy,1522 portu,FREEPDB1 ve yerel env dosyasının varlığı. Parolayı yazdırmadan kontrol edin; şema/seed çalıştırmayın. |
| 5181 açılmıyor | Node/Vite yolu ve port sahibi; frontend terminali/yerel log. Backend adresi5181 değildir. |
| /api erişilemiyor | Backend8081,frontend proxy; dev terminalinde HESAPLAB_API_TARGET farklı mı? Normal hedef8081. |
| Yeniden başlatmada giriş düştü/401 | Backend oturumu bellektedir; normal yeniden giriş yapın. Başka kullanıcıya ait hesap numarası404 olması beklenir. CSRF'siz POST403; giriş sonrası yeni token alınır. |
| PENDING outbox | Relay açık mı, Oracle/broker erişimi, next_attempt_at ve attempts; aralık2–60s,1s tarama. SQL tanı sorguları aşağıda; veriyi elle SENT yapmayın. |
| Outbox SENT,dekont PENDING | SENT yalnız broker onayıdır. Worker'ın receipt consumer'ı var mı, ana kuyruk/DLQ nasıl? READY ayrı Oracle işlemidir. |
| Dekont FAILED/DLQ | Consumer geçici hatayı3 kez/1–2s dener; kalıcıyı doğrudan DLQ'ya gönderir. Hazır sonucu bozmaz. README15.1–15.5 seçilmiş sentetik redrive ve receipt POST akışını kullanın; para transferini yeniden oluşturmayın. |
| Aynı iş tekrar teslim oldu | Outbox confirm→SENT veya worker commit→ack kesintisinde tekrar olabilir; sabit eventId ve transfer-id idempotency READY sonucunu korur. Exactly-once değildir. |

Oracle SQL Developer'da yalnız okuma için:
```sql
SELECT event_id, transfer_id, status, attempts, next_attempt_at, sent_at, last_error
FROM receipt_outbox ORDER BY created_at, event_id;
SELECT transfer_id, status, updated_at FROM transfer_receipts ORDER BY transfer_id;
```
Loglarda yalnız güvenli event/message/transfer kimlikleriyle takip edin; logun tamamını/CSRF/parolaları paylaşmayın.

## Kabul ve kapsam kanıtları
Ayrıntılı kayıtlar README'nin ilgili aşama başlıklarında ve LEARNING_PLAN'ın yönetici kayıtlarındadır. Bu tablo geçmiş kanıtları özetler; bugünkü prova51 testi yeniden çalıştırmaz.
| Alan | Mevcut kanıt/kaynak | Sınır |
| --- | --- | --- |
| Giriş/CUSTOMER/ADMIN | 11.1–11.4/11.8 geliştirici+yönetici: oturum,CSRF,bcrypt,401/403/404,fixation ve kendi hesap SQL filtresi; son backend10 Security testi | Demo girişleri; rol/parola değiştirme akışı yok. |
| Yönetim | 11.6–11.7 iki parça geliştirici+yönetici: güvenli listeler,CUSTOMER oluşturma,0 TRY hesap açma,iki ad düzenleme,unique409/parola sınırları | Silme/pasifleştirme/rol-parola değiştirme/hesap kapatma/bakiye düzenleme yok; tam yönetim değildir. |
| Transfer/sahiplik/kilit | 10.3–10.7/11.5 geliştirici+yönetici: kendi kaynak→CUSTOMER hedef,BigDecimal,Oracle transaction,iki hareket,sıralı hesap kilidi,paralel/rollback testleri | Gerçek para veya banka bağlantısı yok; üretim güvenlik/onay sistemi değildir. |
| Idempotency | 13.1–13.4 geliştirici+yönetici: kullanıcı+UUID UNIQUE,eşdeğer replay aynıid,farklı içerik409,eşzamanlı tek işlem | Tarayıcı anahtarı bellekte; yenileme/çıkışla kaybolabilir. |
| Hareket/dekont | 10.7/14.5–14.8 geliştirici+yönetici: kendi son50 hareket,gönderici dekont API/metin,UI PENDING/READY/FAILED,Tab-Enter/390px | Ekstre/PDF/e-posta yok; alıcının bakiye/hareketi dekontta yok. |
| Retry/DLQ | 15.1–15.5 geliştirici+yönetici:14 worker testi,gerçek broker tesliminde spy kontrollü2 geçici hata→3. denemede READY,tükenince tek DLQ,kalıcı ret,aynıid redrive | Hatalar spy kaynaklı; global3 deneme garantisi yok. Broker restart DLQ kalıcılığı geliştirici kanıtıdır; classic DLX kayıpsızlık garantisi vermez. |
| Outbox | 15.6–15.8 geliştirici+yönetici:37 backend+14 worker/package; Oracle rollback/replay/iki bağlantı SKIP LOCKED,gerçek broker kapalı HTTP200→otomatik SENT/READY | Confirm sonrası SENT hatası kontrollü SQL testidir. Confirm anında process kill yok. Gerçek PENDING işli backend restart geliştirici kanıtıdır. SENT≠READY; at-least-once sınırı vardır. |

15.10 açıklama yeterliği ve16 öğretim yönetici sohbetindedir. İşletim provası veya çalışan kod, Berk'in öğrendiği/mülakata hazır olduğu/projenin tamamlandığı anlamına gelmez.

## Bu turdaki prova kaydı
Bu kayıt gerçek komut/prova sonuçlarıdır; geçmiş arıza testlerinin yerine geçmez.

Prova 2026-11-02, geliştirici: önce Node5181 PID14984, backend PID16332 ve worker PID45952 için executable/komut/port sahipliği doğrulandı. Bu eski JAR süreçleri relative target yolu kullanıyordu; yerel PID kayıtları, tam JDK yolu, rol JAR adı ve backend port sahibi birlikte doğrulandı. Yeniden başlatılanlar absolute proje JAR/Vite yollarını kullanır. Docker Desktop/diğer projeler/sekme içerikleri yönetilmedi.

Frontend→worker→backend Windows PID sonlandırması; hesaplab-rabbitmq→hesaplab-oracle durdurması; ardından Oracle healthy→RabbitMQ healthy/policy→backend→worker→frontend açılışı gerçekten yapıldı. Oracle stop komutunda --time60 alias'ı kullanıldı; Docker29 bunu deprecated uyarısıyla kabul etti. Container/volume silinmedi; SQL/seed/migration/parola hazırlığı ve yeniden derleme yapılmadı. PID sonlandırması sakin durumda yapıldı; graceful Ctrl+C veya gerçek bilgisayar yeniden başlatması sınandığı iddia edilmez.

Önce/sonra ayrı JDBC salt-okuma snapshot'ı app_users/accounts/account_movements/transfers/transfer_receipts/receipt_outbox tablolarının tüm mevcut satır ve sütunlarını (CLOB içeriği ve hash sütunları dahil, değerleri yazdırmadan) karşılaştırdı. SHA256 satır parmak izleri tamamen aynı:3 kullanıcı/3 hesap/3 hareket/0 transfer/0 dekont/0 outbox. Yerel kontrol yardımcıları/fingerprint dosyaları .tools/ altında Git dışındadır.

Backend CSRF200, frontend200; Vite /api proxy üzerinden müşteri01 giriş→yalnız kendi2 hesabı (10000.00/3500.00), müşteri02→yalnız kendi1 hesabı (2000.00) doğrulandı. Düzeltilmiş smoke oturumları logout edildi. İlk smoke yardımcısının PowerShell JSON-dizi sayımı yanlış ret üretti; yalnız yardımcı betik düzeltildi ve aynı HTTP akışı/snapshot karşılaştırması başarıyla tamamlandı. Uygulama kodunda hata/düzeltme yoktu.

Son portlar localhost1522/5673/15673/8081/5181; Oracle/RabbitMQ healthy, receipt/demo consumer1, DLQ consumer0, üç kuyruk ready0/unacked0; tam-ad policy priority50 korundu. Yeni PID'ler backend41100,worker42912,frontend50512 olarak yerel PID dosyalarına yazıldı; bunlar gelecekte tekrar doğrulanmalıdır.

Bu turdaki giriş kontrolü HTTP/proxy smoke'udur; yeni görsel UI/Tab-Enter/mobil testi değildir. Önceki yönetici UI kanıtı yukarıdaki kabul tablosunda kaynaklandırılır.37 backend+14 worker, kontrollü hata/outbox-retry testleri yeniden çalıştırılmadı. Bu tur bekleyen iş yoktu; arıza/kilit/confirm yarışı veya yeni pending işli OS yeniden açılışı sınanmadı. Önceki gerçek backend restart ile kontrollü SENT SQL hatası kanıtları birbirinden ayrıdır.

Backend, worker, frontend, Oracle ve RabbitMQ açık bırakıldı. Teknik sonuç yönetici incelemesine sunulur;15.10 açıklama yeterliği/16 öğretim veya Berk adına öğrenme/mülakat teyidi verilmez. Commit/push yok.

Yönetici15.9: RUNBOOK/README/plan/STATE ve ilgili diff incelendi; uygulama kaynağı değişmedi. Rehberde Maven3.9.11 ile Wrapper3.3.4 ayrımı ve normal açılışta çalıştırılmayan şema/seed/migration betikleri netleştirildi. Canlı frontend HTTP200; Vite proxy üzerinden müşteri01 yalnız2 hesabı10000.00/3500.00,müşteri02 yalnız1 hesabı2000.00 aldı; özel smoke oturumları logout edildi. Yönetici salt-okuma smoke öncesi/sonrası altı tabloda eski9 kaydın bütün sütun fingerprint'ini eşleştirdi; değerler/parola/hash raporlanmadı. Oracle/RabbitMQ healthy,receipt/demo consumer1,DLQ consumer0,üç kuyruk ready0/unacked0 ve5181/8081 dinliyor. Özel parola taraması/diff geçti. Kapatma-açma provası geliştirici kanıtıdır; yönetici yeniden durdurma/test-build/migration/UI yapmadı. Teknik MVP'nin belgelenmiş kapsamı içinde yeni işlevsel bulgu yok; öğrenme ve mülakat teyidi verilmedi. Son belge değişiklikleri yerelde; commit/push yok. Sıradaki16.1 büyük resim öğretimi,15.10 açıklama kontrolü öğrenme turunda ele alınır.
