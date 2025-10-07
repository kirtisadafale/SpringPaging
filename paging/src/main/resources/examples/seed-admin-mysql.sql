-- Example MySQL script to insert an admin user with a BCrypt-hashed password.
-- Replace <bcrypt-hash> with the output from BcryptGenerator.

USE paging_db;

INSERT INTO users (username, password, enabled) VALUES ('admin', '$2a$10$Vw0/G.nRiYDExn/.FSceg.pKcyDJurCEMKygGuDMFbZ2dlmq8FY/y', true);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
