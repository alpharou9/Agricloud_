# 🌿 AgriCloud Blog - Final Clean Version

## ⚠️ IMPORTANT - To See The New Design

You MUST completely delete your old project and start fresh:

### Step 1: Close IntelliJ completely

### Step 2: Delete your old JavaBlog folder
Delete the entire old project folder from your computer.

### Step 3: Extract THIS zip to a NEW location

### Step 4: Database Setup
Run this in MySQL Workbench:

```sql
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
    image_path VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users VALUES (1, 'admin', 'admin@agricloud.com', 'pass', NOW());

INSERT INTO posts (title, content, author, user_id) VALUES 
('Welcome', 'Your blog platform', 'Admin', 1),
('Farming', 'Tips and tricks', 'Expert', 1);
```

### Step 5: Open In IntelliJ
- File → Open → Select the NEW extracted folder
- Wait for Maven to import

### Step 6: Build
```bash
mvn clean install
mvn javafx:run
```

## 🎨 What's New

The design is COMPLETELY DIFFERENT:
- Modern card layout
- Bigger titles
- Better spacing
- Hover effects
- Same green AgriCloud colors

## 🔍 Verify New Design Loaded

When app starts, you should see:
- Light gray background (#fafafa)
- Large title text
- Modern card shadows
- Different look than your screenshot

If it looks THE SAME, IntelliJ is using cached files!

## 💡 Force Clean Build

If design doesn't change:
```bash
rm -rf target/
mvn clean compile
mvn javafx:run
```
