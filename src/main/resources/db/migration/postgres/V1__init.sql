CREATE TABLE accounts
(
    id          UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    balance     BIGINT       NOT NULL,
    user_id     UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);

CREATE TABLE category
(
    id         UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(255) NOT NULL,
    user_id    UUID         NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE transaction
(
    id                     UUID                        NOT NULL,
    date_issued            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type                   SMALLINT                    NOT NULL,
    amount                 BIGINT                      NOT NULL,
    description            VARCHAR(255)                NOT NULL,
    user_id                UUID                        NOT NULL,
    origin_account_id      UUID,
    destination_account_id UUID,
    category_id            UUID,
    created_at             TIMESTAMP WITHOUT TIME ZONE,
    updated_at             TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_transaction PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE accounts
    ADD CONSTRAINT FK_ACCOUNTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE category
    ADD CONSTRAINT FK_CATEGORY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_DESTINATION_ACCOUNT FOREIGN KEY (destination_account_id) REFERENCES accounts (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_ORIGIN_ACCOUNT FOREIGN KEY (origin_account_id) REFERENCES accounts (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);