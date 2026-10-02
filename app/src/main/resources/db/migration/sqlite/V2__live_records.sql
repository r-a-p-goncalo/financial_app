ALTER TABLE financial_contexts ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1;
ALTER TABLE accounts ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1;
ALTER TABLE transactions ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1;

CREATE INDEX financial_contexts_live ON financial_contexts (is_live);
CREATE INDEX accounts_context_live ON accounts (financial_context_id, is_live);
CREATE INDEX transactions_context_live ON transactions (financial_context_id, is_live);
