-- ============================================================
-- DATABASE VERIFICATION SCRIPT
-- Run this line by line to check what's happening
-- ============================================================

-- Step 1: Check if javablog database exists
SHOW DATABASES;

-- Step 2: Try to use it
USE javablog;

-- Step 3: Check if tables exist
SHOW TABLES;

-- Step 4: Check posts table structure
DESCRIBE posts;

-- Step 5: Count posts
SELECT COUNT(*) AS total_posts FROM posts;

-- Step 6: Show all posts
SELECT * FROM posts;

-- Step 7: Count comments
SELECT COUNT(*) AS total_comments FROM comments;

-- Step 8: Show all comments
SELECT * FROM comments;

-- If posts table is empty, let's add data:
INSERT INTO posts (title, content, author, user_id, image_path) 
VALUES 
('Test Post 1', 'This is a test post', 'Admin', 1, NULL),
('Test Post 2', 'Another test post', 'User', 1, NULL),
('Test Post 3', 'Third test post', 'Expert', 1, NULL);

-- Verify it worked
SELECT * FROM posts;
