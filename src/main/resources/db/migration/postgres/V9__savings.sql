ALTER TABLE accounts
    ADD type VARCHAR(255) NOT NULL DEFAULT 'REGULAR';

ALTER TABLE accounts
    ADD target_amount BIGINT;