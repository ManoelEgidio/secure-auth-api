
CREATE TABLE user_authorities
(
    user_id   UUID         NOT NULL,
    authority VARCHAR(255) NOT NULL
);

CREATE TABLE users
(
    id         UUID         NOT NULL,
    created_at date         NOT NULL,
    updated_at date,
    created_by UUID,
    updated_by UUID,
    name       VARCHAR(255) NOT NULL,
    login      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,
    enabled    BOOLEAN,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_login UNIQUE (login);

CREATE INDEX idx_enabled ON users (enabled);

CREATE INDEX idx_login ON users (login);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_CREATED_BY FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_UPDATED_BY FOREIGN KEY (updated_by) REFERENCES users (id);

ALTER TABLE user_authorities
    ADD CONSTRAINT fk_user_authorities_on_user FOREIGN KEY (user_id) REFERENCES users (id);