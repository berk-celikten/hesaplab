-- 7.6–7.7: Yalnız yeni, ayrı demo bağlantısında çalıştırın.
-- SQL*Plus'ı etkileşimli açın; bu dosyayı stdin'e pipe etmeyin.
-- PAUSE sırasında bağlantı açık kalır ve kilit tutulur. EXIT yoktur.
SET AUTOCOMMIT OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
WHENEVER OSERROR EXIT FAILURE ROLLBACK

DECLARE
    account_id accounts.id%TYPE;
    account_balance accounts.balance%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('A SID=' || SYS_CONTEXT('USERENV', 'SID')
        || ', SESSIONID=' || SYS_CONTEXT('USERENV', 'SESSIONID'));

    -- Dış sorgu yalnız accounts üzerindedir; kullanıcı alt sorgusu kilit almaz.
    SELECT a.id, a.balance INTO account_id, account_balance
    FROM accounts a
    WHERE a.owner_user_id = (
        SELECT u.id FROM app_users u WHERE u.username = 'demo_customer_01'
    )
      AND a.name = 'Vadesiz Hesap' AND a.currency = 'TRY'
    FOR UPDATE OF a.balance WAIT 5;

    DBMS_OUTPUT.PUT_LINE('A_LOCKED: hesap=' || account_id || ', bakiye=' || account_balance);
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
-- Önce B'yi çalıştırıp süre aşımını görün. Sonra B'yi tekrar çalıştırın.
-- B beklerken A'da Enter: aşağıdaki ROLLBACK aynı A oturumunda çalışır.
PAUSE A kilidi tutuyor. B ikinci kez beklerken Enter ile ROLLBACK yapin.
ROLLBACK;
PROMPT A_RELEASED: ROLLBACK tamamlandi; SQL*Plus baglantisi acik kalir.
