-- ============================================================
-- DATABASE DIAGNOSTIC SCRIPT
-- Run this to check if everything is set up correctly
-- ============================================================

-- Check if database exists
SHOW DATABASES LIKE 'javablog';

-- Use the database
USE javablog;

-- Check if tables exist
SHOW TABLES;

-- Check posts table structure (should include image_path column)
DESCRIBE posts;

-- Check how many posts exist
SELECT COUNT(*) AS total_posts FROM posts;

-- Check how many comments exist
SELECT COUNT(*) AS total_comments FROM comments;

-- Show all posts (to verify data)
SELECT 
    id, 
    title, 
    author, 
    image_path,
    CHAR_LENGTH(content) AS content_length,
    created_at 
FROM posts 
ORDER BY created_at DESC;

-- Show all comments
SELECT 
    c.id,
    c.content,
    c.author,
    p.title AS post_title,
    c.created_at
FROM comments c
LEFT JOIN posts p ON c.post_id = p.id
ORDER BY c.created_at DESC;

-- Check for NULL image_path (this is OK)
SELECT COUNT(*) AS posts_without_images 
FROM posts 
WHERE image_path IS NULL;

-- Final status
SELECT 
    'Database is ready!' AS Status,
    (SELECT COUNT(*) FROM posts) AS Posts,
    (SELECT COUNT(*) FROM comments) AS Comments,
    (SELECT COUNT(*) FROM users) AS Users;
