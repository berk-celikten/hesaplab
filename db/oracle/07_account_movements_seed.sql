-- Canlandırılmış açılış kayıtlarıdır; gerçek işlem geçmişi değildir.
-- Hesap bakiyelerini değiştirmez. Kimlikleri Oracle ve mevcut hesaplar sağlar.
-- Hareketi olan veya bakiyesi sıfır olan hesaba açılış kaydı eklenmez.
INSERT INTO account_movements (account_id, direction, amount, balance_after, description)
SELECT a.id, 'CREDIT', a.balance, a.balance, 'Demo açılış bakiyesi'
FROM accounts a
WHERE a.balance > 0
  AND NOT EXISTS (
      SELECT 1
      FROM account_movements m
      WHERE m.account_id = a.id
  );

COMMIT;
