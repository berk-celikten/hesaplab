-- Metadata kontrolü sonrası ayrı bağlantıda bir kez; otomatik SQL başlangıcı yok.
CREATE TABLE receipt_outbox (
 event_id VARCHAR2(36 CHAR) PRIMARY KEY,
 transfer_id NUMBER NOT NULL REFERENCES transfers(id),
 request_no NUMBER(10) NOT NULL,
 event_type VARCHAR2(30 CHAR) DEFAULT 'PREPARE_RECEIPT' NOT NULL,
 status VARCHAR2(10 CHAR) DEFAULT 'PENDING' NOT NULL,
 attempts NUMBER(10) DEFAULT 0 NOT NULL,
 next_attempt_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
 sent_at TIMESTAMP WITH TIME ZONE,
 last_error VARCHAR2(100 CHAR),
 CONSTRAINT uq_outbox_request UNIQUE (transfer_id,request_no),
 CONSTRAINT ck_outbox_request CHECK (request_no>=1),
 CONSTRAINT ck_outbox_type CHECK (event_type='PREPARE_RECEIPT'),
 CONSTRAINT ck_outbox_status CHECK (status IN ('PENDING','SENT')),
 CONSTRAINT ck_outbox_attempts CHECK (attempts>=0),
 CONSTRAINT ck_outbox_sent CHECK ((status='PENDING' AND sent_at IS NULL) OR (status='SENT' AND sent_at IS NOT NULL AND last_error IS NULL))
);
CREATE INDEX ix_outbox_due ON receipt_outbox(status,next_attempt_at,created_at);
CREATE UNIQUE INDEX uq_outbox_pending ON receipt_outbox(CASE WHEN status='PENDING' THEN transfer_id END);
