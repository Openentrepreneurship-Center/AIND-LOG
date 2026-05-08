-- Remove status column from posts table
DROP INDEX IF EXISTS idx_posts_status;
ALTER TABLE posts DROP COLUMN status;
