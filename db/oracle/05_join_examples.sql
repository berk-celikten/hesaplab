-- 7.1: JOIN yalnız sonuç üretir; tabloları ve kayıtları değiştirmez.
-- a, accounts tablosunun; u, app_users tablosunun kısa adıdır.
-- AS account_id gibi sütun takma adları, sonuç başlıklarını anlaşılır kılar.
-- ON, hesap sahibinin kimliğiyle kullanıcı kimliğini eşleştirir.

-- INNER JOIN yalnız eşleşen kullanıcı ve hesapları getirir.
-- Bir kullanıcının iki hesabı varsa her hesap eşleşmesi için bir satır gelir.
SELECT
    a.id AS account_id,
    u.username AS username,
    u.display_name AS customer_name,
    a.name AS account_name,
    a.balance AS balance,
    a.currency AS currency
FROM accounts a
INNER JOIN app_users u ON a.owner_user_id = u.id
ORDER BY a.id;

-- LEFT JOIN soldaki bütün kullanıcıları korur; hesap varsa yanına ekler.
-- Hesabı olmayan demo admin de gelir; hesap sütunları NULL (değer yok) olur.
-- NULL, sıfır bakiye veya boş adlı bir hesap bulunduğu anlamına gelmez.
SELECT
    u.id AS user_id,
    u.username AS username,
    u.role AS role,
    a.id AS account_id,
    a.name AS account_name,
    a.balance AS balance
FROM app_users u
LEFT JOIN accounts a ON a.owner_user_id = u.id
ORDER BY u.id, a.id;
