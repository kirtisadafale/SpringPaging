CREATE DATABASE IF NOT EXISTS paging_db;
CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'mypassword';
GRANT ALL PRIVILEGES ON paging_db.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;



--verify movie_audit table structure
-- In MySQL, you can use:
use paging_db;
SHOW COLUMNS FROM movie_audit;
-- or
DESC movie_audit;
-- or
SELECT COLUMN_NAME, DATA_TYPE
FROM information_schema.columns
WHERE table_schema = '<db>' AND table_name = 'movie_audit';