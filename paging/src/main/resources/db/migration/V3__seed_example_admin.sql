-- Flyway seed migration: INSERT an example admin user.
-- WARNING: This file uses a placeholder BCrypt hash. Replace '<bcrypt-hash>' with a real BCrypt hash
-- generated using the included BcryptGenerator before applying to production.

INSERT INTO users (username, password, enabled) VALUES ('admin', '$2a$10$Vw0/G.nRiYDExn/.FSceg.pKcyDJurCEMKygGuDMFbZ2dlmq8FY/y', true);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');

-- IMPORTANT: Do NOT commit real passwords in plaintext into source-controlled migrations.
