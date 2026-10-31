-- Oracle 19c söz dizimi hedeflenmiştir; yerel Oracle AI Database 26ai Free üzerinde çalıştırıldı.
CREATE TABLE app_users (
    id NUMBER GENERATED ALWAYS AS IDENTITY,
    username VARCHAR2(50 CHAR) NOT NULL,
    display_name VARCHAR2(100 CHAR) NOT NULL,
    role VARCHAR2(8 CHAR) NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uq_app_users_username UNIQUE (username),
    CONSTRAINT ck_app_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

CREATE TABLE accounts (
    id NUMBER GENERATED ALWAYS AS IDENTITY,
    owner_user_id NUMBER NOT NULL,
    name VARCHAR2(100 CHAR) NOT NULL,
    balance NUMBER(15,2) DEFAULT 0 NOT NULL,
    currency VARCHAR2(3 CHAR) DEFAULT 'TRY' NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_user_id) REFERENCES app_users (id),
    CONSTRAINT ck_accounts_balance CHECK (balance >= 0),
    CONSTRAINT ck_accounts_currency CHECK (currency = 'TRY')
);
