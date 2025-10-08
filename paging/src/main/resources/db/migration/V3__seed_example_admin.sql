-- Flyway seed migration: INSERT an example admin user.
-- WARNING: This file uses a placeholder BCrypt hash. Replace '<bcrypt-hash>' with a real BCrypt hash
-- generated using the included BcryptGenerator before applying to production.

INSERT INTO users (username, password, enabled) VALUES ('admin', '$2a$10$vptzEqp/MUENnlI.mbW7ZeubGHNYq5YxGbYkUIFBPzkOMi9qeQFxa', true);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');

-- IMPORTANT: Do NOT commit real passwords in plaintext into source-controlled migrations.
