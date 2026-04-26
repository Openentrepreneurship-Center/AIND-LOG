CREATE INDEX idx_payments_pg_transaction_id ON payments (pg_transaction_id) WHERE pg_transaction_id IS NOT NULL;
