ALTER TABLE transaction
    DROP COLUMN type;

ALTER TABLE transaction
    ADD type VARCHAR(255) NOT NULL;