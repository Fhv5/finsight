CREATE TABLE budgets
(
    id                     UUID                        NOT NULL,
    limit_amount            BIGINT                      NOT NULL,
    user_id                UUID                        NOT NULL,
    category_id            UUID,
    created_at             TIMESTAMP WITHOUT TIME ZONE,
    updated_at             TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_budget PRIMARY KEY (id)
);

ALTER TABLE budgets
    ADD CONSTRAINT FK_BUDGET_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE budgets
    ADD CONSTRAINT FK_BUDGET_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);