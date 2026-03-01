# 🚀 JavaBlog Quick Start Guide

## Step 1: Set Up Database

1. Open MySQL Workbench or MySQL command line
2. Run the following commands:

```sql
-- Create database
CREATE DATABASE javablog;

-- Use the database
USE javablog;

-- Run the schema file
SOURCE /path/to/database_schema.sql;
```

Or simply run:
```bash
mysql -u root -p < database_schema.sql
```

## Step 2: Configure Database Connection

1. Navigate to: `src/main/java/esprit/rania/config/DatabaseConfig.java`
2. Update if your MySQL password is different:

```java
public static final String DB_PASS = "your_mysql_password";
```

## Step 3: Build the Project

Using Maven:
```bash
mvn clean install
```

## Step 4: Test Database Connection

Run the test classes to verify everything is working:

### Test Posts:
```bash
mvn exec:java -Dexec.mainClass="esprit.rania.test.PostServiceTest"
```

### Test Comments:
```bash
mvn exec:java -Dexec.mainClass="esprit.rania.test.CommentServiceTest"
```

## Step 5: Run the Application

### Option A - Using Maven:
```bash
mvn clean javafx:run
```

### Option B - Using IntelliJ IDEA:
1. Right-click on `Main.java`
2. Select "Run 'Main'"

## 📂 Project Package Structure

```
esprit.rania
├── config          → Database configuration
├── controller      → JavaFX controllers
├── database        → Database connection manager
├── models          → Entity classes (Post, Comment)
├── services        → Business logic (PostService, CommentService)
├── utilities       → Helper classes (Alerts, Validation, Formatting)
└── test           → Test classes
```

## 🎯 What You Have

### Models (entities):
- ✅ **Post.java** - Blog post entity
- ✅ **Comment.java** - Comment entity

### Services (business logic):
- ✅ **PostService.java** - CRUD operations for posts
- ✅ **CommentService.java** - CRUD operations for comments

### Controllers (UI):
- ✅ **PostController.java** - Manage posts view
- ✅ **CommentController.java** - Manage comments view

### Utilities:
- ✅ **AlertHelper.java** - Show alerts/dialogs
- ✅ **DateFormatter.java** - Format dates and times
- ✅ **Validator.java** - Validate user inputs

### Database:
- ✅ **DatabaseConnection.java** - Manage DB connections
- ✅ **DatabaseConfig.java** - DB configuration
- ✅ **database_schema.sql** - Database schema with sample data

## 📝 Next Steps (What You Need to Do)

1. **Create FXML Files** for the UI:
   - `MainView.fxml` - Main application window
   - `PostView.fxml` - Posts management view
   - `CommentView.fxml` - Comments management view

2. **Design the UI** using Scene Builder or manually

3. **Link Controllers to FXML** files

4. **Test the Application** with different operations

5. **Add Custom Features** based on your requirements

## 🔧 Common Commands

### Clean and build:
```bash
mvn clean package
```

### Run tests:
```bash
mvn test
```

### Run application:
```bash
mvn javafx:run
```

## ❓ Troubleshooting

### MySQL Connection Error?
- Check if MySQL is running: `sudo service mysql status`
- Verify credentials in `DatabaseConfig.java`
- Ensure database `javablog` exists

### JavaFX Error?
- Ensure Java 17 is installed
- Run with Maven: `mvn javafx:run`

### Build Error?
- Clean Maven cache: `mvn clean`
- Re-import Maven project in IDE

## 📚 Useful Resources

- JavaFX Documentation: https://openjfx.io/
- MySQL Documentation: https://dev.mysql.com/doc/
- Maven Documentation: https://maven.apache.org/guides/

---

**You're all set! 🎉**

All the backend code is ready. You just need to create the FXML files for the user interface!
