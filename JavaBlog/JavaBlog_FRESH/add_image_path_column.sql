-- ============================================================
-- Database Migration: Add image_path column to posts table
-- Run this if you already have data and want to keep it
-- ============================================================

USE javablog;

-- Check if column exists, if not add it
ALTER TABLE posts ADD COLUMN IF NOT EXISTS image_path VARCHAR(500);

-- Verify the change
DESCRIBE posts;

-- Show sample of posts table
SELECT id, title, author, image_path, created_at FROM posts LIMIT 5;
