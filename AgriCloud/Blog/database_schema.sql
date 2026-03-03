-- JavaBlog Database Schema WITH IMAGE SUPPORT
DROP DATABASE IF EXISTS javablog;
CREATE DATABASE javablog;
USE javablog;

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Posts table WITH image_path
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

-- Comments table
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

-- Indexes for performance
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Insert test user
INSERT INTO users (username, email, password) 
VALUES ('admin', 'admin@agricloudtest.com', 'test123');

-- Insert sample posts (without images)
INSERT INTO posts (title, content, author, user_id, image_path, created_at, updated_at) 
VALUES 
(
    'Welcome to AgriCloud Blog!',
    'This is your first blog post. AgriCloud is a comprehensive platform for farmers and agricultural professionals to share knowledge, experiences, and best practices.',
    'Admin',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Sustainable Farming Practices',
    'Sustainable agriculture is essential. Key practices include crop rotation, organic pest control, water conservation, and soil health management.',
    'AgriExpert',
    1,
    NULL,
    NOW(),
    NOW()
),
(
    'Technology in Agriculture',
    'Precision agriculture uses GPS, sensors, and data analytics to optimize yields and reduce waste. Monitor soil, weather, and plant health in real-time.',
    'TechFarmer',
    1,
    NULL,
    NOW(),
    NOW()
);

-- Insert sample comments
INSERT INTO comments (content, author, post_id, user_id, created_at, updated_at)
VALUES
('Great post! Looking forward to more content.', 'John Farmer', 1, 1, NOW(), NOW()),
('Very helpful tips. Will implement on my farm.', 'Sarah Green', 2, 1, NOW(), NOW()),
('Do you have more info about GPS for small farms?', 'Mike Johnson', 3, 1, NOW(), NOW());

-- Verify
SELECT 'Setup Complete!' AS Status;
SELECT COUNT(*) AS Posts FROM posts;
SELECT COUNT(*) AS Comments FROM comments;
