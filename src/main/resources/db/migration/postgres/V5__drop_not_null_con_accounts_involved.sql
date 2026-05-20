ALTER TABLE transaction
    ALTER COLUMN origin_account_id DROP NOT NULL;

ALTER TABLE transaction
    ALTER COLUMN destination_account_id DROP NOT NULL;