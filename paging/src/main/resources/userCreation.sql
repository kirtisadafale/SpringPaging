CREATE DATABASE IF NOT EXISTS paging_db;
CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'mypassword';
GRANT ALL PRIVILEGES ON paging_db.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;
