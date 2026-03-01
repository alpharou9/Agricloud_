-- Simple working database schema WITHOUT images
DROP DATABASE IF EXISTS javablog;
CREATE DATABASE javablog;
USE javablog;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

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

INSERT INTO users (username, email, password) VALUES ('admin', 'admin@test.com', 'test123');

INSERT INTO posts (title, content, author, user_id) VALUES 
('Welcome to AgriCloud', 'This is your first blog post!', 'Admin', 1),
('Farming Tips', 'Great tips for sustainable farming', 'Expert', 1),
('Tech in Agriculture', 'How technology is changing farming', 'TechGuru', 1);

INSERT INTO comments (content, author, post_id, user_id) VALUES
('Great post!', 'Reader1', 1, 1),
('Very helpful', 'Reader2', 2, 1);

SELECT * FROM posts;
SELECT * FROM comments;
