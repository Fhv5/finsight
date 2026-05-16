CREATE TABLE tokens
(
    id                            UUID                        NOT NULL,
    jti                           UUID                 NOT NULL UNIQUE,
    refresh_token                 VARCHAR(255)         NOT NULL UNIQUE,
    token_type                    VARCHAR(255)                NOT NULL,
    user_id                       UUID                        NOT NULL,
    revoked                       BOOLEAN       NOT NULL DEFAULT FALSE,
    access_token_expires_at       TIMESTAMPTZ                  NOT NULL,
    refresh_token_expires_at      TIMESTAMPTZ                  NOT NULL,
    created_at                    TIMESTAMPTZ             DEFAULT NOW(),

    CONSTRAINT pk_auth_tokens PRIMARY KEY (id),
    CONSTRAINT fk_auth_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_auth_tokens_token_id ON tokens (jti);
CREATE INDEX idx_auth_tokens_user_id ON tokens (user_id);
CREATE INDEX idx_auth_tokens_refresh_token_id ON tokens(refresh_token);
