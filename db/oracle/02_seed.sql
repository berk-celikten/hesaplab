-- 01_schema.sql sonrasında, boş tablolara bir kez uygulanmak üzere hazırlanmıştır.
-- Yerel Oracle Free üzerinde bir kez uygulandı. Identity kimliklerini Oracle üretir.
INSERT INTO app_users (username, display_name, role)
VALUES ('demo_customer_01', 'Demo Müşteri 01', 'CUSTOMER');

INSERT INTO app_users (username, display_name, role)
VALUES ('demo_customer_02', 'Demo Müşteri 02', 'CUSTOMER');

INSERT INTO app_users (username, display_name, role)
VALUES ('demo_admin', 'Demo Admin', 'ADMIN');

INSERT INTO accounts (owner_user_id, name, balance, currency)
VALUES (
    (SELECT id FROM app_users WHERE username = 'demo_customer_01'),
    'Vadesiz Hesap', 10000.00, 'TRY'
);

INSERT INTO accounts (owner_user_id, name, balance, currency)
VALUES (
    (SELECT id FROM app_users WHERE username = 'demo_customer_01'),
    'Birikim Hesabı', 3500.00, 'TRY'
);

INSERT INTO accounts (owner_user_id, name, balance, currency)
VALUES (
    (SELECT id FROM app_users WHERE username = 'demo_customer_02'),
    'Vadesiz Hesap', 2000.00, 'TRY'
);

COMMIT;
