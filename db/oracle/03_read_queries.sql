-- Oracle'da oluşturulan kullanıcı ve hesap kayıtlarını gösterir.
SELECT id, username, display_name, role
FROM app_users
ORDER BY id;

SELECT id, owner_user_id, name, balance, currency
FROM accounts
ORDER BY id;
