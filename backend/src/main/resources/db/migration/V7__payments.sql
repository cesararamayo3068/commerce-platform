CREATE TABLE payments (
 id BIGSERIAL PRIMARY KEY,
 order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 outcome VARCHAR(20),
 amount NUMERIC(12,2) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT ck_payment_status CHECK (status IN ('PENDING','APPROVED','REJECTED')),
 CONSTRAINT ck_payment_outcome CHECK (outcome IS NULL OR outcome IN ('APPROVED','REJECTED')),
 CONSTRAINT ck_payment_amount CHECK (amount >= 0)
);
CREATE INDEX idx_payments_status ON payments(status);
-- Historical orders are intentionally not backfilled: their prior lifecycle remains valid.
