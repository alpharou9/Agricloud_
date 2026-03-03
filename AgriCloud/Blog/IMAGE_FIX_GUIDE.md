# 🌿 AgriCloud - GREEN THEME + IMAGE FIX

## ✅ WHAT'S FIXED

### 1. GREEN FARM THEME
- Light green background (#f0fdf4)
- Green buttons, borders, accents (#16a34a)
- Green hover effects
- Green stats badges
- Perfect for agriculture/farming!

### 2. IMAGE DISPLAY DEBUGGING
Added detailed console messages to track image upload

---

## 🚀 SETUP

### Step 1: Database
```sql
DROP DATABASE IF EXISTS javablog;
CREATE DATABASE javablog;
USE javablog;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    email VARCHAR(100),
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    user_id INT NOT NULL,
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
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
('Welcome', 'AgriCloud blog', 'Admin', 1);
```

### Step 2: Build
```bash
mvn clean install
mvn javafx:run
```

You MUST see:
```
🌿 LOADED GREEN FARM THEME!
✅ AgriCloud Blog is ready
```

---

## 🔍 DEBUG IMAGE UPLOAD

### When You Upload an Image, Console Should Show:

```
📸 Image selected: C:\path\to\your\image.jpg
✅ Image saved to: uploads/post_images/abc-123.jpg
📝 Creating post with image path: uploads/post_images/abc-123.jpg
✅ Post created successfully with ID: 5
🖼️ Post #5 has image path: uploads/post_images/abc-123.jpg
✅ Image file exists, loading...
📁 Image file: C:\full\path\uploads\post_images\abc-123.jpg
📏 Image dimensions: 1920x1080
✅ Image added to card!
```

### If You DON'T See These Messages:

**Problem 1: No "📸 Image selected" message**
→ File chooser isn't working
→ Make sure you clicked "Choose Image" button

**Problem 2: "❌ Image save failed"**
→ File permissions issue
→ Check if `uploads/post_images/` folder can be created

**Problem 3: "ℹ️ Post #X has no image"**
→ Image path wasn't saved to database
→ Run this SQL:
```sql
SELECT id, title, image_path FROM posts WHERE id = X;
```

**Problem 4: "❌ Image file does not exist"**
→ File was saved to database but not to disk
→ Check if file exists in: `AgriCloud_FIXED/uploads/post_images/`

---

## 📊 CHECK DATABASE

Run this in MySQL Workbench:
```sql
USE javablog;
SELECT id, title, author, image_path FROM posts ORDER BY id DESC LIMIT 5;
```

Look at `image_path` column:
- **NULL** = Image never saved
- **"uploads/post_images/abc.jpg"** = Image saved, check if file exists

---

## 🎨 GREEN THEME FEATURES

- Light green background everywhere
- Green buttons and badges
- Green borders on forms
- Green hover effects
- Green scrollbars
- Perfect agricultural aesthetic!

---

## 💡 FORCE CLEAN BUILD

If you still see old design:
```bash
rm -rf target/
mvn clean compile javafx:run
```

The console messages will tell us EXACTLY where the image upload fails!
