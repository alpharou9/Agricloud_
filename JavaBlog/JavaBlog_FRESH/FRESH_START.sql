-- ============================================================
-- COMPLETE FRESH START - GUARANTEED TO WORK
-- Copy this ENTIRE script and run it all at once in MySQL Workbench
-- ============================================================

-- Drop everything
DROP DATABASE IF EXISTS javablog;

-- Create fresh database
CREATE DATABASE javablog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use it
USE javablog;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create posts table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create comments table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert test user (REQUIRED for posts to work)
INSERT INTO users (id, username, email, password) 
VALUES (1, 'admin', 'admin@test.com', 'test123');

-- Insert posts (using the user_id = 1)
INSERT INTO posts (title, content, author, user_id, image_path, created_at, updated_at) 
VALUES 
(
    'Welcome to AgriCloud Blog',
    'This is your first blog post. Welcome to the AgriCloud blogging platform! Start writing and sharing your agricultural knowledge with the community.',
    'Admin',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Top 10 Sustainable Farming Techniques',
    'Learn about the most effective sustainable farming methods: crop rotation, composting, integrated pest management, water conservation, and more. These practices help preserve our environment for future generations.',
    'AgriExpert',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'The Future of Smart Agriculture',
    'Technology is revolutionizing farming. From drones and sensors to AI-powered analytics, discover how precision agriculture is changing the way we grow food and manage resources.',
    'TechFarmer',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Organic Pest Control Methods',
    'Natural ways to control pests without harmful chemicals. Learn about beneficial insects, companion planting, neem oil, and other organic solutions that work.',
    'GreenGuru',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Water Management in Dry Climates',
    'Effective irrigation strategies for water-scarce regions. Drip irrigation, mulching, rainwater harvesting, and drought-resistant crops are key to successful farming in arid areas.',
    'WaterWise',
    1,
    NULL,
    NOW(),
    NOW()
);

-- Insert some comments
INSERT INTO comments (content, author, post_id, user_id, created_at, updated_at)
VALUES
('Great introduction! Looking forward to more posts.', 'John Farmer', 1, 1, NOW(), NOW()),
('This is exactly what I needed. Thank you!', 'Sarah Green', 2, 1, NOW(), NOW()),
('Very informative. Can you share more about crop rotation?', 'Mike Fields', 2, 1, NOW(), NOW()),
('The future is here! Amazing technology.', 'Tech Enthusiast', 3, 1, NOW(), NOW()),
('I use neem oil on my farm and it works great!', 'Organic Joe', 4, 1, NOW(), NOW()),
('Do you have tips for rainwater harvesting systems?', 'Desert Dan', 5, 1, NOW(), NOW());

-- Verify everything was created
SELECT '✅ DATABASE CREATED!' AS Status;
SELECT COUNT(*) AS Users FROM users;
SELECT COUNT(*) AS Posts FROM posts;
SELECT COUNT(*) AS Comments FROM comments;

-- Show the posts
SELECT 
    id,
    title,
    author,
    SUBSTRING(content, 1, 50) AS content_preview,
    image_path,
    created_at
FROM posts
ORDER BY created_at DESC;

-- Show database info
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'javablog'
ORDER BY TABLE_NAME;
