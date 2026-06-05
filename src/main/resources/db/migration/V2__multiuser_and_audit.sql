-- V2: Multi-user system and audit log

-- -------------------------------------------------------
-- Table: users
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    username            VARCHAR(100)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    full_name           VARCHAR(255),
    role                VARCHAR(20)   NOT NULL CHECK(role IN ('ADMIN','COLABORADOR')),
    active              BOOLEAN       NOT NULL DEFAULT 1,
    must_change_password BOOLEAN      NOT NULL DEFAULT 0,
    created_at          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    last_login          TIMESTAMP
);

-- -------------------------------------------------------
-- Table: audit_log
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER      REFERENCES users(id),
    action      VARCHAR(50)  NOT NULL,
    entity_name VARCHAR(100),
    entity_id   VARCHAR(50),
    details     TEXT,
    timestamp   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------
-- Indexes
-- -------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username
    ON users(username);

CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp
    ON audit_log(user_id, timestamp);

-- -------------------------------------------------------
-- Add user tracking to existing tables (nullable FK)
-- -------------------------------------------------------
ALTER TABLE producto ADD COLUMN created_by_user_id INTEGER REFERENCES users(id);
ALTER TABLE compra   ADD COLUMN created_by_user_id INTEGER REFERENCES users(id);

-- -------------------------------------------------------
-- NOTE: Default admin user is seeded at runtime by
--       DataInitializer (Spring @PostConstruct) using
--       BCryptPasswordEncoder to avoid storing a
--       pre-computed hash in version control.
--       Credentials: admin / Admin123*
--       The user will be prompted to change password
--       on first login (must_change_password = 1).
-- -------------------------------------------------------
