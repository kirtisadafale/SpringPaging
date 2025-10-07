-- Add correlation_id column to movie_audit for traceability
-- This migration is idempotent: it will add the column only if it doesn't exist.

-- MySQL syntax
ALTER TABLE movie_audit
  ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(255);

-- For databases that do not support IF NOT EXISTS in ALTER TABLE (older MySQL versions),
-- the Flyway baseline should be adjusted or use a DB-specific wrapper. The above works on
-- MySQL 8+ and H2 (in many versions) used for tests.
