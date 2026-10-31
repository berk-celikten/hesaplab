# HesapLab — ayrıntılı öğrenme ve çalışma planı

## Güncel konum
- **6.1–6.2 güncel konum — Gerçek Oracle kayıtları ve grafik arayüzü:** Yönetici SQL/modeli inceledi, Docker ve mevcut imajla ayrı hesaplab-oracle/hesaplab-oradata kurdu. localhost:1522, HESAPLAB/FREEPDB1. DDL/altı INSERT/COMMIT ve gerçek SELECT geçti: 3 kullanıcı/3 hesap, doğru sahiplik/tutarlar. Resmî Oracle SQL Developer 26.3.0 VS Code'a kuruldu; APP_USERS Columns/Data ve ACCOUNTS Data doğrulandı. Sürükleme/arama başarısız oldu; docs/diagrams/hesaplab.dgm ile eklenti Oracle metadata'sını okuyup iki tabloyu ve FK_ACCOUNTS_OWNER bağlantısını çizdi, Fit to Screen sonrası görsel doğrulandı. SQL'den hesaplab-er.png ayrıca üretildi; resim canlı değildir. Docker tercihi/kurulum kolaylığı Berk tarafından henüz anlaşılmadı; örnekle açıklanacak. İlk boş ortamın ayar biçimi düzeltilirken yalnız yeni boş HesapLab kaynakları yeniden oluşturuldu; bankflow için komut uygulanmadı. Oracle AI Database 26ai Free 23.26.3.0.0; 19c sunucusu ayrıca denenmedi. Parolalar ignored yerel dosyada. Sonraki öğretim tablo/satır/sütun, sahiplik ve SELECT/WHERE; SQL ve React açıkları korunur.
- **5.4–5.5 — Asenkron çalışma sırasını öğrenme.** frontend/exercises/async-demo.js içinde Promise/setTimeout ve async runDemo örneği var. Gerçek paket komutu npm run demo; promptta demo:async istenmişti, mevcut demo adı paket/README ile tutarlı ve bu adla doğrulandı. Geliştirici kontrollerinden sonra yönetici JS/paket/README/diff ve npm run demo çıkış 0, build/lint/diff sonuçlarını doğruladı. Kendi ölçümünde (npm başlangıcı dahil) 1/2 mesajları 2,008s, son mesaj 5,020s anında geldi; fark yaklaşık 3,01s. React kaynaklarında alıştırma referansı yok; UI/mobil testi yapılmadı. VS Code'da dosya açıldı; terminal açılışı Restricted Mode güven ayarı istediği için iptal edildi, güven ayarı değiştirilmedi. Kod/çalıştırma engeli yok. İlk ders: await sonrası fonksiyon devamı beklerken çağıran bölümdeki kod ilerler. Yeni Promise/resolve/setTimeout/async/await kavramları henüz öğrenilmedi; önceki ad/kimlik ayrımı da tekrar edilecek.
- **5.6.2 güncel konum — İlk açılışta otomatik veri alma (geliştirici ve yönetici incelemesi geçti):** 5.6.1 geliştirici ve yönetici kontrollerinden geçti. Berk, VS Code'da App.jsx içindeki handleReloadAccounts gösterildikten sonra istek beklenirken isLoadingAccounts=true cevabını doğru verdi. Önceki zamanlama/catch/finally soruları da dar örneklerde doğruydu; genel yetkinlik veya bağımsız kod yazma varsayılmaz. Şimdi ilk verinin hazır demo listeden gösterilmesi yerine App açılışında servisten beklenmesi, useEffect/[] ve cleanup öğretilecek. StrictMode'un geliştirmede ek kurulum-temizleme döngüsü açıklanacak; önceki URL kimliği/ad açığı korunur.
- İlk etkileşim: iki bakiyeyi tek butonla gizleme/gösterme. State App'te; görünürlük props ile kartlara ulaşır.
- Bakiye alıştırmasını Berk yaptı: Vadesiz 10.000,00 TL; Birikim 3.500,00 TL.
- Component/props, temel veri akışı, JSX'te metin/değer ayrımı ve küçük JavaScript örnekleri cevaplarla kontrol edildi.
- Göster/gizle örneğinde state, olay ve props akışı küçük uygulama ve kendi sözleriyle açıklamayla doğrulandı.
- map'te tek hesap ve key için benzersiz kimlik gerekçesi temel sorularla kontrol edildi; kapsamlı JavaScript bilgisi varsayılmaz.
- find/filter nesne-liste farkı, ortak customerId ve seçimin asıl hesap listesini değiştirmediği örneklerle kontrol edildi; kapsamlı kod yetkinliği varsayılmaz.
- Güncel kısa durum ve kontrol sonuçlarının kaynağı STATE.md'dir. Bu plan numaraları sabit tutar.

## Takip ilkesi
Bir alt adım için **kod hazır**, **davranış kontrol edildi** ve **Berk'in anlayışı doğrulandı** ayrı durumlardır.
Kod birden fazla alt adımı içerebilir; öğretim bir seferde tek kavram ve tek soruyla ilerler.
Yanıt yanlışsa daha basit örnek kullanılır. Sadece “tamam” cevabı öğrenme kanıtı sayılmaz.
Gerekli bir önkoşula geri dönülürse nedenini ve dönülen adım numarasını açıkla.
Bu plan eğitim sırasını gösterir; bütün maddeleri tek bir geliştirici görevine dönüştürme.

## 1. Projenin temelini oluşturma
**Durum:** İlk ekranın kodu hazır. Bu aşamadaki her teknik terimin öğrenildiği varsayılmaz.
- **1.1 Kapsam:** Demo hesaplar, sistem içi transfer, hareketler ve yönetim ihtiyaçlarını belirle.
- **1.2 Kullanıcı/hesap/rol:** Kişiyi, kişiye ait hesabı ve kişinin yetkilerini ayır.
- **1.3 Roller:** CUSTOMER kendi hesaplarını kullanır; ADMIN kullanıcı ve hesap yönetir.
- **1.4 Kurulum:** Vite, React ve JavaScript projesinin nasıl çalıştırıldığını açıkla.
- **1.5 Parçalar:** App, Header, AccountList ve AccountCard görevlerini belirle.
- **1.6 Demo veri:** Aynı müşterinin vadesiz ve birikim hesaplarını göster.
- **1.7 Veri akışı:** App'ten listeye, listeden kartlara bilgi gönder.
- **1.8 GitHub:** Kaynak kod ile GitHub'daki sürüm arasındaki ilişkiyi açıkla.
- **1.9 Kontroller:** Geliştirici raporu ile yöneticinin kendi kontrollerini ayır.
- **1.10 Değerlendirme:** Çalışan ekranın, öğrenilmiş kodla aynı şey olmadığını takip et.

## 2. Mevcut ekranın kodunu anlama
**Durum:** Aşağıdaki temel örnekler çalışıldı; geniş JavaScript yetkinliği veya bütün liste işlemleri tamamlandı sayılmaz.
- **2.1 Gerekli JavaScript:** Değişken, fonksiyon, nesne ve dizi kavramlarını projedeki ihtiyaçla tanıt.
- **2.2 Ad/değer:** `customerName` gibi bir isimle o ismin taşıdığı metni ayır; farklı örneklerle kontrol et.
- **2.3 Nesne/dizi:** Tek hesabın alanlarını bir nesnede, hesapların bütününü dizide tutmayı açıkla.
- **2.4 Fonksiyon:** Bilgi alma, işi yapma ve sonuç döndürmeyi küçük bir örnekte göster.
- **2.5 Component:** Ekranın tekrar kullanılabilir parçasını kart örneğiyle anlat.
- **2.6 Görevler:** Dört component'in neden ayrı olduğunu ve her birinin sorumluluğunu göster.
- **2.7 JSX:** Etiketleri, sabit metni, `{name}` / `{balance}` ile değer göstermeyi ve className'in temel görevini ayır.
- **2.8 Props:** Bir component'e dışarıdan verilen bilgilerin nasıl alındığını açıkla.
- **2.9 Veri yolu:** App bütün diziyi AccountList'e verir; liste her kart için ilgili hesabın alanlarını aktarır.
- **2.10 Tekrar kullanım:** Şablon ortak olsa da her kartın aldığı hesap bilgilerinin ayrı olduğunu kontrol et.
- **2.11 Kullanıcı uygulaması:** Birikim bakiyesini 3.500,00 TL yapmayı Berk'in kendisine yaptır. Tamamlandı.
- **2.12 Sonuç:** Diğer hesabın değişmediğini ve beklenen değerin göründüğünü kontrol et. Tamamlandı.
- **2.13 Anlayış:** Kod değişikliği ve props akışı ilişkisini Berk'in kendi sözleriyle açıklamasını iste.
- **2.14 Değerlendirme:** Yalnızca cevabı ve uygulaması görülen bilgileri doğrulandı olarak kaydet.

## 3. State ve kullanıcı olayları
**Durum:** Göster/gizle kodu incelendi; temel örnekler, başlangıç alıştırması ve 3.7 akış açıklaması tamamlandı. Genel React yetkinliği varsayılmaz.
- **3.1 Sabit/değişen bilgi:** Kod dosyasını düzenlemekle kullanıcı tıklamasının ekranı değiştirmesini karşılaştır.
- **3.2 State:** React'in hatırladığı değişebilen bilgiyi anlat. `showBalances` tutar değil, görünürlük bilgisidir.
- **3.3 Güncelleme:** `useState(true)`, mevcut değer ve güncelleyen fonksiyonun görevlerini ayrı ayrı işle.
- **3.4 Olay:** `onClick={toggleBalances}` ile fonksiyonu bağlama ve tıklanınca çalışmasını göster.
- **3.5 Props/state:** App görünürlük durumunu yönetir; AccountList ve AccountCard bu bilgiyi props olarak alır.
- **3.6 Koşullu içerik:** Duruma göre bakiye veya noktalar; butonda gizle veya göster yazısı seçilir.
- **3.7 Anlayış ve uygulama:** Berk'in tıklama öncesi/sonrası durumu tahmin etmesini ve akışı açıklamasını iste.

### 3.7 uygulama sonucu ve açıklama
- Berk false başlangıç denemesinde noktaları gördüğünü bildirdi; tıklama sonrası state'in true olacağını doğru söyledi.
- Berk başlangıcı tekrar true yaptı. Yönetici App kaynağını okuyup sayfayı yenileyerek 10.000,00 TL / 3.500,00 TL görünümünü doğruladı.
- Bakiye verileri korundu; varsayılan görünür başlangıç geri getirildi.
- Yönetici yalnızca dosyayı açıp ilgili yeri seçti; uygulama kodu değişikliğini Berk yaptı.
- Berk görünürlüğün true'dan false'a değişmesini, props ile AccountList/AccountCard'a ulaşmasını ve gösterimin değişmesini kendi sözleriyle açıkladı.

### 3. adımın kodu okuma sırası
1. Önce butonu kullanarak sonucu gözle: tutarlar değişmeden yalnızca gösterim değişir.
2. App'teki `showBalances` değerinin neyi temsil ettiğini açıkla; `true` / `false` anlamını öğret.
3. `useState(true)` içindeki başlangıç değerini açıkla.
4. `setShowBalances` ile durumun güncellendiğini göster; normal değişken atamasıyla aynı olmadığını öğret.
5. `toggleBalances` fonksiyonuna geç. `currentValue` ve `!` kavramlarını ayrı küçük örneklerle işle.
6. Butondaki `onClick` bağlantısını göster. Fonksiyonu vermek ile hemen çalıştırmayı ayrı değerlendir.
7. `showBalances` prop'unun App → AccountList → AccountCard yolunu takip et.
8. Karttaki `koşul ? doğruysa : yanlışsa` ifadesini gerçek kod üzerinden açıkla.
9. Buton metninin aynı state'e göre değiştiğini göster.
10. Sayfa yenilenince neden başlangıca dönüldüğünü, kalıcı saklama eklenmeden anlat.
11. Tek tek sorular sor; bütün bu maddelerin aynı anda öğrenildiğini varsayma.

### Bu görevde incelenen kaynaklar ve sonuç
- App: tek state, başlangıç `true`, önceki değeri tersine çeviren güncelleme, buton ve props gönderimi.
- AccountList: görünürlük bilgisini alıp her AccountCard'a iletir; burada ikinci bir görünürlük state'i yoktur.
- AccountCard: gelen görünürlük bilgisine göre tutarı veya `••••••` gösterir; hesap verisini değiştirmez.
- index.css: mevcut tasarıma eklenen buton, hover ve klavye odağı görünümü.
- Yönetici build/lint'i çalıştırdı; ikisi başarılı. Masaüstü ve dar ekran akışını tekrar doğruladı.
- İki bakiye gizlendi ve doğru tutarlarla geri geldi; gizliyken yenileme görünür başlangıca döndü.
- Enter ile buton çalıştı; 390px genişlikte yatay taşma görülmedi.
- Bu kapsamda düzeltme gerektiren bulgu yok. Özelliğin yazılmış olması öğrenildiği anlamına gelmez.

## 4. Listeler, formlar ve birden fazla demo müşteri
**Durum:** map/key ve find/filter temel örnekleri kontrol edildi. Berk 4.5–4.6 temel sorularını cevapladı ve ekran alıştırmasını yaptı. 4.7 ilk arama alanı kodu hazır; yöneticinin kaynak/diff, build/lint ve masaüstü/mobil davranış kontrollerinden geçti. Önceki müşteri seçimi/gizlilik kontrolleri de mevcut kodda tamamlandı. Yeni arama kodunun kullanıcı anlayışı henüz doğrulanmadı.
- **4.1 Listeyi inceleme:** Berk iki/üç hesaplı varsayımlarda liste ve kart sayısını daha önce doğru açıkladı; temel ilişki kontrol edildi.
- **4.2 map:** Berk account'un tek hesap olduğunu ve account.balance'ın o hesabın bakiyesini verdiğini doğru açıkladı.
- **4.3 key:** Berk id'nin hesaba özgü olduğunu, bakiyenin başka hesaplarla aynı olabileceğini doğru açıkladı.
- **4.4 Bulma/süzme:** Berk find sonucunun tek nesne, filter sonucunun dizi olduğunu ve tüm müşteri hesapları için filter gerektiğini doğru açıkladı.
- **4.5 Demo müşteriler:** Ortak customerId, seçilen kimliğin state'te tutulması, customer-02 için bir hesap süzülmesi ve asıl listede üç hesabın korunması cevaplarla kontrol edildi. Nesnenin tamamı ile .name ayrımı düzeltildi; .id için customer-02 cevabı doğruydu.
- **4.6 Seçim/giriş:** Berk demo seçim ile gerçek giriş farkını açıkladı. Gizliyken müşteri değişiminde noktaları ve gösterince seçimin korunduğunu gördüğünü bildirdi. Farklı setter'lar ve React'in diğer state'i koruması açıklandı; müşteri butonuna setShowBalances(true) eklense bakiyelerin görüneceğini doğru tahmin etti. Bu temel örneklerin ötesinde yetkinlik varsayılmaz; Berk'in gözlemleri bağımsız yönetici kontrolü değildir.
- **4.7 Formlar:** Arama/state/props ve başlangıç kod alıştırması çalışıldı; metin/boolean/boşluk, ad/value ve yenileme ayrımları tekrar gerektiriyor. Select ve tutar kodları yönetici kontrollerinden geçti. Berk tutar/arama state ayrımını küçük örnekle doğru cevapladı. Farklı müşteride tutarın boşaldığını, ardından aynı müşteride de boşaldığını bildirdi. Yönetici son bildirimi tekrarlayamadı: kontrollü 02/300/tekrar02 denemesinde 300 kaldı; 01'e geçince boşaldı. Kod değiştirilmedi. Öğrenmeyi 4.8'in somut form örneğinde sürdür; bu gözlemler genel yetkinlik sayılmaz.
- **4.8 Transfer arayüzü:** Kaynak/hedef/tutar tek formda; onSubmit/preventDefault ve transferDraft nesnesiyle yalnız önizleme var. Kaynak seçili müşterinin hesapları; hedef kaynak dışındaki bütün demo hesaplar. Etikette müşteri/hesap adı, value'da id bulunur. Boş seçimler güvenli açıklamalarla gösterilir; doğrulama 4.9'da. Form alanı veya farklı müşteri değişince taslak temizlenir; aynı müşteri/arama/gizlilikte korunur. Kod geliştirici ve yönetici kontrollerinden geçti. Berk buton gerekliliğini, yenilemede boş tutarı ve yalnız taslak oluşmasını doğru açıkladı; yöneticinin 125,50→200 gösteriminden sonra kendi buton alıştırmasında 200 bildirdi. Genel state/form yetkinliği varsayılmaz. Gerçek transfer ve Oracle transaction entegrasyon aşamasında.
- **4.9 Doğrulama:** Boş ve geçersiz girişleri kullanıcıya açıkça göster.
  - **4.9.1 Zorunlu alanlar:** Kaynak/hedef boşsa veya amountText.trim() boşsa gönderimde alan hataları göster, taslak oluşturmayı durdur. Alan değişiminde bütün eski hataları temizle; farklı müşteri hataları/formu/taslağı sıfırlar, aynı müşteri/arama/gizlilik korur. İlk açılışta hata gösterme; dolu tutarı ham metin olarak koru. Yalnız boş alan kontrolü kodu geliştirici ve bağımsız yönetici kontrollerinden geçti: üç/tek hata, boşluk, 125,50/0/abc, temizleme/çakışma/koruma/reset/yenileme, bakiyeler, aria bağlantıları, klavye/1280-390px ve taşma doğrulandı. Kapsam içinde bulgu yok. Berk erken return varken taslak oluşmayacağını doğru cevapladı; yalnız kaynak seçilip yeniden gönderilince hedef/tutar uyarılarının kalacağını bildirdi. Genel hata state/form yetkinliği varsayılmaz.
  - **4.9.2 Tutarın geçerliliği:** Doğrulama için kırpılmış yerel kopyada tüm metnin biçimini denetle; ardından virgülü noktaya çevirip Number ile sonlu ve sıfırdan büyük olduğunu kontrol et. Başta rakam, isteğe bağlı tek ondalık ayırıcı ve ardından bir/iki rakam kabul et; binlik ayırıcı, işaret ve üslü gösterim kabul etme. Ham amountText/state/taslak değişmesin; yeni sayısal state veya bakiye hesabı ekleme. Boş alanların mesajı ve önceki hata/koruma/reset akışı korunsun. Kod geliştirici ve bağımsız yönetici kontrollerinden geçti; kaynak/diff/build/lint, 20 girdi/ham metin/sonluluk, birlikte hata/düzeltme/koruma/reset/yenileme, aria/klavye/1280-390px ve taşma doğrulandı; bulgu yok. Berk trim'in state'i değiştirdiğini söyledi; açıklamadan sonra boşluksuz değişken adını trimmedAmount diye doğru seçti. Number'ın metni sayıya çevirdiğini doğru açıkladı; sıfırın sayı olarak geçerli olduğu ve pozitifliğin proje kuralı olduğu öğretildi. Bu cevaplar kopya/biçim/sonluluk konusunda genel yetkinlik sayılmaz; tekrar edilecek.
- **4.10 Değerlendirme:** Berk liste üretimini ve form verilerinin nerede tutulduğunu açıklayabilsin. İlk sahiplik sorusuna targetAccountId/transferDraft/formErrors satırlarını gönderdi; App sahipliği ve alan/taslak/hata ayrımı açıklandı. Yazarken amountText'in güncellendiğini doğru cevapladı. 5.2'de gezinmede 200'ü koruyan component için App cevabı doğruydu. Bu somut örnekler bütün state/props kodunda genel yetkinlik sayılmaz; gerektiğinde tekrar edilecek.

### 4.5–4.6 geliştirici görevinin sınırı
- İki demo müşteri arasında normal butonlarla seçim yapılacak; gerçek giriş ve yetkilendirme 11. aşamada.
- customer-01 mevcut checking/savings hesaplarının sahibi kalacak; bakiyeler 10.000,00 TL / 3.500,00 TL korunacak.
- customer-02 için checking-02 kimlikli 2.000,00 TL bakiyeli tek demo hesap eklenecek.
- App'te seçili müşteri kimliği state olacak; müşteri find, hesapları filter ile hesaplanacak.
- Seçilen ad, hesap sayısı, kart sahibi ve hesap listesinin erişilebilir etiketi aynı müşteriye göre gösterilecek.
- Mevcut görünürlük tercihi müşteri değişiminde korunacak. Yenileme customer-01 ve görünür bakiyelerle başlayacak.
- Form, gerçek giriş, rol, transfer, API, backend, yeni bağımlılık veya alt ajan bu görevin kapsamında değil.
- README'nin eski 3.000,00 TL ve state yok açıklamaları güncel kodu anlatacak biçimde düzeltilecek.
- Geliştirici raporu geldiğinde ilgili kod ve diff okunup uygun kontroller tekrarlanacak; kod yazılınca öğrenilmiş sayılmayacak.

## 5. Routing ve asenkron veri alma
**Durum:** 5.1–5.2, 5.3 ve ilk 5.4–5.5 kodları geliştirici ve bağımsız yönetici kontrollerinden geçti. /transfer/200/App örnekleri kullanıcıyla kontrol edildi; genel yetkinlik varsayılmaz. Ad/kimlik ayrımı tekrar gerektiriyor. Terminal alıştırmasının gerçek 1→2→3 sırası ve yaklaşık üç saniyelik farkı yönetici tarafından da doğrulandı; yeni asenkron kavramlar henüz öğrenilmedi.
- **5.1 Ekranlar:** Hesaplarım, transfer ve hareketlerin sorumluluklarını belirle.
- **5.2 Routing:** Adres ile gösterilen ekranın ilişkisini öğret. Berk menü geçişinde transfer yolunu, dönünce 200'ü ve state sahibi App'i doğru bildirdi. SPA mantığını kendi sorusunda tarif etti; aynı HTML/çalışan App ve değişen sayfa component'i açıklandı. F5 sonrası başlangıca dönme açıklandı; bu kullanıcı alıştırması henüz doğrulanmadı.
  - **5.1–5.2 ilk görev sınırı:** React Router declarative kurulumuyla /hesaplar ve /transfer; / başlangıcı /hesaplar'a yönlenir, bilinmeyen adreste anlaşılır sayfa gösterilir. Hesaplar ve transfer formu ayrı page component'lerine taşınır; ortak demo müşteri/gizlilik kontrolleri, state'ler ve handler'lar App'te kalır. Header bağlantıları sayfa yenilemeden geçiş yapar; gezinme/geri-ileri değerleri korur, doğrudan açma/yenileme başlangıcı yükler. Hareket ekranı, hesap ayrıntısı, gerçek transfer, veri alma, backend veya auth bu görevde yok.
- **5.3 Seçili hesap:** Ayrıntı ekranının hangi hesaba ait olduğunu taşı.
  - **Öğrenme notu:** Berk /hesaplar/savings için accountId cevabını Birikim Hesabı diye verdi. Adın Birikim Hesabı, kimliğin savings olduğu ve Link→URL→useParams→customerAccounts.find akışı gerçek bağlantı açılarak öğretildi. Yeni akış bağımsız doğru cevapla henüz doğrulanmadı.
  - **5.3 ilk görev sınırı:** /hesaplar/:accountId, AccountDetailsPage ve listeden Link ile ayrıntıya gitme. useParams kimliği okur; hesap customerAccounts.find ile bulunur, yeni state'e kopyalanmaz. Ayrıntıda ad/sahip/bakiye ve gizlilik korunur. Bilinmeyen/diğer müşterinin hesabında aynı açıklama ve /hesaplar dönüşü gösterilir. URL müşteriyi otomatik seçmez; yenilemede customer-01 başlangıcı uygulanır. Mevcut state'ler App'te kalır; ayrıntıya gidip gelme arama/form/hata/taslağı korur. API/asenkron veri/gerçek yetkilendirme veya yeni hesap verisi eklenmez. Geliştirici ve bağımsız yönetici kontrolleri geçti: kaynak/diff/build/lint, üç hesap/sahiplik/yenileme/gizlilik, arama-geri/ileri, form-taslak-hata, aktif menü/Tab-Enter/1280-390px ve taşma doğrulandı. İşlevsel bulgu yok; README'nin veri sorumluluğu ifadesi düzeltildi. Yeni parametre kodu öğrenilmiş sayılmaz.
- **5.4 Asenkron mantık:** Sonuç beklenirken uygulamanın diğer işlere devam edebilmesini göster.
- **5.5 Promise/async/await:** Bekleme, sonuç ve hata ilişkisini küçük örneklerle işle.
  - **5.4–5.5 ilk görev sınırı:** frontend/exercises/async-demo.js içinde getDemoMessage ve async runDemo; 3000ms setTimeout ile çözülen Promise. İlk mesaj runDemo içinde await öncesinde, ikinci mesaj çağıran bölümde runDemo çağrısından hemen sonra, son mesaj await sonrasında yazılır. Gerçek komut npm run demo (prompttaki demo:async yerine); paket ve README tutarlı. Geliştirici ve yönetici kod/çıktı/build/lint/diff kontrollerini yaptı; yönetici ölçümünde ilk iki mesaj aynı 2,008s anında, son mesaj 5,020s anında geldi (npm dahil). React'e import/yeni bağımlılık/API/worker/ret senaryosu yok; UI testi yapılmadı. VS Code terminali güven ayarı değiştirilmeden açılmadı; dosya VS Code'da, çıktı ayrı çalıştırmayla gösteriliyor. Promise/resolve/setTimeout/async/await henüz öğrenilmedi.
  - **5.4–5.5 ikinci görev sınırı (geliştirici ve yönetici kontrolleri geçti):** frontend/exercises/event-loop-demo.js içinde ilk mesaj, 0ms setTimeout içine son mesaj, çağrıdan sonra ikinci mesaj; npm run demo:loop ile 1→2→3 sırası. Mevcut async-demo.js ve npm run demo korunur. Yeni örnekte Promise/async/await, worker, yoğun döngü, React, ağ veya bağımlılık eklenmez. Amaç callback'in hemen çağrılmadığını ve mevcut senkron koddan sonra çalıştığını göstermek; bu kod ikinci JavaScript iş parçacığı açmaz. Sıfır gecikme tam zamanlama garantisi değildir. Geliştirici PowerShell terminal aracında iki komutu gerçekten çalıştırdı: 1→2→3 ve çıkış 0; build/lint/diff de 0. Yönetici kaynak/paket/README/ilgili diff ve planı okuyup iki komut/build/lint/diff başarısını bağımsız doğruladı; kapsam içinde bulgu yok. Güven ayarları değiştirilmedi; UI testi yapılmadı. Tek soruluk öğrenme kontrolü sırada; event loop/iş parçacığı öğrenilmiş sayılmıyor. Önceki URL kimliği/ad ayrımı açığı korunur.
  - **5.4–5.5 ikinci alıştırmanın öğrenme kontrolü:** Berk 2. mesajın önce gelmesini, zamanlayıcının fonksiyonunun daha sonra çalıştırılmak üzere zamanlanmasıyla doğru açıkladı. Bu dar örnek doğru; genel event loop/iş parçacığı yetkinliği varsayılmaz.
  - **5.5 üçüncü görev sınırı (geliştirici ve yönetici kontrolleri geçti):** Ayrı frontend/exercises/async-error-demo.js ve npm run demo:error. getDemoMessage, Promise'ı 1000ms sonra reject(new Error('Demo bilgi alınamadı.')) ile reddeder; kullanılmayan ilk executor parametresi _resolve. async runDemo önce 1. mesajı yazar; try içinde await ve yalnız başarıda yazılabilecek sonuç mesajı, catch içinde error.message, finally içinde beklemenin sona erdiği mesajı bulunur. Çağıran bölüm runDemo'dan hemen sonra 2. mesajı yazar. Gerçek çıktı iki tarafça 1→2→3. Hata→4. Bekleme sona erdi olarak doğrulandı; başarı mesajı yazılmaz, hata karşılandığı için komut çıkışı 0. Yönetici üç JS/paket/README/plan/ilgili diff incelemesini ve üç demo/build/lint/diff kontrollerini bağımsız yaptı; hepsi çıkış 0, kapsam içinde bulgu yok. Mevcut iki örnek ve bağımlılık sürümleri korundu; React kaynaklarında alıştırma referansı yok. Güven ayarları değişmedi; UI testi yapılmadı. Tek soruluk öğrenme kontrolü sırada; hata araçları henüz öğrenilmedi.
- **5.6 Bekleme/boş sonuç:** Henüz gelmemiş veri ile boş listeyi ayır.
  - **5.5 üçüncü alıştırmanın öğrenme kontrolü:** Berk başarı mesajının atlanmasını hata gelmesi ve catch'in çalışmasıyla doğru açıkladı. Başarı durumunda da finally çalışacağını doğru söyledi. Genel hata yönetimi veya bağımsız kod yazma yetkinliği varsayılmaz.
  - **5.6.1 ilk görev sınırı (geliştirici ve bağımsız yönetici kontrolleri geçti):** services/accountService.js aynı üç demo hesabını tek yerde tutar; demoAccounts başlangıç verisi, getDemoAccounts yaklaşık 1000ms sonra yeni nesneler içeren kopya listeyle çözülen Promise. App'te accounts başlangıçta demoAccounts, isLoadingAccounts false; async handleReloadAccounts isteği butonla başlatır, await sonucunu setAccounts'a verir, try/finally ile yüklenme biter. Hesaplarım butonu yüklenirken disabled; beklemede kart/sayısal sonuç/boş mesajı yerine erişilebilir durum gösterilir. Tamamlanınca seçili müşteri ve mevcut arama/gizlilikten sonuç türetilir; boş müşteri ve boş arama ayrılır. İstek tüm müşterilerin verisini getirir; müşteri değişimi veya sayfa gezinmesi isteği yeniden başlatmaz, eski müşteriyi geri seçmez ve form/arama/gizliliği sıfırlamaz. Ayrıntı ve transfer aynı App hesap state'ini kullanır; yenileme sırasında önceki veri state'te tutulur. Otomatik ilk yükleme/useEffect, ağ, hata simülasyonu/retry, yeni müşteri/bağımlılık veya gerçek transfer eklenmez. Geliştirici build/lint/diff/üç demo, UI koruma/ayrıntı/tutar/reset/klavye/1280-390px ve Node kopya kontrollerini yaptı; geçici [] UI kontrolünden sonra normal üç hesap geri getirildi. Yönetici servis/App/AccountsPage/CSS/README/plan/ilgili diff'i okudu; build/lint/diff/üç demo çıkış 0 ve Node ile ayrı liste/nesneler, sonuç kopyasını değiştirince kaynak/sonraki cevabın korunmasını doğruladı. UI başlangıç/yükleme/disabled/kart-sayı-boş mesajı gizleme, Türkçe arama/gizlilik ve beklerken 01→02 seçimi, beklerken gezinme ve ham 125,50 form-taslağı/0 hatası koruması, savings ayrıntısı/üç tutar/yenileme/Tab-Enter/1280-390px ve yatay taşma kontrol edildi; bulgu yok, tarayıcı hata kaydı boştu. [] UI yönetici tarafından tekrar çalıştırılmadı; boş müşteri/arama koşulları kaynakta incelendi. Geçici ekran boyutu geri alındı ve test state'i yenilemeyle temizlendi. Yeni React/asenkron akışı ve bağımsız kod yazma öğrenilmiş sayılmaz.
  - **5.6.1 öğrenme kontrolü:** İlk VS Code açma komutu kullanıcıya görünürlüğü doğrulamadı; @oai/sky ile doğru pencere seçilip öne getirildi, App.jsx/handleReloadAccounts ve 46–55 satırları görsel olarak doğrulandı. Berk istek sürerken isLoadingAccounts=true cevabını doğru verdi. Yeni akışın tamamını bağımsız kurabildiği varsayılmaz.
  - **5.6.2 görev sınırı (kod hazır, geliştirici kontrolü tamam):** App accounts=[]/isLoadingAccounts=true ile başlar; demoAccounts başlangıç importu kaldırılır, servis ve üç hesap korunur. App üst düzey useEffect([]) içinde yerel async ilk-yükleme fonksiyonu getDemoAccounts'ı await eder. Cleanup yerel ignore bayrağını değiştirir; eski kurulumun hem setAccounts hem finally/loading güncellemesi engellenir. Bu yöntem zamanlayıcı/isteği iptal etmez, sonucunu kullanmaz. Effect callback'i async yapılmaz; kararsız handleReloadAccounts bağımlılık gizleyerek çağrılmaz, state değiştiren ilk çağrı render gövdesine konmaz. StrictMode korunur; geliştirmede ek setup→cleanup→setup olabilir, useRef ile atlama eklenmez ve üretimde her zaman tek çağrı olduğu genellemesi yapılmaz. Menü/müşteri/arama/gizlilik değişimi ilk isteği yeniden başlatmaz; kullanıcı seçimleri tamamlanmada sıfırlanmaz. AccountsPage zaten yüklenmeyi gösterir; veri henüz yokken ayrıntı ve transfer de yüklenme mesajı gösterir, yanlış bulunamadı/boş form göstermez. Bu bekleme accounts.length===0 && isLoadingAccounts ile türetilebilir; ek state gerekmez. Veri varsa manuel yenilemede ayrıntı/transfer mevcut veriyi korur; 404 geciktirilmez. Boş servis cevabı loading=false olduktan sonra boş sonuç olur; geçici [] UI kontrolünden sonra normal üç hesap geri getirilir. Ağ/hata-retry/gerçek transfer/yeni bağımlılık veya soyutlama eklenmez. Geliştirici build/lint/diff/üç demo çıkış 0; doğrudan hesaplar-ayrıntı-transfer/yenileme, ilk seçimler/gezinme, 404, manuel koruma, klavye/1280-390px kontrollerini yaptı. Geçici [] cevapla bekleme/boş ayrımı doğrulandı, üç hesap geri getirildi. Geçici console kayıtlarıyla StrictMode setup→cleanup→setup, eski sonuç/finally ignore=true ve tercihlerde yeni setup olmaması görüldü; kayıtlar kaldırıldı. Yönetici yeni App/ilgili diff/README incelemesi ve build/lint/diff çıkış 0 sonucunu doğruladı. UI ilk yükleme, beklerken 02 seçimi ve transfer beklemesi, sonuçta checking-02 seçenekleri, doğrudan savings bekleme ve 3.500 TL doğrulandı; bulgu yok. Üç demo, mobil, geçici boş cevap ve StrictMode kayıtları bu tur tekrar çalıştırılmadı; geliştirici kontrolü olarak kalır. Berk kısa/hedefli çalışma istedi; gereksiz tekrar azaltılacak. Otomatik ilk çağrı kısa anlatılıp tek soruyla kontrol edilecek. Yeni Effect kavramları öğrenilmiş sayılmaz.
- **5.7 Hata/deneme:** Veri alınamazsa anlaşılır sonuç ve uygun tekrar davranışı belirle.
  - **5.7.1 görev sınırı (kod hazır, geliştirici kontrolü tamam):** getDemoAccounts(shouldFail=false), yaklaşık 1000ms sonunda kontrollü reject(Error) veya mevcut kopya liste; rastgelelik/ağ yok. İlk yükleme normal başarılı kalır. App accountsError metni ve hem Effect hem manuel çağrıda catch/finally; Effect ignore koruması catch'te de sürer. Yeni denemede eski hata temizlenir; başarısızlıkta önceki hesaplar ve form/arama/gizlilik korunur. Hesaplarım'daki Demo hatasını dene düğmesi yalnız başarısız çağrı başlatır; normal yükleme/Tekrar dene başarı yolunu çağırır, DOM event'i shouldFail parametresine aktarılmaz. Hata erişilebilir açıklama ve Tekrar dene ile gösterilir; veri yokken boş hesap/bulunamadı/form gösterilmez, veri varsa önceki veri gösterilir. Yükleme boyunca ilgili butonlar disabled olur. 404/StrictMode/cleanup/üç tutar ve terminal alıştırmaları korunur; yeni bağımlılık/büyük soyutlama yok. İlk hata yolu geçici getDemoAccounts(true) ile kontrol edilip normal ilk çağrıya dönülebilir; son App diskten tekrar doğrulanır. Geliştirici build/lint/diff, başarı→demo hata→retry, ilk hatada üç sayfa/yanlış boş mesajı olmaması, eski veri/tercih/taslak koruması ve klavye/390px kontrollerini yaptı. Geçici ilk getDemoAccounts(true) kaldırılıp normal çağrı geri getirildi; son App diskten yeniden okundu. Değişmeyen terminal alıştırmaları tekrar edilmedi; yönetici incelemesi bekleniyor. Yeni hata UI akışı öğrenilmiş sayılmaz.
- **5.8 Değerlendirme:** Bekleme, başarı, boş ve hata durumlarını açıklat.

## 6. Oracle tablo tasarımı ve temel SQL
**Durum:** SQL/model hazır; yerel Oracle 26ai Free üzerinde uygulanıp sorgulandı. 6.1–6.7 öğrenimi tamamlanmış sayılmıyor.
- **6.1 Bilgiler:** Kullanıcı, hesap, transfer ve hareket ihtiyaçlarını belirle.
  - **6.1 ilk çalışma paketi (Oracle üzerinde uygulandı):** app_users/accounts; identity PK, unique username, CUSTOMER/ADMIN check, sahip FK, NUMBER(15,2)/negatif olmayan bakiye, TRY. Geliştirici statik kontrol yaptı; yönetici ayrı yerel Oracle ile DDL/altı INSERT/COMMIT/SELECT çalıştırdı. Üç kullanıcı/üç hesap, sahiplik/tutarlar doğrulandı. compose.yaml ve 03_read_queries.sql eklendi; ortam/bağlantı bilgileri DATABASE_MODEL içinde. 19c sunucusu ve negatif kısıt testleri ayrıca yapılmadı. FK rolü garanti etmez; React halen demo servisini kullanır. Çalıştırma öğrenme anlamına gelmez.
- **6.2 Tablo yapısı:** Tablo, satır ve sütunu hesap örneğiyle öğret.
- **6.3 Kimlikler:** Kayıt kimlikleri ve birincil anahtarı açıkla.
- **6.4 Sahiplik:** Kullanıcı-hesap ilişkisini ve yabancı anahtarı kur. Normalizasyonu kullanıcı bilgisini her hesapta tekrar tutma/güncelleme tutarsızlığı örneğiyle tanıt; owner_user_id tekrarının gerekli ilişki bilgisi olduğunu ayır. Ayrıntılı normal formlar başlangıç açıklamasıyla öğrenilmiş sayılmaz.
- **6.5 Veri türleri:** Para, tarih ve metin alanlarını seç; hesaplama ile ekran biçimini ayır.
- **6.6 Kısıtlar:** Boşluk, tekrar ve ilişki kurallarını veritabanında uygula.
- **6.7 Demo veri:** Sorguları sınamak için küçük ve anlaşılır kayıtlar oluştur.
- **6.8 SQL:** SELECT, WHERE, ORDER BY, INSERT, UPDATE ve DELETE işlemlerini çalış.
  - **6.8.1 alıştırmalar hazır (geliştirici/yönetici kontrolü geçti, öğrenme doğrulanmadı):** db/oracle/04_select_exercises.sql içinde id/owner_user_id/name/balance sütunlarını id sırasıyla getiren tek hazır SELECT, kısa SELECT/FROM/WHERE/ORDER BY açıklamaları ve üç çözümsüz yorum/TODO var. Geliştirici mevcut VS Code Oracle SQL Developer/HesapLab bağlantısında Run Script ile hazır sorguyu çalıştırdı; üç hesap, id sırası ve 10000/3500/2000 doğrulandı. Yönetici SQL/README/plan/ilgili diff'i inceledi; diff kontrolünü ve gerçek Run Script sonucunu ayrıca doğruladı: id 1/2/3, sahip 1/1/2, üç bakiye. Bulgu yok; üç TODO cevapsız. README adımları aynı arayüzle tutarlı. Şema/seed/veri/frontend değişmedi; build/lint tekrarlanmadı. Berk ilk sorgusunu mevcut kayıttan kimlik bularak yazacak; SQL/kısıt yetkinliği varsayılmaz.
- **6.9 Değerlendirme:** Bir müşterinin hesaplarını getiren sorguyu açıklat.

## 7. JOIN, transaction ve kilitleme
**Durum:** 7.1 dosyası geliştirici/yönetici kontrolünden geçti; JOIN öğrenimi henüz değerlendirilmedi. Yönetici iki SELECT'i mevcut Oracle'da SQL*Plus ile çalıştırdı: INNER 3/LEFT 4 satır, sahiplik/tutar/admin eşleşmemesi doğru; çıkış 0 ve diff kontrolü geçti. Berk hızlanmak istedi; önceki WHERE/diğer TODO sonuçları görülmedi, SQL öğrenimi tamamlandı sayılmaz. 7.2 uygulandı; 7.3–7.5 SQL alıştırması hazır ve geliştirici doğruladı, öğrenme ayrıca değerlendirilecek. 7.6–7.8 planlandı.
- **7.1 JOIN:** Ayrı tablolardaki ilişkili bilgileri tek sonuçta getir.
  - **7.1 çalışma paketi (inceleme geçti, öğrenme doğrulanmadı):** db/oracle/05_join_examples.sql yalnız iki SELECT içerir: accounts–app_users INNER JOIN ve kullanıcıdan başlayan LEFT JOIN; ON owner_user_id=id. Alias, iki hesap için iki satır, INNER/LEFT farkı ve NULL açıklamaları var. Geliştirici VS Code Run Script ile; yönetici kod/docs/diff incelemesi ve SQL*Plus ile sonuçları ayrı doğruladı: INNER 3/LEFT 4, doğru sahiplik/10000-3500-2000 TRY, adminin eşleşmeyen hesap alanları. Bulgu yok. Unsaved kullanıcı sorgusu ve önceki TODO'lar korundu; şema/seed/veri/frontend/backend değişmedi, build/lint tekrarlanmadı. Berk ikinci sorgunun yeni sekmedeki kopyasında LEFT→INNER değişimini çalıştırıp farkı açıklayacak; yanıt henüz yok, genel SQL/JOIN yetkinliği varsayılmaz.
- **7.2 Hareketler:** Güncel bakiye ile geçmiş işlem kayıtlarının farklı görevlerini anlat.
  - **7.2 çalışma paketi (geliştirici/yönetici kontrolü geçti, öğrenme doğrulanmadı):** 06 schema/07 seed/08 queries mevcut Oracle'da uygulandı. account_movements: identity PK/account FK, CREDIT-DEBIT, pozitif amount, negatif olmayan balance_after, isteğe bağlı açıklama, saat dilimli created_at. Yönetici kod/docs/ilgili diff ve SQL*Plus ile üç açılış CREDIT/sahiplik/10000-3500-2000/zaman alanlarını; COMMIT çıkarılmış seed'in savepoint/rollback tekrarında 0 ek kayıt/son toplam 3 sonucunu doğruladı, çıkış 0. Metadata'da 7 sütun/11 etkin-doğrulanmış kısıt var. Negatif ORA-02291/02290 kontrolleri geliştiriciye aittir, yönetici tekrarlamadı. Mevcut kullanıcı/hesap/bakiyeler korunur; eski şema/seed tekrarlanmadı. Asıl unsaved diyagram/sorgu korunup ayrı dgm kopyasında üç tablo/iki FK çizgisi görsel doğrulandı; PNG eski iki tablo. Sonraki öğretim güncel bakiye/geçmiş snapshot ayrımı ve neden çok hareket satırı tutulduğu. Transfer/React/Java ve genel yetkinlik varsayılmaz; 7.1 cevabını yönetici vermişti, WHERE/TODO/kısıt/normal form açıkları sürer.
- **7.3 Transfer adımları:** Kaynak, hedef, tutar, kontroller ve kayıtları sırala.
- **7.4 Transaction:** Bakiye ve hareket değişikliklerini birlikte tamamla veya geri al.
- **7.5 Commit/rollback:** Sonucun kalıcılaşmasını ve hata durumunu veriler üzerinde gözlemle.
  - **7.3–7.5 çalışma paketi (hazır, öğrenme doğrulanmadı):** db/oracle/09_transfer_transaction_demo.sql; kullanıcı adı/hesap adıyla iki TRY hesabını bulur, varlık/farklılık/yeterli bakiye kontrol eder. 200 TRY için iki UPDATE/iki hareket INSERT aynı transaction'dadır. Ayrı SQL*Plus oturumunda geliştirici çıkış 0 ile doğruladı: önce 10000/2000 ve 3 hareket; geçici 9800/2200, DEBIT/CREDIT 200 ve balance_after 9800/2200; başarı rollback sonrası başlangıç. Kontrollü ORA-20010 yakalandığında kaynak 9800 kaldığı gösterildi; açık rollback sonrası 10000/2000 ve 3 hareket. Başlangıç/son sorgularında tüm mevcut hesap/hareket sütunları korundu, Birikim 3500 kaldı. COMMIT çalıştırılmadı; açıklaması var. Kimlik boşlukları normaldir, eşzamanlılık/kilitleme ayrı konudur. README çalıştırma komutu/diff/son dosya kontrolü yapıldı; eski SQL/React/backend/unsaved sekmeler korunur, frontend testleri tekrarlanmadı. Berk yalnız çok hareket satırını bir hesabın birden fazla hareketiyle açıkladı; transferi/rollback'i bağımsız yazabildiği veya genel SQL yetkinliği varsayılmaz. Sıradaki tek adım çıktıda geçici durum ile rollback sonrası durumu açıklaması. Yönetici de 09 SQL/README/ilgili diff ve ayrı SQL*Plus çalıştırmasını doğruladı: iki senaryo aynı sonuçlar/çıkış 0; önce-sonra üç hesap ve üç hareketin tüm sütunları aynı. COMMIT çalıştırılmadı; bulgu yok, öğrenme yanıtı bekliyor.
- **7.6 Eşzamanlılık:** Aynı hesaba aynı anda erişen işlemlerdeki problemi tanıt.
- **7.7 Kilitleme:** Bir işlemin diğerini bekletmesinin amacını açıkla.
- **7.8 Değerlendirme:** Yarım transferin neden yanlış olduğunu ve nasıl önlendiğini açıklat.

## 8. Java ve nesne yönelimli programlama
**Durum:** Planlandı.
- **8.1 Rolü:** Java'nın sunucuda neyi yöneteceğini açıkla.
- **8.2 Dil temeli:** Değişken, tür, koşul, döngü ve metotları ihtiyaç kadar öğret.
- **8.3 Sınıf/nesne:** Hesap tanımı ile belirli hesap örneğini ayır.
- **8.4 Üyeler:** Alan, kurucu ve metotların görevlerini uygula.
- **8.5 Kapsülleme:** Hesap bilgisinin değişmesini kurallı metotlarla yönet.
- **8.6 Koleksiyonlar:** Hesap ve hareket listeleri üzerinde çalış.
- **8.7 Hatalar:** Beklenen iş hataları ile teknik sorunları ayır.
- **8.8 Para değerleri:** Uygun sayı türü ve yuvarlama kurallarını belirle.
- **8.9 Değerlendirme:** Küçük sınıfı okuyup değiştirme ve nedenini açıklama uygulaması yap.

## 9. Spring Boot ve REST API
**Durum:** Planlandı.
- **9.1 Sunucu:** Ayrı uygulamayı oluşturup çalıştır.
- **9.2 HTTP:** İstek, yöntem, adres, durum kodu ve yanıtı öğret.
- **9.3 JSON:** Java nesnesi ile React'in aldığı veri arasındaki taşıma biçimini açıkla.
- **9.4 İlk API:** Küçük hesap listesini döndüren uç noktayı hazırla.
- **9.5 Sorumluluklar:** İstek karşılama, iş kuralları ve veri erişimini ayır.
- **9.6 Oracle:** Bağlantıyı kur; hassas ayarları kaynak koddan ayrı tut.
- **9.7 Girdi/hata:** Geçersiz isteklerde tutarlı ve anlaşılır yanıtlar üret.
- **9.8 Değerlendirme:** Bir isteğin sunucudaki yolunu açıklat.

## 10. Entegrasyon ve transfer
**Durum:** Planlandı.
- **10.1 Veri kaynağı:** React hesaplarını API ve Oracle verisinden getir.
- **10.2 Hata ayıklama:** Ekran, sunucu ve veritabanı sorunlarını ayır.
- **10.3 İstek:** Transfer formundaki bilgileri sunucuya gönder.
- **10.4 Kurallar:** Hesap, tutar, yeterli bakiye ve işlem kurallarını sunucuda uygula.
- **10.5 Atomik kayıt:** İki bakiyeyi ve hareketleri aynı Oracle transaction'ında işle.
- **10.6 Sonuç:** Başarı veya reddi ve güncel bilgileri ekrana taşı.
- **10.7 Hareket ekranı:** İlgili hesabın geçmiş işlemlerini göster.
- **10.8 Değerlendirme:** Butondan veritabanına ve tekrar ekrana olan yolu açıklat.

## 11. Giriş, roller ve hesap sahipliği
**Durum:** Planlandı.
- **11.1 Kavramlar:** Kimlik doğrulama ile yetkilendirmeyi ayır.
- **11.2 Giriş:** Demo kullanıcı girişini ve uygun parola saklamayı kur.
- **11.3 Oturum:** Sonraki isteklerde kullanıcının nasıl tanındığını açıkla.
- **11.4 Hesap erişimi:** Müşterinin yalnızca kendi hesaplarına erişmesini sunucuda sağla.
- **11.5 Transfer yetkisi:** Kaynak hesabın kullanıcıya ait olduğunu doğrula.
- **11.6 Yönetim:** ADMIN kullanıcı ve hesap yönetimi işlemlerini ekle.
- **11.7 Sınır:** ADMIN'e varsayılan olarak müşteri adına transfer yetkisi verme.
- **11.8 Çıkış/red:** Geçersiz oturum ve yetkisiz işlemleri ele al.
- **11.9 Değerlendirme:** Yetkinin neden yalnızca ekranda kontrol edilemeyeceğini açıklat.

## 12. Kritik senaryoları test etme
**Durum:** Planlandı. Önceki her görevde de kapsamına uygun kontrol yapılır.
- **12.1 Amaç:** Testin yakalayacağı davranış bozukluğunu belirle.
- **12.2 Senaryolar:** Normal, geçersiz ve yetkisiz kullanımı koddan önce çıkar.
- **12.3 Başarı:** Yanıtla birlikte bakiye ve hareket kayıtlarını doğrula.
- **12.4 Red:** Reddedilen transferlerde verinin değişmediğini kontrol et.
- **12.5 Geri alma:** İşlem ortasındaki hatada kısmi sonuç kalmadığını sına.
- **12.6 Yetkiler:** Müşteri ve yönetici sınırlarını test et.
- **12.7 Test türü:** Birim ve entegrasyon testlerinin farklı amaçlarını öğret.
- **12.8 Değerlendirme:** Berk'in özelliğe uygun anlamlı senaryolar çıkarmasını iste.

## 13. Idempotency ve eşzamanlılık
**Durum:** Planlandı.
- **13.1 İstek tekrarı:** Çift tıklama ve yanıt alınamaması durumlarını incele.
- **13.2 Idempotency:** Aynı isteğin ikinci transfer oluşturmamasını sağla.
- **13.3 İstek kimliği:** Tekrarı tanıma ve aynı kimlikle farklı içerik durumunu belirle.
- **13.4 Yanıt kaybı:** Kayıt tamamlanıp yanıt ulaşmadığında tekrar denemeyi sına.
- **13.5 Paralel transfer:** Aynı hesaptan gelen farklı işlemlerde toplam bakiyeyi koru.
- **13.6 Kilit sırası:** Bekleme ve çakışma durumlarını değerlendir.
- **13.7 Veriler:** Son bakiye, transfer ve hareket sayılarını birlikte doğrula.
- **13.8 Değerlendirme:** Tekrarlanan istek ile eşzamanlı farklı isteklerin farkını açıklat.

## 14. RabbitMQ ve ayrı Spring Boot worker
**Durum:** Planlandı.
- **14.1 İş seçimi:** Dekont/ekstre hazırlamanın neden arka planda yapılacağını belirle.
- **14.2 Mesaj/kuyruk:** Üreten uygulama ve işi alan uygulamanın ilişkisini öğret.
- **14.3 Ayrı program:** Ana uygulama ile worker'ı ayrı çalıştır.
- **14.4 İçerik:** Mesajın taşıyacağı gerekli bilgileri belirle.
- **14.5 Gönderim:** Başarılı işlemle ilgili arka plan işini bildir.
- **14.6 İşleme:** Worker mesajı alıp işi yapsın ve sonucu kaydetsin.
- **14.7 Durumlar:** Hazırlanıyor, tamamlandı ve başarısız sonuçlarını takip et.
- **14.8 Ayrım:** Tamamlanmış transfer ile dekont işinin sonucunu ayrı ele al.
- **14.9 Değerlendirme:** Uygulama, kuyruk ve worker görevlerini açıklat.

## 15. Retry, DLQ ve outbox
**Durum:** Planlandı.
- **15.1 Hata noktaları:** Uygulama, kuyruk ve worker kesintilerini çıkar.
- **15.2 Hata türü:** Geçici ve kalıcı hataları ayır.
- **15.3 Retry:** Yeniden deneme sayısı ve aralığını belirle.
- **15.4 DLQ:** İşlenemeyen mesajları incelemek için ayrı kuyruğa taşı.
- **15.5 Tekrar işleme:** Aynı mesajın tekrar sonuç üretmesini doğru yönet.
- **15.6 Gönderim boşluğu:** Veritabanı kaydı ile mesaj gönderiminin ayrı başarısızlıklarını incele.
- **15.7 Outbox:** Transfer ve gönderilecek olay kaydını aynı transaction'da sakla; ayrı süreçle gönder.
- **15.8 Tekrar gönderim:** Outbox yanında tüketici tarafının da tekrarları ele almasını sağla.
- **15.9 Toparlanma:** Durdurma/yeniden başlatma senaryolarını sına.
- **15.10 Değerlendirme:** Kayıp ve tekrar risklerine karşı davranışı açıklat.

## 16. Proje anlatımı ve mülakat
**Durum:** Planlandı.
- **16.1 Kısa anlatım:** Amaç ve temel özellikleri yaklaşık bir dakikada anlat.
- **16.2 Kullanıcı yolu:** Girişten hesaplar ve transfere uzanan akışı göster.
- **16.3 Teknik yol:** React, Spring Boot, Oracle, RabbitMQ ve worker ilişkisini anlat.
- **16.4 Kararlar:** Tercihleri ve çözülen ihtiyacı örneklerle açıkla.
- **16.5 Hatalar:** Yetersiz bakiye, tekrar istek ve worker kesintisini anlat.
- **16.6 Kanıt:** Yapılan testleri ve sınırlarını doğru sun.
- **16.7 Belgeler:** README ve çalıştırma adımlarını güncel tut.
- **16.8 Prova:** Soruları tek tek cevapla; eksik noktaları tekrar çalış.
- **16.9 Son ölçüt:** Projeyi ezberlemeden amacı, davranışı ve nedenleriyle açıklayabil.

## 17. Bütün aşamalarda çalışma düzeni
- **17.1 Tek hedef:** Her görevde küçük, anlaşılır bir öğrenme hedefi seç.
- **17.2 Önce anlatım:** Yeni kavramı sade örnek ve projenin ihtiyacıyla ilişkilendir.
- **17.3 Kullanıcı uygulaması:** Öğrenme alıştırmasını Berk yapsın; geliştirici onun yerine yapmasın.
- **17.4 Tek soru:** Bir yanıtı değerlendirmeden yeni soru yığınına geçme.
- **17.5 Geliştirici promptu:** Üstünde model, düşünme ve hız önerisi; içinde hedef, kapsam ve kabul ölçütleri bulunsun.
- **17.6 Rapor ve kod inceleme:** Her geliştirici çıktısından sonra aşağıdaki döngüyü uygula.
  - **17.6.1 Raporu ayır:** Ne yapıldığı, neyin test edildiği ve neyin yapılmadığı iddialarını çıkar.
  - **17.6.2 Bağlamı oku:** Önce AGENTS ve STATE; sonra yalnızca gereken kaynaklar. Bütün repoyu her tur okuma.
  - **17.6.3 Değişiklikleri bul:** Git durumunu ve diff'i incele; Berk'in önceki değişikliklerini geliştirici değişikliğiyle karıştırma.
  - **17.6.4 Güncel kodu oku:** Yalnızca rapordaki parçayı değil, değişen fonksiyon/component'in ilgili bütününü oku.
  - **17.6.5 Akışı izle:** Girdi, state, props, olay, dönüş değeri ve ekrana/veritabanına giden sonucu takip et.
  - **17.6.6 Kuralları kontrol et:** Görev kapsamı, sahiplik, veri bütünlüğü ve ilgili sınır durumlarını değerlendir.
  - **17.6.7 Uygun kontrolleri çalıştır:** İlgili build/lint/testleri çalıştır; düşük etkili işlerde gereksiz test altyapısı ekleme.
  - **17.6.8 Davranışı gözle:** Arayüz işinde gerçek ekranı ve etkileşimi; backend işinde ilgili istek ve kayıtları doğrula.
  - **17.6.9 Kanıtı ayır:** “Geliştirici bildirdi”, “kodu okuyarak gördüm”, “kendim çalıştırdım” sonuçlarını açıkça ayır.
  - **17.6.10 Karar ver:** Bulgu varsa somut tetikleyici/etki/düzeltme isteği; yoksa incelenen kapsam içinde sonucu bildir.
  - **17.6.11 Düzeltmeyi sınırla:** Gerekirse küçük düzeltme promptu hazırla; yeni özelliklerle karıştırma.
  - **17.6.12 Kodu öğret:** İlgili dosyayı aç, satırı göster, bütün resmi anlat; sonra yeni sözdizimini parçala.
  - **17.6.13 Ekranı kullan:** Berk isterse sekmeleri yönet ve satırları seç; kullanıcı alıştırmasının kodunu onun yerine yazma.
  - **17.6.14 Öğrenmeyi kontrol et:** Küçük tahmin/uygulama ve tek soruyla anlayışı değerlendir; gerekirse sadeleştir.
  - **17.6.15 Notları güncelle:** STATE kısa kalsın; kodun ve öğrenmenin tamamlanma durumunu ayrı yaz.
- **17.7 AI ile çalışma:** Görev tanımlama, kod inceleme, hata ayıklama, test tasarlama ve doğrulamayı öğret.
- **17.8 Durum kaydı:** AGENTS/ROADMAP/STATE en fazla 40 satır; uzun açıklamalar bu planda bulunsun.
- **17.9 Her yanıtın konumu:** Güncel alt adımı belirt. Kod hazır mı, ne kontrol edildi, ne öğrenildi, sıradaki tek iş ne: ayrı takip et.

## Geliştirici sonucundan sonra kısa değerlendirme biçimi
1. **Konum:** Öğrenme planındaki güncel numara.
2. **Kod sonucu:** Ne değişti, hangi davranış oluştu?
3. **İncelenen kaynak:** İlgili dosya ve akış.
4. **Kendi kontrolüm:** Gerçekte yaptığım kontroller ve sonuçları.
5. **Bulgular/sınırlar:** Düzeltme gerektiren durum veya kontrol edilemeyen bölüm.
6. **Öğrenme durumu:** Berk neyi açıkladı; hangi yeni konular hâlâ bekliyor?
7. **Sıradaki tek iş:** Küçük açıklama, kullanıcı alıştırması, tek soru veya gerekiyorsa düzeltme promptu.
