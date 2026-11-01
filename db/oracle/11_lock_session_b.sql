-- 7.6–7.7: A'dan gerçekten farklı, yeni bir Oracle bağlantısı kullanın.
SET AUTOCOMMIT OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
WHENEVER OSERROR EXIT FAILURE ROLLBACK
VARIABLE demo_account_id NUMBER

-- Bu blok önce tamamlanır: normal SELECT, A kilitliyken de sonuç verir.
DECLARE
    account_balance accounts.balance%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('B SID=' || SYS_CONTEXT('USERENV', 'SID')
        || ', SESSIONID=' || SYS_CONTEXT('USERENV', 'SESSIONID'));
    SELECT a.id, a.balance INTO :demo_account_id, account_balance
    FROM accounts a
    WHERE a.owner_user_id = (
        SELECT u.id FROM app_users u WHERE u.username = 'demo_customer_01'
    )
      AND a.name = 'Vadesiz Hesap' AND a.currency = 'TRY';
    DBMS_OUTPUT.PUT_LINE('B_NORMAL_SELECT: hesap=' || :demo_account_id
        || ', bakiye=' || account_balance);
END;
/
PROMPT B_WAIT_START: ayni hesap icin FOR UPDATE WAIT 5 basliyor.
DECLARE
    account_balance accounts.balance%TYPE;
    started_at PLS_INTEGER := DBMS_UTILITY.GET_TIME;
BEGIN
    SELECT a.balance INTO account_balance
    FROM accounts a WHERE a.id = :demo_account_id
    FOR UPDATE OF a.balance WAIT 5;
    DBMS_OUTPUT.PUT_LINE('B_LOCKED: bakiye=' || account_balance
        || ', bekleme(sn)=' || (DBMS_UTILITY.GET_TIME - started_at) / 100);
EXCEPTION
    WHEN OTHERS THEN
        -- WAIT 5 süre aşımı: sürüme göre ORA-30006 veya ORA-00054 dönebilir.
        -- Yerel 26ai, ORA-00054 altında ORA-40097 ayrıntısını da döndürüyor.
        IF SQLCODE IN (-30006, -54) THEN
            DBMS_OUTPUT.PUT_LINE('B_EXPECTED_TIMEOUT: ' || SQLERRM
                || ', bekleme(sn)=' || (DBMS_UTILITY.GET_TIME - started_at) / 100);
        ELSE
            ROLLBACK;
            RAISE; -- Beklenmeyen hataları başarı gibi gösterme.
        END IF;
END;
/
-- Başarıda da beklenen süre aşımında da yalnız bu B oturumunu temizler.
ROLLBACK;
PROMPT B_RELEASED: ROLLBACK tamamlandi.
