-- Flyway migration: create tables for Spring Security JdbcUserDetailsManager
-- Creates `users` and `authorities` tables with a simple schema compatible with the defaults

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS authorities (
  username VARCHAR(50) NOT NULL,
  authority VARCHAR(50) NOT NULL,
  CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_authorities_unique ON authorities (username, authority);

-- Example: to seed an admin user, run a SQL insert with a BCrypt-hashed password, for example:
-- INSERT INTO users (username, password, enabled) VALUES ('admin', '<bcrypt-hash-here>', true);
-- INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');

-- NOTE: Do not include plaintext passwords in migrations; use a pre-hashed BCrypt value if seeding users here.
