-- ============================================================
-- COMPLETE DATABASE RESET & INITIALIZATION
-- This script will completely reset your database and add sample posts
-- ============================================================

-- Step 1: Drop and recreate database
DROP DATABASE IF EXISTS javablog;
CREATE DATABASE javablog;
USE javablog;

-- Step 2: Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Step 3: Create posts table WITH image_path column
CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    user_id INT NOT NULL,
    image_path VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 4: Create comments table
CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 5: Create indexes
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Step 6: Insert test user
INSERT INTO users (username, email, password) 
VALUES ('admin', 'admin@agricloudtest.com', 'test123');

-- Step 7: Insert sample posts (with NULL image_path)
INSERT INTO posts (title, content, author, user_id, image_path, created_at, updated_at) 
VALUES 
(
    'Welcome to AgriCloud Blog!',
    'This is your first blog post. AgriCloud is a comprehensive platform for farmers and agricultural professionals to share knowledge, experiences, and best practices. Edit or delete this post to start creating your own content!',
    'Admin',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Sustainable Farming Practices for 2024',
    'Sustainable agriculture is more important than ever. Here are some key practices: crop rotation, organic pest control, water conservation, and soil health management. These techniques help preserve the environment while maintaining productivity.',
    'AgriExpert',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Introduction to Precision Agriculture',
    'Precision agriculture uses technology like GPS, sensors, and data analytics to optimize crop yields and reduce waste. Farmers can now monitor soil conditions, weather patterns, and plant health in real-time to make informed decisions.',
    'TechFarmer',
    1,
    NULL,
    NOW(),
    NOW()
);

-- Step 8: Insert sample comments
INSERT INTO comments (content, author, post_id, user_id, created_at, updated_at)
VALUES
(
    'Great post! Looking forward to more content like this.',
    'John Farmer',
    1,
    1,
    NOW(),
    NOW()
),
(
    'Very helpful tips on crop rotation. I will implement these on my farm.',
    'Sarah Green',
    2,
    1,
    NOW(),
    NOW()
),
(
    'Do you have more information about GPS technology for small farms?',
    'Mike Johnson',
    3,
    1,
    NOW(),
    NOW()
),
(
    'Precision agriculture has transformed my farming operations. Highly recommend!',
    'Lisa Brown',
    3,
    1,
    NOW(),
    NOW()
);

-- Step 9: Verify everything was created
SELECT 'Database Setup Complete!' AS Status;
SELECT COUNT(*) AS TotalPosts FROM posts;
SELECT COUNT(*) AS TotalComments FROM comments;
SELECT id, title, author, image_path FROM posts;
