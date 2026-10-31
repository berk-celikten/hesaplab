-- 7.2: Mevcut kullanıcı/hesap tablolarını değiştirmeden hareket tablosunu ekler.
-- Bir kez uygulanır; tablo zaten varsa önce yapısı ve verisi incelenmelidir.
CREATE TABLE account_movements (
    id NUMBER GENERATED ALWAYS AS IDENTITY,
    account_id NUMBER NOT NULL,
    direction VARCHAR2(6 CHAR) NOT NULL,
    amount NUMBER(15,2) NOT NULL,
    balance_after NUMBER(15,2) NOT NULL,
    description VARCHAR2(200 CHAR),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT pk_account_movements PRIMARY KEY (id),
    CONSTRAINT fk_movements_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT ck_movements_direction CHECK (direction IN ('CREDIT', 'DEBIT')),
    CONSTRAINT ck_movements_amount CHECK (amount > 0),
    CONSTRAINT ck_movements_balance CHECK (balance_after >= 0)
);
