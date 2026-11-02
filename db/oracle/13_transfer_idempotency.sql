-- Bir kez, metadata kontrolünden sonra ayrı bağlantıda.
CREATE TABLE transfers (
 id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id NUMBER NOT NULL REFERENCES app_users(id),
 idempotency_key VARCHAR2(36 CHAR) NOT NULL,
 source_account_id NUMBER NOT NULL REFERENCES accounts(id),
 target_account_id NUMBER NOT NULL REFERENCES accounts(id),
 amount NUMBER(15,2) NOT NULL CHECK(amount>0),
 currency VARCHAR2(3 CHAR) DEFAULT 'TRY' NOT NULL CHECK(currency='TRY'),
 created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
 CONSTRAINT uq_transfers_user_key UNIQUE(user_id,idempotency_key),
 CONSTRAINT ck_transfer_different CHECK(source_account_id<>target_account_id)
);
ALTER TABLE account_movements ADD (transfer_id NUMBER);
ALTER TABLE account_movements ADD CONSTRAINT fk_movement_transfer FOREIGN KEY(transfer_id) REFERENCES transfers(id);
