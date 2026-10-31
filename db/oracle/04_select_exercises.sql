-- 6.8.1: İlk SELECT alıştırmaları
-- SELECT: Sonuçta gösterilecek sütunları seçer.
-- FROM: Verinin alınacağı tabloyu belirtir.
-- WHERE: Yalnız koşulu sağlayan satırları getirir.
-- ORDER BY: Sonuç satırlarını belirtilen sütuna göre sıralar.

-- Hazır örnek: Hesapları kimlik sırasıyla getirir.
SELECT id, owner_user_id, name, balance
FROM accounts
ORDER BY id;

-- TODO 1: Demo Müşteri 01'in kimliğini mevcut kullanıcı kaydından bul.
-- Ardından yalnızca bu müşterinin hesaplarını getir; kimliği tahmin etme.

-- TODO 2: Bakiyesi 3000 ve üzerindeki hesapları getir.
-- Sonucu bakiyeye göre büyükten küçüğe sırala.

-- TODO 3: Yalnızca ADMIN kullanıcıların id, display_name ve role bilgilerini getir.
-- Bu üç alıştırmanın sorgularını Berk yazacak; hazır çözüm yoktur.
