# AgriCloud - User Management Module (Module 1)

## ⚠️ CRITICAL GIT COMMIT RULE ⚠️

**NEVER include Claude/Anthropic co-authoring in commits!**

When committing code:
- ❌ **DO NOT** add `Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>`
- ❌ **DO NOT** mention AI assistance in commit messages
- ✅ **DO** write commits as if you wrote the code yourself
- ✅ **DO** use standard commit messages like: `feat: Add user login functionality`

**Example of CORRECT commit:**
```bash
git commit -m "feat: Add user authentication with bcrypt password hashing"
```

**Example of WRONG commit (will cause you to fail):**
```bash
git commit -m "feat: Add user authentication

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Project Overview

**Module:** User Management (Module 1)
**Package:** `esprit.farouk`
**Technology:** JavaFX 17+ with MySQL database
**Build Tool:** Maven

---

## Prerequisites

Before starting, ensure you have:
1. **Java Development Kit (JDK) 17 or higher**
2. **Apache Maven 3.6+**
3. **WAMP Server** (for MySQL database)
4. **IntelliJ IDEA** or any Java IDE
5. **Git** for version control

---

## Maven Dependencies

Create a `pom.xml` file in your project root with these dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>esprit.farouk</groupId>
    <artifactId>agricloud</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>AgriCloud User Management</name>
    <description>Smart Farm Management System - User Management Module</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <javafx.version>17.0.2</javafx.version>
    </properties>

    <dependencies>
        <!-- JavaFX Controls -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- JavaFX FXML -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- JavaFX Web (for WebView - used in map picker later) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-web</artifactId>
            <version>${javafx.version}</version>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- BCrypt for password hashing -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>

        <!-- JavaMail API for email functionality -->
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.6.2</version>
        </dependency>

        <!-- JUnit for testing (optional but recommended) -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- JavaFX Maven Plugin -->
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>esprit.farouk.Main</mainClass>
                </configuration>
            </plugin>

            <!-- Maven Shade Plugin (for creating executable JAR) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.4.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>esprit.farouk.Main</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Database Setup

### 1. Start WAMP Server
- Launch WAMP (icon should turn GREEN)
- Ensure both Apache and MySQL services are running

### 2. Database Configuration
```
Host: localhost (127.0.0.1)
Port: 3306
Username: root
Password: (leave empty for default WAMP)
Database Name: agricloud
```

### 3. Create Database via phpMyAdmin

**Option A: Using phpMyAdmin GUI**
1. Open browser: `http://localhost/phpmyadmin`
2. Click "New" in left sidebar
3. Database name: `agricloud`
4. Collation: `utf8mb4_unicode_ci`
5. Click "Create"

**Option B: Using SQL Command**
```sql
CREATE DATABASE agricloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agricloud;
```

### 4. Create Tables

Copy and execute this SQL in phpMyAdmin (SQL tab):

```sql
-- =====================================================
-- Module 1: User Management Tables
-- =====================================================

-- Table: roles
CREATE TABLE roles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    permissions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: users
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    profile_picture VARCHAR(255),
    status ENUM('active', 'inactive', 'blocked') DEFAULT 'active',
    email_verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    INDEX idx_email (email),
    INDEX idx_role_id (role_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: password_resets (for forgot password feature)
CREATE TABLE password_resets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    reset_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_reset_code (reset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name, description, permissions) VALUES
('Admin', 'Full system access with all permissions', '["all"]'),
('Farmer', 'Farm and product management', '["farms", "products", "orders"]'),
('Customer', 'Shopping and order management', '["shop", "cart", "orders"]'),
('Guest', 'Limited read-only access', '["shop_view", "blog_view"]');

-- Insert default admin user (password: admin123)
-- BCrypt hash for 'admin123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (role_id, name, email, password, status) VALUES
(1, 'Admin User', 'admin@agricloud.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'active');

-- Insert test farmer user (password: farmer123)
-- BCrypt hash for 'farmer123': $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO users (role_id, name, email, password, phone, status) VALUES
(2, 'Test Farmer', 'farmer@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567890', 'active');

-- Insert test customer user (password: customer123)
-- BCrypt hash for 'customer123': $2a$10$YC5J0hclRGp0nZ6CYR3t5.JQqmhVHfLGWPx8PdJxRqH3fYE6JVLCe
INSERT INTO users (role_id, name, email, password, phone, status) VALUES
(3, 'Test Customer', 'customer@test.com', '$2a$10$YC5J0hclRGp0nZ6CYR3t5.JQqmhVHfLGWPx8PdJxRqH3fYE6JVLCe', '+0987654321', 'active');
```

### 5. Verify Database Setup

Run this query to confirm:
```sql
-- Check roles
SELECT * FROM roles;

-- Check users
SELECT u.id, u.name, u.email, r.name as role, u.status
FROM users u
JOIN roles r ON u.role_id = r.id;
```

You should see:
- 4 roles: Admin, Farmer, Customer, Guest
- 3 users: Admin, Test Farmer, Test Customer

---

## Project Structure

Create this folder structure in your project:

```
src/
├── main/
│   ├── java/
│   │   └── esprit/
│   │       └── farouk/
│   │           ├── Main.java
│   │           ├── controllers/
│   │           │   ├── LoginController.java
│   │           │   ├── RegisterController.java
│   │           │   ├── DashboardController.java
│   │           │   └── ForgotPasswordController.java
│   │           ├── models/
│   │           │   ├── User.java
│   │           │   └── Role.java
│   │           ├── services/
│   │           │   ├── UserService.java
│   │           │   ├── RoleService.java
│   │           │   └── DatabaseConnection.java
│   │           └── utils/
│   │               ├── ValidationUtils.java
│   │               ├── EmailUtils.java
│   │               └── SessionManager.java
│   └── resources/
│       ├── fxml/
│       │   ├── login.fxml
│       │   ├── register.fxml
│       │   ├── dashboard.fxml
│       │   └── forgot-password.fxml
│       └── css/
│           └── style.css
└── test/
    └── java/
        └── esprit/
            └── farouk/
                └── (test classes)
```

---

## Implementation Guide

### Step 1: Database Connection Class

**File:** `src/main/java/esprit/farouk/services/DatabaseConnection.java`

```java
package esprit.farouk.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "agricloud";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // Empty for default WAMP

    private static final String DB_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("✓ Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Step 2: Model Classes

**File:** `src/main/java/esprit/farouk/models/User.java`

```java
package esprit.farouk.models;

import java.time.LocalDateTime;

public class User {
    private long id;
    private long roleId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String profilePicture;
    private String status; // active, inactive, blocked
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field for display (from JOIN with roles table)
    private String roleName;

    // Empty constructor
    public User() {}

    // Minimal constructor (for registration)
    public User(long roleId, String name, String email, String password) {
        this.roleId = roleId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = "active";
    }

    // Full constructor
    public User(long id, long roleId, String name, String email, String password,
                String phone, String profilePicture, String status,
                LocalDateTime emailVerifiedAt, LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.roleId = roleId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.profilePicture = profilePicture;
        this.status = status;
        this.emailVerifiedAt = emailVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters (inline style)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email +
               "', role=" + roleName + ", status='" + status + "'}";
    }
}
```

**File:** `src/main/java/esprit/farouk/models/Role.java`

```java
package esprit.farouk.models;

import java.time.LocalDateTime;

public class Role {
    private long id;
    private String name;
    private String description;
    private String permissions; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Empty constructor
    public Role() {}

    // Minimal constructor
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Full constructor
    public Role(long id, String name, String description, String permissions,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name; // For ComboBox display
    }
}
```

---

## Code Patterns & Conventions

### Naming Conventions
- **Package names:** lowercase (e.g., `esprit.farouk.models`)
- **Class names:** PascalCase (e.g., `UserService`, `LoginController`)
- **Method names:** camelCase (e.g., `getUserById`, `validateEmail`)
- **Variables:** camelCase (e.g., `userName`, `emailField`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `DB_HOST`, `MAX_LOGIN_ATTEMPTS`)

### Database Patterns
- **Always use `PreparedStatement`** (prevents SQL injection)
- **Null-safe timestamp handling:**
  ```java
  Timestamp timestamp = rs.getTimestamp("created_at");
  if (timestamp != null) {
      user.setCreatedAt(timestamp.toLocalDateTime());
  }
  ```
- **Check for null on primitives:**
  ```java
  long roleId = rs.getLong("role_id");
  if (rs.wasNull()) {
      roleId = 0; // or handle appropriately
  }
  ```

### Service Layer Pattern
Each service should have:
- `mapRow(ResultSet rs)` helper method for object mapping
- Standard CRUD methods: `create()`, `getById()`, `getAll()`, `update()`, `delete()`
- Business logic methods (e.g., `authenticate()`, `blockUser()`)

### Controller Pattern
- Use `@FXML` annotation for UI elements
- Initialize method: `@FXML public void initialize()`
- Keep controllers thin - delegate to services
- Use `Dialog<ButtonType>` for forms with validation loops

---

## Features to Implement (Module 1 Checklist)

### Core Authentication
- [ ] Login screen with email/password
- [ ] Register screen with role selection (Farmer/Customer only)
- [ ] Forgot password with email-based 6-digit code
- [ ] Password hashing with BCrypt
- [ ] Blocked user login prevention
- [ ] Session management (store logged-in user)

### Admin Features
- [ ] Dashboard with sidebar navigation
- [ ] Users CRUD (Add, Edit, Delete)
- [ ] Roles CRUD (Add, Edit, Delete)
- [ ] Search/filter users table
- [ ] Block/Unblock user toggle
- [ ] Statistics page (user counts, pie chart by role, registrations chart)

### User Profile
- [ ] Profile editing (name, email, phone)
- [ ] Change password functionality
- [ ] View role and status

### Validation
- [ ] Email regex validation
- [ ] Phone number validation
- [ ] Name minimum 2 characters
- [ ] Password strength validation
- [ ] Unique email check

### Guest Features (Basic)
- [ ] Guest login creates temporary user
- [ ] Limited sidebar menu for guests
- [ ] No profile editing for guests

---

## Email Configuration (Gmail SMTP)

**File:** `src/main/java/esprit/farouk/utils/EmailUtils.java`

```java
package esprit.farouk.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtils {
    // Gmail SMTP Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465"; // SSL port
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // TODO: Change this
    private static final String SENDER_PASSWORD = "your-app-password"; // TODO: Use Gmail App Password

    /**
     * Sends an email using Gmail SMTP
     *
     * @param recipientEmail Recipient's email address
     * @param subject Email subject
     * @param messageBody Email body (can be HTML)
     * @return true if sent successfully, false otherwise
     */
    public static boolean sendEmail(String recipientEmail, String subject, String messageBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(messageBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✓ Email sent successfully to: " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("✗ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends password reset email with 6-digit code
     */
    public static boolean sendPasswordResetEmail(String recipientEmail, String resetCode) {
        String subject = "AgriCloud - Password Reset Code";
        String body = "<html><body style='font-family: Arial, sans-serif;'>" +
                     "<h2 style='color: #2E7D32;'>Password Reset Request</h2>" +
                     "<p>You requested to reset your password. Use the code below:</p>" +
                     "<div style='background: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px;'>" +
                     "<h1 style='color: #2E7D32; letter-spacing: 5px;'>" + resetCode + "</h1>" +
                     "</div>" +
                     "<p>This code will expire in 15 minutes.</p>" +
                     "<p>If you didn't request this, please ignore this email.</p>" +
                     "<hr style='margin-top: 30px;'/>" +
                     "<p style='color: #666; font-size: 12px;'>AgriCloud - Smart Farm Management System</p>" +
                     "</body></html>";

        return sendEmail(recipientEmail, subject, body);
    }
}
```

**Important:** To use Gmail SMTP:
1. Enable 2-factor authentication on your Gmail account
2. Generate an "App Password" (Google Account → Security → App Passwords)
3. Replace `SENDER_EMAIL` and `SENDER_PASSWORD` in the code above

---

## Validation Utilities

**File:** `src/main/java/esprit/farouk/utils/ValidationUtils.java`

```java
package esprit.farouk.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone regex pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    /**
     * Validates email format
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates phone number format
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates name (minimum 2 characters)
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.trim().length() >= 2;
    }

    /**
     * Validates password strength (minimum 6 characters)
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    /**
     * Checks if a string is empty or null
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
```

---

## Session Management

**File:** `src/main/java/esprit/farouk/utils/SessionManager.java`

```java
package esprit.farouk.utils;

import esprit.farouk.models.User;

public class SessionManager {
    private static User currentUser = null;

    /**
     * Sets the currently logged-in user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("✓ User session started: " + user.getName());
    }

    /**
     * Gets the currently logged-in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if current user is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && "Admin".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is farmer
     */
    public static boolean isFarmer() {
        return currentUser != null && "Farmer".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is customer
     */
    public static boolean isCustomer() {
        return currentUser != null && "Customer".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is guest
     */
    public static boolean isGuest() {
        return currentUser != null && "Guest".equals(currentUser.getRoleName());
    }

    /**
     * Logs out the current user
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("✓ User logged out: " + currentUser.getName());
            currentUser = null;
        }
    }
}
```

---

## Build and Run Commands

### Using Maven Command Line

```bash
# Clean and compile the project
mvn clean compile

# Run the application
mvn javafx:run

# Package as JAR (executable)
mvn clean package

# Run tests
mvn test
```

### Using IntelliJ IDEA

1. **Import Project:**
    - File → Open → Select your project folder
    - IntelliJ will auto-detect Maven and import dependencies

2. **Run Configuration:**
    - Run → Edit Configurations → Add New → Application
    - Main class: `esprit.farouk.Main`
    - VM options: `--module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml,javafx.web`

3. **Build:**
    - Build → Build Project (Ctrl+F9)

4. **Run:**
    - Right-click `Main.java` → Run 'Main'

---

## Testing Credentials

After setup, you can test with these accounts:

```
Admin Login:
Email: admin@admin.com
Password: farouk

Farmer Login:
Email: farmer@farmer.com
Password: farouk

Customer Login:
Email: customer@customer.com
Password: farouk
```

---

## Git Workflow (IMPORTANT!)

### Repository Information
- **Repository:** https://github.com/alpharou9/Agricloud_.git
- **Platform:** GitHub

### Initial Setup - Option A: Starting Fresh (New Project)

If you're creating the project from scratch:

```bash
# Navigate to your projects folder
cd C:\Users\rouk1\OneDrive\Bureau

# Create new project folder
mkdir Agricloud_
cd Agricloud_

# Initialize git
git init

# Configure git (use YOUR name and email, not Claude!)
git config user.name "Your Name"
git config user.email "your-email@example.com"

# Create .gitignore
echo target/
*.class
*.jar
*.war
*.ear
.idea/
*.iml
.vscode/
.DS_Store
*.log > .gitignore

# Add remote repository
git remote add origin https://github.com/alpharou9/Agricloud_.git

# Create initial README
echo "# AgriCloud - Smart Farm Management System" > README.md

# First commit (NEVER mention Claude/AI!)
git add .
git commit -m "Initial project setup"

# Push to GitHub
git branch -M main
git push -u origin main
```

### Initial Setup - Option B: Clone Existing Repository

If the repository already has some files:

```bash
# Navigate to your projects folder
cd C:\Users\rouk1\OneDrive\Bureau

# Clone the repository
git clone https://github.com/alpharou9/Agricloud_.git
cd Agricloud_

# Configure git (use YOUR name and email, not Claude!)
git config user.name "Your Name"
git config user.email "your-email@example.com"

# Verify remote
git remote -v
```

### Standard Workflow (Daily Development)

```bash
# Always pull latest changes first
git pull origin main

# Check what files changed
git status

# Add specific files (preferred over git add .)
git add src/main/java/esprit/farouk/services/UserService.java
git add src/main/java/esprit/farouk/controllers/LoginController.java

# Or add all changes (be careful!)
git add .

# Commit (NEVER mention AI/Claude!)
git commit -m "feat: Add user authentication with bcrypt"

# Push to remote
git push origin main
```

### Before Each Push - Safety Checklist

✅ **Check your commit message** - No mention of Claude/AI/Anthropic
✅ **Review your changes** - Use `git diff` to see what you're committing
✅ **Test your code** - Make sure it compiles and runs
✅ **Pull first** - Always pull before pushing to avoid conflicts

### If You Accidentally Added Claude in Commit

```bash
# If you haven't pushed yet, you can fix the last commit:
git commit --amend -m "feat: Add user authentication with bcrypt"

# If you already pushed, you'll need to force push (BE CAREFUL!):
git commit --amend -m "feat: Add user authentication with bcrypt"
git push --force origin main

# ⚠️ Only use force push if you're the only one working on the branch!
```

### Commit Message Examples (CORRECT)
```bash
git commit -m "feat: Add login screen with email validation"
git commit -m "feat: Implement user registration with role selection"
git commit -m "feat: Add forgot password flow with email verification"
git commit -m "fix: Resolve password hashing issue in UserService"
git commit -m "style: Update dashboard CSS styling"
git commit -m "refactor: Simplify user validation logic"
```

---

## Common Issues and Solutions

### Issue 1: JavaFX Runtime Not Found
**Solution:**
```bash
# Add VM options to run configuration:
--module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml,javafx.web
```

### Issue 2: MySQL Connection Failed
**Solution:**
- Verify WAMP is running (icon should be GREEN)
- Check port 3306 is not blocked
- Confirm database name is `agricloud`
- Ensure username is `root` with empty password

### Issue 3: BCrypt Class Not Found
**Solution:**
```bash
# Update Maven dependencies
mvn clean install -U
```

### Issue 4: Email Sending Fails
**Solution:**
- Enable 2FA on Gmail
- Generate App Password (not your regular password)
- Update `EmailUtils.java` with correct credentials

---

## Additional Resources Needed

To complete Module 1, you'll also need:

1. **CSS Styling File** - for making the UI look professional
2. **FXML Layout Files** - for each screen (login, register, dashboard, etc.)
3. **Service Layer Implementation** - `UserService.java` and `RoleService.java` with full CRUD
4. **Controller Classes** - handling UI logic and user interactions
5. **Main Application Class** - entry point that loads the login screen

---

## Next Steps After Setup

1. ✅ Install all prerequisites (JDK, Maven, WAMP)
2. ✅ Create `pom.xml` with all dependencies
3. ✅ Set up database and run SQL scripts
4. ✅ Create project structure (packages and folders)
5. ✅ Implement DatabaseConnection class and test connection
6. ✅ Create model classes (User, Role)
7. ✅ Implement utility classes (ValidationUtils, EmailUtils, SessionManager)
8. ⬜ Implement service layer (UserService, RoleService)
9. ⬜ Create FXML layouts for each screen
10. ⬜ Implement controllers for each screen
11. ⬜ Create Main application class
12. ⬜ Test complete authentication flow
13. ⬜ Implement admin features (Users CRUD, Statistics)
14. ⬜ Add CSS styling
15. ⬜ Test all features thoroughly

---

## Questions to Answer Before Starting

Please provide this information:

1. **Gmail Account for Email Sending:**
    - Email address: ___________________
    - App Password: ___________________ (generate from Google Account settings)

2. **Project Location:**
    - Where should the project folder be created? ___________________

3. **IDE Preference:**
    - IntelliJ IDEA / Eclipse / VS Code / Other: ___________________

4. **Git Repository:**
    - Platform: GitHub ✓
    - Repository URL: https://github.com/alpharou9/Agricloud_.git ✓

5. **Team Member Name (for commits):**
    - Name: ___________________ (TODO: Fill this in!)
    - Email (for git config): ___________________ (TODO: Fill this in!)

---

## Summary

This CLAUDE.md provides everything needed to start Module 1 from scratch:

✅ All Maven dependencies listed
✅ Complete database setup with SQL scripts
✅ Project structure defined
✅ Code patterns and conventions explained
✅ Git workflow with CRITICAL commit rules
✅ Utility classes for validation, email, and session management
✅ Model classes structure
✅ Testing credentials provided
✅ Build and run commands documented

**Remember: NEVER include Claude/AI mentions in your git commits or you will fail the class!**

Good luck with your project! 🚀
