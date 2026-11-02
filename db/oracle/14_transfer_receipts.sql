-- Metadata kontrolünden sonra, ayrı bağlantıda yalnız bir kez uygulanır.
-- Eski transferler/açılış hareketleri için geriye dönük kayıt üretilmez.
CREATE TABLE transfer_receipts (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transfer_id NUMBER NOT NULL,
    status VARCHAR2(10 CHAR) DEFAULT 'PENDING' NOT NULL,
    content CLOB,
    error_message VARCHAR2(200 CHAR),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uq_receipt_transfer UNIQUE (transfer_id),
    CONSTRAINT fk_receipt_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id),
    CONSTRAINT ck_receipt_status CHECK (status IN ('PENDING', 'READY', 'FAILED')),
    CONSTRAINT ck_receipt_result CHECK (
        (status = 'READY' AND content IS NOT NULL AND error_message IS NULL)
        OR (status = 'PENDING' AND content IS NULL AND error_message IS NULL)
        OR (status = 'FAILED' AND content IS NULL AND error_message IS NOT NULL)
    )
);
