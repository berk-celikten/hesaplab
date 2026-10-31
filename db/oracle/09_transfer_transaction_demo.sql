-- 7.3–7.5: Dosyanın tamamını yeni, yalnız bu demoya ayrılmış bağlantıda çalıştırın.
-- Açık kullanıcı oturumundaki bekleyen işlemlerle aynı bağlantıyı kullanmayın.
-- Demo kullanıcı adları gerçek kimlik doğrulama değildir.
-- COMMIT değişiklikleri kalıcılaştırır; bu dosyada çalıştırılmaz.
-- ROLLBACK bakiyeleri ve hareketleri geri alır; identity sayacı geri sarılmaz.
-- Eşzamanlılık/kilitleme bu alıştırmanın kapsamı dışındadır.
SET AUTOCOMMIT OFF
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
WHENEVER OSERROR EXIT FAILURE ROLLBACK

DECLARE
    transfer_amount CONSTANT NUMBER(15,2) := 200;
    source_id accounts.id%TYPE;
    target_id accounts.id%TYPE;
    source_before accounts.balance%TYPE;
    target_before accounts.balance%TYPE;
    source_after accounts.balance%TYPE;
    target_after accounts.balance%TYPE;
    source_movement_id account_movements.id%TYPE;
    target_movement_id account_movements.id%TYPE;
    movements_before NUMBER;
    movements_after NUMBER;
BEGIN
    -- SELECT INTO: Eksik veya birden çok eşleşmede hata verir; dış handler geri alır.
    SELECT a.id, a.balance INTO source_id, source_before
    FROM accounts a
    JOIN app_users u ON u.id = a.owner_user_id
    WHERE u.username = 'demo_customer_01' AND a.name = 'Vadesiz Hesap'
      AND a.currency = 'TRY';

    SELECT a.id, a.balance INTO target_id, target_before
    FROM accounts a
    JOIN app_users u ON u.id = a.owner_user_id
    WHERE u.username = 'demo_customer_02' AND a.name = 'Vadesiz Hesap'
      AND a.currency = 'TRY';

    IF source_id = target_id THEN
        RAISE_APPLICATION_ERROR(-20001, 'Kaynak ve hedef farklı olmalı.');
    END IF;
    IF source_before < transfer_amount THEN
        RAISE_APPLICATION_ERROR(-20002, 'Kaynak bakiyesi yetersiz.');
    END IF;

    SELECT COUNT(*) INTO movements_before FROM account_movements;
    DBMS_OUTPUT.PUT_LINE('ÖNCE: kaynak=' || source_before || ', hedef=' || target_before
        || ', toplam hareket=' || movements_before);

    -- 1 ve 2: İki bakiye değişikliği aynı transaction içindedir.
    UPDATE accounts SET balance = balance - transfer_amount WHERE id = source_id
    RETURNING balance INTO source_after;
    UPDATE accounts SET balance = balance + transfer_amount WHERE id = target_id
    RETURNING balance INTO target_after;

    -- 3 ve 4: amount iki yönde de pozitif; balance_after yeni bakiyedir.
    INSERT INTO account_movements (account_id, direction, amount, balance_after, description)
    VALUES (source_id, 'DEBIT', transfer_amount, source_after, 'Demo transfer çıkışı')
    RETURNING id INTO source_movement_id;
    INSERT INTO account_movements (account_id, direction, amount, balance_after, description)
    VALUES (target_id, 'CREDIT', transfer_amount, target_after, 'Demo transfer girişi')
    RETURNING id INTO target_movement_id;

    DBMS_OUTPUT.PUT_LINE('BAŞARI / GEÇİCİ: kaynak=' || source_after || ', hedef=' || target_after);
    -- Yalnız bu çalıştırmada eklenen hareketleri, aynı bağlantıda geri almadan göster.
    FOR movement IN (
        SELECT id, account_id, direction, amount, balance_after
        FROM account_movements
        WHERE id IN (source_movement_id, target_movement_id)
        ORDER BY id
    ) LOOP
        DBMS_OUTPUT.PUT_LINE('Yeni hareket: id=' || movement.id || ', hesap=' || movement.account_id
            || ', yön=' || movement.direction || ', tutar=' || movement.amount
            || ', balance_after=' || movement.balance_after);
    END LOOP;
    SELECT COUNT(*) INTO movements_after FROM account_movements;
    IF source_after <> source_before - transfer_amount
        OR target_after <> target_before + transfer_amount
        OR movements_after <> movements_before + 2 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Geçici transfer sonucu beklenenden farklı.');
    END IF;

    ROLLBACK;
    SELECT balance INTO source_after FROM accounts WHERE id = source_id;
    SELECT balance INTO target_after FROM accounts WHERE id = target_id;
    SELECT COUNT(*) INTO movements_after FROM account_movements;
    DBMS_OUTPUT.PUT_LINE('BAŞARI / ROLLBACK SONRASI: kaynak=' || source_after
        || ', hedef=' || target_after || ', toplam hareket=' || movements_after);
    IF source_after <> source_before OR target_after <> target_before
        OR movements_after <> movements_before THEN
        RAISE_APPLICATION_ERROR(-20004, 'Başarı senaryosunun geri alınması doğrulanamadı.');
    END IF;

    -- Ayrı hata senaryosu: Yalnız kaynak güncellenir, ardından kontrollü hata gelir.
    BEGIN
        UPDATE accounts SET balance = balance - transfer_amount WHERE id = source_id;
        RAISE_APPLICATION_ERROR(-20010, 'Kaynak düşüldükten sonra kontrollü demo hatası.');
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE <> -20010 THEN
                RAISE; -- Beklenmeyen hata dış handler'a gider.
            END IF;
            DBMS_OUTPUT.PUT_LINE('YAKALANAN HATA: ' || SQLERRM);
            SELECT balance INTO source_after FROM accounts WHERE id = source_id;
            DBMS_OUTPUT.PUT_LINE('HATA / ROLLBACK ÖNCESİ: kaynak=' || source_after);
            -- Bir SQL ifadesinin hatası, önceki başarılı ifadeleri topluca geri almaz.
            -- Yakalanan hata tek başına transaction rollback'i yerine geçmez.
            ROLLBACK;
    END;

    SELECT balance INTO source_after FROM accounts WHERE id = source_id;
    SELECT balance INTO target_after FROM accounts WHERE id = target_id;
    SELECT COUNT(*) INTO movements_after FROM account_movements;
    DBMS_OUTPUT.PUT_LINE('HATA / ROLLBACK SONRASI: kaynak=' || source_after
        || ', hedef=' || target_after || ', toplam hareket=' || movements_after);
    IF source_after <> source_before OR target_after <> target_before
        OR movements_after <> movements_before THEN
        RAISE_APPLICATION_ERROR(-20005, 'Hata senaryosunun geri alınması doğrulanamadı.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
ROLLBACK;
