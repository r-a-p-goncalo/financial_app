CREATE TABLE users (
    user_id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    password_hashing_strategy TEXT,
    password_hash TEXT,
    CHECK (
        (password_hashing_strategy IS NULL AND password_hash IS NULL)
        OR (password_hashing_strategy IS NOT NULL AND password_hash IS NOT NULL)
    )
);

CREATE TABLE financial_contexts (
    financial_context_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    parent_financial_context_id TEXT,
    overridden_attributes INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (parent_financial_context_id)
        REFERENCES financial_contexts(financial_context_id)
);

CREATE TABLE financial_context_permissions (
    financial_context_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    permission TEXT NOT NULL CHECK (permission IN ('READ', 'WRITE', 'OWNER')),
    granted_by_user_id TEXT NOT NULL,
    granted_at TEXT NOT NULL,
    PRIMARY KEY (financial_context_id, user_id),
    FOREIGN KEY (financial_context_id)
        REFERENCES financial_contexts(financial_context_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (granted_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE accounts (
    financial_context_id TEXT NOT NULL,
    account_id TEXT NOT NULL,
    name TEXT NOT NULL,
    initial_amount TEXT NOT NULL,
    parent_financial_context_id TEXT,
    parent_account_id TEXT,
    overridden_attributes INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (financial_context_id, account_id),
    FOREIGN KEY (financial_context_id)
        REFERENCES financial_contexts(financial_context_id),
    FOREIGN KEY (parent_financial_context_id, parent_account_id)
        REFERENCES accounts(financial_context_id, account_id)
);

CREATE TABLE transactions (
    financial_context_id TEXT NOT NULL,
    transaction_id TEXT NOT NULL,
    origin_account_id TEXT,
    target_account_id TEXT,
    date_time TEXT NOT NULL,
    value TEXT NOT NULL,
    parent_financial_context_id TEXT,
    parent_transaction_id TEXT,
    overridden_attributes INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (financial_context_id, transaction_id),
    FOREIGN KEY (financial_context_id)
        REFERENCES financial_contexts(financial_context_id),
    FOREIGN KEY (financial_context_id, origin_account_id)
        REFERENCES accounts(financial_context_id, account_id),
    FOREIGN KEY (financial_context_id, target_account_id)
        REFERENCES accounts(financial_context_id, account_id),
    FOREIGN KEY (parent_financial_context_id, parent_transaction_id)
        REFERENCES transactions(financial_context_id, transaction_id)
);

CREATE INDEX financial_contexts_by_parent
    ON financial_contexts (parent_financial_context_id);

CREATE INDEX financial_context_permissions_by_user
    ON financial_context_permissions (user_id);

CREATE INDEX accounts_by_parent
    ON accounts (parent_financial_context_id, parent_account_id);

CREATE INDEX transactions_by_origin_account
    ON transactions (financial_context_id, origin_account_id);

CREATE INDEX transactions_by_target_account
    ON transactions (financial_context_id, target_account_id);

CREATE INDEX transactions_by_parent
    ON transactions (parent_financial_context_id, parent_transaction_id);
