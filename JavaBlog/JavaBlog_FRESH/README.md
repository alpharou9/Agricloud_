# JavaBlog - Blog Management System

A JavaFX-based blog management application for creating and managing blog posts and comments.

## 📋 Project Overview

JavaBlog is a desktop application built with JavaFX that allows users to:
- Create, read, update, and delete blog posts
- Add and manage comments on posts
- Search posts by title
- View post and comment statistics

## 🏗️ Project Structure

```
JavaBlog/
├── src/
│   └── main/
│       ├── java/
│       │   └── esprit/
│       │       └── rania/
│       │           ├── config/
│       │           │   └── DatabaseConfig.java
│       │           ├── controller/
│       │           │   ├── PostController.java
│       │           │   └── CommentController.java
│       │           ├── database/
│       │           │   └── DatabaseConnection.java
│       │           ├── models/
│       │           │   ├── Post.java
│       │           │   └── Comment.java
│       │           ├── services/
│       │           │   ├── PostService.java
│       │           │   └── CommentService.java
│       │           ├── utilities/
│       │           │   ├── AlertHelper.java
│       │           │   ├── DateFormatter.java
│       │           │   └── Validator.java
│       │           └── Main.java
│       └── resources/
│           └── fxml/
│               ├── MainView.fxml
│               ├── PostView.fxml
│               └── CommentView.fxml
├── pom.xml
└── database_schema.sql
```

## 🔧 Technologies Used

- **Java 17**
- **JavaFX 17.0.2** - UI Framework
- **MySQL 9.2.0** - Database
- **Maven** - Build Tool
- **AtlantaFX** - Modern UI Theme

## 📦 Dependencies

- MySQL Connector Java
- JavaFX Controls, FXML, and Web
- jBCrypt (for password hashing)
- JavaMail
- AtlantaFX Base

## 🚀 Getting Started

### Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
2. **MySQL Server** installed and running
3. **Maven** installed
4. **IntelliJ IDEA** (or any Java IDE)

### Database Setup

1. Open MySQL Workbench or MySQL command line
2. Run the SQL script located at `database_schema.sql`:
   ```bash
   mysql -u root -p < database_schema.sql
   ```
3. This will create:
   - Database named `javablog`
   - Tables: `users`, `posts`, `comments`
   - Sample data for testing

### Configuration

1. Open `src/main/java/esprit/rania/config/DatabaseConfig.java`
2. Update database credentials if needed:
   ```java
   public static final String DB_USER = "root";
   public static final String DB_PASS = "your_password";
   ```

### Running the Application

#### Using IntelliJ IDEA:
1. Open the project in IntelliJ IDEA
2. Wait for Maven to download dependencies
3. Right-click on `Main.java` and select "Run 'Main'"

#### Using Maven Command Line:
```bash
mvn clean javafx:run
```

## 📚 Package Documentation

### 1. **config** Package
- **DatabaseConfig.java**: Contains database connection configuration
  - Database URL
  - Database credentials
  - Connection parameters

### 2. **database** Package
- **DatabaseConnection.java**: Manages database connections
  - Singleton connection pattern
  - Connection pooling
  - Connection lifecycle management

### 3. **models** Package
- **Post.java**: Entity class for blog posts
  - Properties: id, title, content, author, userId, createdAt, updatedAt
  - Getters/Setters and toString()

- **Comment.java**: Entity class for comments
  - Properties: id, content, author, postId, userId, createdAt, updatedAt
  - Getters/Setters and toString()

### 4. **services** Package
- **PostService.java**: Business logic for posts
  - CRUD operations (Create, Read, Update, Delete)
  - Search functionality
  - Get posts by user

- **CommentService.java**: Business logic for comments
  - CRUD operations
  - Get comments by post
  - Get comment count

### 5. **controller** Package
- **PostController.java**: JavaFX controller for post management
  - Add/Update/Delete posts
  - Search posts
  - Table view management

- **CommentController.java**: JavaFX controller for comment management
  - Add/Update/Delete comments
  - Post selection
  - Comment filtering

### 6. **utilities** Package
- **AlertHelper.java**: JavaFX alert utilities
  - Success/Error/Warning/Info alerts
  - Confirmation dialogs

- **DateFormatter.java**: Date/time formatting utilities
  - Format date and time
  - Relative time (e.g., "2 hours ago")

- **Validator.java**: Input validation utilities
  - Validate post title and content
  - Validate comment content
  - Email and text validation

## 🗃️ Database Schema

### Users Table
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- username (VARCHAR)
- email (VARCHAR)
- password (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Posts Table
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- title (VARCHAR)
- content (TEXT)
- author (VARCHAR)
- user_id (INT, FOREIGN KEY)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Comments Table
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- content (TEXT)
- author (VARCHAR)
- post_id (INT, FOREIGN KEY)
- user_id (INT, FOREIGN KEY)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## ✨ Features

### Post Management
- ✅ Create new blog posts
- ✅ View all posts in a table
- ✅ Update existing posts
- ✅ Delete posts (with cascade delete for comments)
- ✅ Search posts by title
- ✅ View post details

### Comment Management
- ✅ Add comments to posts
- ✅ View all comments for a post
- ✅ Update comments
- ✅ Delete comments
- ✅ Display comment count

### Validation
- ✅ Title: 3-200 characters
- ✅ Post content: 10-10,000 characters
- ✅ Comment content: 1-1,000 characters
- ✅ Author name validation

### UI Features
- ✅ Modern AtlantaFX theme
- ✅ Responsive table views
- ✅ Date/time formatting
- ✅ User-friendly alerts
- ✅ Search functionality

## 🎯 Future Enhancements

- [ ] User authentication and authorization
- [ ] Rich text editor for post content
- [ ] Image upload for posts
- [ ] Categories and tags
- [ ] Post statistics and analytics
- [ ] Export posts to PDF
- [ ] Email notifications
- [ ] Dark/Light theme toggle

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check if MySQL server is running
   - Verify database credentials in DatabaseConfig.java
   - Ensure `javablog` database exists

2. **JavaFX Runtime Components Missing**
   - Make sure JavaFX SDK is properly configured
   - Use Maven to run: `mvn clean javafx:run`

3. **FXML Not Found**
   - Verify FXML files are in `src/main/resources/fxml/`
   - Check file paths in controller classes

## 📝 Notes

- The default user ID is set to 1 for all operations
- Update this in controllers when implementing user authentication
- Database tables use CASCADE DELETE for referential integrity
- All timestamps are stored in UTC timezone

## 👥 Contributors

- Your Name (Module: Blog - Posts & Comments)

## 📄 License

This project is created for educational purposes.
