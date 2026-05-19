ALTER TABLE category
    ADD CONSTRAINT uq_categories_user_name_type UNIQUE (user_id, name, type)