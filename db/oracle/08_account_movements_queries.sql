-- accounts.balance güncel durumdur; balance_after hareket sonrasındaki geçmiş durumdur.
-- CREDIT giriş, DEBIT çıkıştır; her iki yönde de amount pozitif tutulur.
-- İleride bakiye ve hareketler aynı Oracle transaction'ında işlenecek; burada transfer yoktur.
SELECT
    m.id AS movement_id,
    a.id AS account_id,
    u.username AS owner_username,
    u.display_name AS owner_name,
    a.name AS account_name,
    m.direction,
    m.amount,
    m.balance_after,
    a.balance AS current_balance,
    a.currency,
    m.description,
    m.created_at
FROM account_movements m
INNER JOIN accounts a ON a.id = m.account_id
INNER JOIN app_users u ON u.id = a.owner_user_id
ORDER BY a.id, m.created_at, m.id;
