# AgriCloud User Management System - Complete Architecture Explanation

**Author:** [Your Name]
**Date:** February 14, 2026
**Module:** User Management (Module 1)
**Technology Stack:** JavaFX 17, MySQL, Maven, BCrypt, JavaMail, Google OAuth 2.0

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture Pattern](#2-architecture-pattern)
3. [Technology Stack](#3-technology-stack)
4. [Database Architecture](#4-database-architecture)
5. [Application Layers](#5-application-layers)
6. [Authentication System](#6-authentication-system)
7. [OAuth Integration](#7-oauth-integration)
8. [Security Implementation](#8-security-implementation)
9. [Key Features Explained](#9-key-features-explained)
10. [Code Flow Examples](#10-code-flow-examples)

---

## 1. Project Overview

### What is AgriCloud?
AgriCloud is a **Smart Farm Management System** designed to help farmers, customers, and administrators manage agricultural operations digitally. The project is divided into multiple modules, and this is **Module 1: User Management**.

### Purpose of Module 1
This module handles all user-related operations:
- User registration and login
- Role-based access control (Admin, Farmer, Customer, Guest)
- Profile management
- Password recovery via email
- OAuth authentication (Google Sign-In)
- User administration (CRUD operations)

### Project Scope
- **Users Managed:** Admin, Farmer, Customer, Guest
- **Authentication Methods:** Traditional (email/password), OAuth (Google), Guest mode
- **Admin Capabilities:** Manage users, roles, view statistics
- **Security:** BCrypt password hashing, email verification, OAuth 2.0

---

## 2. Architecture Pattern

### Overall Pattern: **3-Tier MVC Architecture**

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  (JavaFX UI - FXML files + Controllers)                     │
│  - Login Screen                                              │
│  - Registration Screen                                       │
│  - Dashboard (role-based views)                             │
│  - Profile Management                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ User Actions
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                      │
│  (Services + Utilities)                                      │
│  - UserService (CRUD operations)                            │
│  - RoleService (role management)                            │
│  - GoogleOAuthService (OAuth flow)                          │
│  - EmailUtils (password reset emails)                       │
│  - ValidationUtils (input validation)                       │
│  - SessionManager (user session)                            │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ Database Queries
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                         │
│  (DatabaseConnection + MySQL Database)                       │
│  - users table                                               │
│  - roles table                                               │
│  - password_resets table                                     │
└─────────────────────────────────────────────────────────────┘
```

### Why This Architecture?

1. **Separation of Concerns**: Each layer has a specific responsibility
2. **Maintainability**: Changes in UI don't affect business logic
3. **Testability**: Each layer can be tested independently
4. **Scalability**: Easy to add new features without breaking existing code

---

## 3. Technology Stack

### Frontend
- **JavaFX 17**: Modern Java UI framework for desktop applications
- **FXML**: XML-based markup language for defining UI layouts
- **AtlantaFX PrimerLight**: Modern CSS theme for JavaFX
- **Custom CSS**: Brand-specific styling with green theme (#16a34a)

### Backend
- **Java 17**: Core programming language
- **Maven**: Build automation and dependency management
- **JDBC**: Database connectivity (MySQL Connector/J 8.0.33)

### Database
- **MySQL 8.0**: Relational database management system
- **WAMP Server**: Windows development stack (Apache, MySQL, PHP)

### Security
- **BCrypt**: Industry-standard password hashing (jBCrypt 0.4)
- **OAuth 2.0**: Google authentication protocol
- **PreparedStatement**: SQL injection prevention

### Email
- **JavaMail API 1.6.2**: Sending password reset emails via Gmail SMTP

### Third-Party Libraries
- **Google OAuth Client 2.2.0**: Google Sign-In integration
- **Gson 2.10.1**: JSON parsing for OAuth responses

---

## 4. Database Architecture

### Entity-Relationship Diagram

```
┌─────────────────┐          ┌─────────────────┐
│     roles       │          │  password_resets│
├─────────────────┤          ├─────────────────┤
│ id (PK)         │          │ id (PK)         │
│ name            │          │ email           │
│ description     │          │ reset_code      │
│ permissions     │◄─────┐   │ expires_at      │
│ created_at      │      │   │ created_at      │
│ updated_at      │      │   └─────────────────┘
└─────────────────┘      │
                         │
                         │ Foreign Key
                         │ (role_id)
                         │
                    ┌────┴──────────────┐
                    │      users        │
                    ├───────────────────┤
                    │ id (PK)           │
                    │ role_id (FK)      │
                    │ name              │
                    │ email (UNIQUE)    │
                    │ password (NULLABLE)│
                    │ phone             │
                    │ profile_picture   │
                    │ status (ENUM)     │
                    │ oauth_provider    │
                    │ oauth_id          │
                    │ email_verified_at │
                    │ created_at        │
                    │ updated_at        │
                    └───────────────────┘
                    Indexes:
                    - idx_email (email)
                    - idx_role_id (role_id)
                    - idx_status (status)
                    - idx_oauth (oauth_provider, oauth_id)
```

### Table Descriptions

#### 1. `users` Table
**Purpose:** Stores all user accounts

**Key Fields:**
- `id`: Primary key (auto-increment)
- `role_id`: Foreign key to roles table
- `email`: Unique identifier (indexed for fast lookups)
- `password`: BCrypt hashed password (nullable for OAuth users)
- `status`: ENUM('active', 'inactive', 'blocked')
- `oauth_provider`: 'google', 'facebook', or NULL
- `oauth_id`: Unique ID from OAuth provider

**Why Password is Nullable?**
- Users who sign in with Google don't need a password
- They authenticate through Google's OAuth system

#### 2. `roles` Table
**Purpose:** Defines user roles and permissions

**Default Roles:**
1. **Admin** (id=1): Full system access
2. **Farmer** (id=2): Farm/product management
3. **Customer** (id=3): Shopping and orders
4. **Guest** (id=4): Limited read-only access

**permissions Field:**
- Stored as JSON array: `["farms", "products", "orders"]`
- Flexible for future permission expansion

#### 3. `password_resets` Table
**Purpose:** Temporary storage for password reset codes

**Flow:**
1. User requests password reset
2. 6-digit code generated and stored
3. Email sent to user
4. User enters code within expiry time
5. Password updated, record deleted

---

## 5. Application Layers

### Layer 1: Models (Data Objects)

**Location:** `src/main/java/esprit/farouk/models/`

#### User.java
```java
public class User {
    private long id;
    private long roleId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String profilePicture;
    private String status;
    private String oauthProvider;
    private String oauthId;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient field (not in database)
    private String roleName; // For display (from JOIN)
}
```

**Purpose:** Plain Old Java Object (POJO) representing a user
**Pattern:** JavaBean with private fields + getters/setters
**Why Transient Fields?** `roleName` comes from a SQL JOIN, not stored in users table

#### Role.java
```java
public class Role {
    private long id;
    private String name;
    private String description;
    private String permissions; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return name; // For ComboBox display
    }
}
```

**toString() Override:** Allows direct use in ComboBox without custom cell factory

---

### Layer 2: Services (Business Logic)

**Location:** `src/main/java/esprit/farouk/services/`

#### DatabaseConnection.java
**Pattern:** Singleton
**Purpose:** Single shared database connection

```java
public class DatabaseConnection {
    private static Connection connection = null; // Shared instance

    public static Connection getConnection() {
        if (connection == null || connection.isClosed()) {
            // Create new connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
        return connection;
    }
}
```

**Why Singleton?**
- Avoid multiple database connections (resource waste)
- Connection pooling for better performance
- Thread-safe access to database

#### UserService.java
**Pattern:** Data Access Object (DAO)
**Purpose:** All database operations for users

**Key Methods:**

1. **CRUD Operations:**
```java
public void add(User user)           // INSERT
public void update(User user)        // UPDATE
public void delete(long id)          // DELETE
public User getById(long id)         // SELECT by ID
public List<User> getAll()           // SELECT all
```

2. **Business Logic:**
```java
public User authenticate(String email, String password) {
    User user = getByEmail(email);
    if (user != null && BCrypt.checkpw(password, user.getPassword())) {
        return user; // Login success
    }
    return null; // Login failed
}
```

3. **OAuth Methods:**
```java
public User getByOAuthId(String provider, String oauthId)
public User createOrUpdateOAuthUser(...)
```

**mapRow() Helper Method:**
```java
private User mapRow(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setName(rs.getString("name"));
    // ... map all fields from ResultSet to User object
    return user;
}
```

**Why mapRow()?**
- Centralizes ResultSet-to-Object conversion
- Reused by all SELECT methods
- Handles null timestamps safely

#### GoogleOAuthService.java
**Pattern:** Service Layer
**Purpose:** Handle Google OAuth 2.0 flow

**Key Methods:**

1. **getAuthorizationUrl()**: Generates Google consent screen URL
2. **getAccessToken(code)**: Exchanges authorization code for access token
3. **getUserInfo(token)**: Fetches user profile from Google

**OAuth Flow:**
```
User clicks "Sign in with Google"
    ↓
getAuthorizationUrl() → Opens browser to Google
    ↓
User approves in browser
    ↓
Google redirects to localhost:3000/oauth/callback?code=XXX
    ↓
OAuthCallbackServer catches code
    ↓
getAccessToken(code) → Gets access token from Google
    ↓
getUserInfo(token) → Gets {id, email, name, picture}
    ↓
createOrUpdateOAuthUser() → Saves to database
```

---

### Layer 3: Controllers (UI Logic)

**Location:** `src/main/java/esprit/farouk/controllers/`

#### LoginController.java
**Purpose:** Handles login screen interactions

**Key Methods:**

```java
@FXML
private void handleLogin() {
    // 1. Get input values
    String email = emailField.getText().trim();
    String password = passwordField.getText();

    // 2. Validate input
    if (email.isEmpty() || password.isEmpty()) {
        showError("Please enter both email and password.");
        return;
    }

    // 3. Authenticate user (calls UserService)
    User user = userService.authenticate(email, password);

    // 4. Check if user is blocked
    if (user != null && "blocked".equals(user.getStatus())) {
        showError("Your account has been blocked.");
        return;
    }

    // 5. Navigate to dashboard
    navigateToDashboard(user);
}
```

**@FXML Annotation:** Links Java methods to FXML UI elements

#### DashboardController.java
**Purpose:** Manages dashboard views (role-based)

**Complexity:** Largest controller (~1000+ lines)

**Architecture:**
```java
public class DashboardController {
    @FXML private VBox sidebarMenu;
    @FXML private StackPane contentArea;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        buildSidebar(); // Different menus for different roles
        showHomeView();
    }

    private void buildSidebar() {
        if ("Admin".equals(currentUser.getRoleName())) {
            // Admin menu: Home, Users, Roles, Statistics, etc.
        } else if ("Farmer".equals(currentUser.getRoleName())) {
            // Farmer menu: Home, Profile, My Farms, Shop, etc.
        }
        // ... more roles
    }
}
```

**View Building Pattern:**
```java
private void showUsersView() {
    // 1. Clear previous view
    contentArea.getChildren().clear();

    // 2. Build new view programmatically
    VBox view = new VBox(20);
    view.getChildren().add(new Label("Users Management"));

    // 3. Create table
    TableView<User> table = buildUsersTable();

    // 4. Add search/filter
    TextField searchField = new TextField();
    searchField.textProperty().addListener((obs, old, newVal) -> {
        filterTable(newVal);
    });

    // 5. Add to content area
    view.getChildren().addAll(searchField, table);
    contentArea.getChildren().add(view);
}
```

**Why Programmatic UI?**
- Dynamic based on user role
- Easier to add search/filter logic
- FXML would be too complex for dynamic content

---

### Layer 4: Utilities

**Location:** `src/main/java/esprit/farouk/utils/`

#### ValidationUtils.java
**Purpose:** Input validation with regex patterns

```java
public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
```

**Why Regex?**
- Email: Prevents invalid formats (e.g., "user@", "user@domain")
- Phone: Supports international formats (+1234567890)
- Centralized validation rules

#### SessionManager.java
**Purpose:** Stores logged-in user across application

```java
public class SessionManager {
    private static User currentUser = null; // Singleton pattern

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && "Admin".equals(currentUser.getRoleName());
    }
}
```

**Why Static?**
- Accessible from any controller
- Persists across screen navigation
- Simple session management for desktop app

#### EmailUtils.java
**Purpose:** Send emails via Gmail SMTP

```java
public static boolean sendPasswordResetEmail(String recipientEmail, String resetCode) {
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "465"); // SSL
    props.put("mail.smtp.auth", "true");

    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
        }
    });

    Message message = new MimeMessage(session);
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
    message.setSubject("AgriCloud - Password Reset Code");
    message.setContent("<h1>" + resetCode + "</h1>", "text/html");

    Transport.send(message);
    return true;
}
```

**SMTP Configuration:**
- Port 465: SSL encryption
- Gmail App Password: Not regular password (2FA required)
- HTML email: Professional styling

---

## 6. Authentication System

### Authentication Methods

#### 1. Traditional Email/Password Authentication

**Flow:**
```
User enters email + password
    ↓
Controller calls UserService.authenticate(email, password)
    ↓
UserService queries database for user with email
    ↓
BCrypt.checkpw(enteredPassword, storedHashedPassword)
    ↓
If match → Return User object
If no match → Return null
    ↓
Controller checks if user is blocked
    ↓
SessionManager.setCurrentUser(user)
    ↓
Navigate to dashboard
```

**Code:**
```java
public User authenticate(String email, String password) throws SQLException {
    User user = getByEmail(email);
    if (user != null && BCrypt.checkpw(password, user.getPassword())) {
        return user;
    }
    return null;
}
```

**BCrypt Hashing:**
```java
// Registration: Hash password before saving
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
user.setPassword(hashedPassword);

// Login: Compare plain password with hash
boolean matches = BCrypt.checkpw(plainPassword, storedHash);
```

**Why BCrypt?**
- Industry standard for password hashing
- Includes salt (prevents rainbow table attacks)
- Slow by design (prevents brute force)
- Auto-handles salt generation

#### 2. Guest Authentication

**Flow:**
```
User clicks "Continue as Guest"
    ↓
Generate unique UUID: "guest_123e4567-e89b-12d3-a456-426614174000@agricloud.com"
    ↓
Create temporary user in database with Guest role
    ↓
Return user object
    ↓
SessionManager.setCurrentUser(guestUser)
    ↓
Navigate to dashboard with limited permissions
```

**Code:**
```java
public User createUniqueGuestUser() throws SQLException {
    String sessionId = UUID.randomUUID().toString();
    String guestEmail = "guest_" + sessionId + "@agricloud.com";
    String guestName = "Guest_" + sessionId.substring(0, 8);

    User newGuest = new User();
    newGuest.setRoleId(4); // Guest role
    newGuest.setEmail(guestEmail);
    newGuest.setName(guestName);
    newGuest.setPassword("guest_temp_" + sessionId);

    add(newGuest);
    return getByEmail(guestEmail);
}
```

**Why Unique Guests?**
- Each session has separate cart/orders
- No data mixing between guest sessions
- Can be cleaned up after 24 hours

**Cleanup:**
```java
public int cleanupOldGuestUsers() throws SQLException {
    String sql = "DELETE FROM users WHERE email LIKE 'guest_%@agricloud.com' " +
                 "AND created_at < NOW() - INTERVAL 24 HOUR";
    PreparedStatement ps = connection.prepareStatement(sql);
    return ps.executeUpdate();
}
```

#### 3. OAuth Authentication (Google)

**Complete Flow (Detailed):**

**Step 1: User Clicks Button**
```java
@FXML
private void handleGoogleLogin() {
    OAuthLoginDialog dialog = new OAuthLoginDialog();
    User user = dialog.showGoogleLogin(stage);
    if (user != null) {
        navigateToDashboard(user);
    }
}
```

**Step 2: Generate Authorization URL**
```java
public String getAuthorizationUrl() {
    return "https://accounts.google.com/o/oauth2/v2/auth?" +
           "client_id=" + GOOGLE_CLIENT_ID +
           "&redirect_uri=http://localhost:3000/oauth/callback" +
           "&response_type=code" +
           "&scope=email profile" +
           "&state=" + UUID.randomUUID();
}
```

**Step 3: Open Browser**
```java
Desktop.getDesktop().browse(new URI(authUrl));
```

**Step 4: Start Callback Server**
```java
OAuthCallbackServer callbackServer = new OAuthCallbackServer();
String authCode = callbackServer.waitForCallback(60); // Wait 60 seconds
```

**Step 5: Callback Server Receives Code**
```java
// OAuthCallbackServer.java
HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 3000), 0);
server.createContext("/oauth/callback", exchange -> {
    String query = exchange.getRequestURI().getQuery();
    // Parse: code=4/0AanRRrvYXXXX&state=YYY
    authorizationCode = extractCodeFromQuery(query);

    // Send success page to browser
    String html = "<html><body><h1>✅ Login Successful!</h1></body></html>";
    exchange.sendResponseHeaders(200, html.length());
    exchange.getResponseBody().write(html.getBytes());

    latch.countDown(); // Signal that code received
});
server.start();
```

**Step 6: Exchange Code for Token**
```java
public String getAccessToken(String authCode) {
    String requestBody = "code=" + authCode +
                        "&client_id=" + GOOGLE_CLIENT_ID +
                        "&client_secret=" + GOOGLE_CLIENT_SECRET +
                        "&redirect_uri=" + GOOGLE_REDIRECT_URI +
                        "&grant_type=authorization_code";

    // POST to https://oauth2.googleapis.com/token
    HttpURLConnection conn = (HttpURLConnection) new URL(GOOGLE_TOKEN_URL).openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.getOutputStream().write(requestBody.getBytes());

    // Parse JSON response: {"access_token": "ya29.XXX", ...}
    JsonObject json = new Gson().fromJson(response, JsonObject.class);
    return json.get("access_token").getAsString();
}
```

**Step 7: Fetch User Info**
```java
public Map<String, String> getUserInfo(String accessToken) {
    URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);

    // Response: {"id": "123", "email": "user@gmail.com", "name": "John", "picture": "url"}
    JsonObject json = new Gson().fromJson(response, JsonObject.class);

    Map<String, String> userInfo = new HashMap<>();
    userInfo.put("id", json.get("id").getAsString());
    userInfo.put("email", json.get("email").getAsString());
    userInfo.put("name", json.get("name").getAsString());
    userInfo.put("picture", json.get("picture").getAsString());
    return userInfo;
}
```

**Step 8: Create or Link User**
```java
public User createOrUpdateOAuthUser(String provider, String oauthId,
                                   String email, String name, String picture) {
    // Check if OAuth ID already exists
    User existingOAuth = getByOAuthId(provider, oauthId);
    if (existingOAuth != null) {
        return existingOAuth; // Already registered with Google
    }

    // Check if email exists (link to existing account)
    User existingEmail = getByEmail(email);
    if (existingEmail != null) {
        linkOAuthAccount(existingEmail.getId(), provider, oauthId);
        return existingEmail;
    }

    // Create new user
    User newUser = new User();
    newUser.setRoleId(3); // Customer
    newUser.setEmail(email);
    newUser.setName(name);
    newUser.setProfilePicture(picture);
    newUser.setOauthProvider("google");
    newUser.setOauthId(oauthId);
    newUser.setPassword(null); // OAuth users don't need password
    add(newUser);

    return getByEmail(email);
}
```

**Why This Approach?**
- **Account Linking**: If user has existing account with same email, link Google instead of creating duplicate
- **No Password Needed**: OAuth users authenticate through Google
- **Profile Picture**: Automatically imported from Google account

---

## 7. OAuth Integration

### Why OAuth?

**Traditional Authentication Problems:**
- Users must remember another password
- Password security responsibility on us
- No quick "one-click" login

**OAuth Benefits:**
- ✅ Users already logged into Google in Chrome
- ✅ No password to remember
- ✅ Secure (Google handles authentication)
- ✅ One-click login if already authenticated
- ✅ Profile picture included

### OAuth 2.0 Authorization Code Flow

**Standard OAuth Flow:**
```
┌─────────┐                                    ┌──────────┐
│  User   │                                    │  Google  │
└────┬────┘                                    └────┬─────┘
     │                                              │
     │ 1. Click "Sign in with Google"              │
     │                                              │
     ▼                                              │
┌─────────────────────┐                            │
│  AgriCloud App      │                            │
│  (JavaFX)           │                            │
└──────┬──────────────┘                            │
       │                                            │
       │ 2. Redirect to Google with client_id      │
       ├───────────────────────────────────────────►│
       │                                            │
       │                                            │ 3. Show consent screen
       │                                            │    "Allow AgriCloud to
       │                                            │     access your email?"
       │                                            │
       │◄───────────────────────────────────────────┤
       │ 4. Redirect back with authorization code  │
       │    http://localhost:3000?code=XXX         │
       │                                            │
       │ 5. Exchange code for access token         │
       ├───────────────────────────────────────────►│
       │    POST /token                            │
       │    client_id + client_secret + code       │
       │                                            │
       │◄───────────────────────────────────────────┤
       │ 6. Return access_token                    │
       │                                            │
       │ 7. Fetch user info with token             │
       ├───────────────────────────────────────────►│
       │    GET /userinfo                          │
       │    Authorization: Bearer token            │
       │                                            │
       │◄───────────────────────────────────────────┤
       │ 8. Return {id, email, name, picture}      │
       │                                            │
       ▼                                            │
  User logged in!
```

### Key OAuth Components

#### 1. OAuthConfig.java
**Purpose:** Store OAuth configuration

```java
public static final String GOOGLE_CLIENT_ID = "178628032183-...googleusercontent.com";
public static final String GOOGLE_CLIENT_SECRET = "GOCSPX-2bKFqjr...";
public static final String GOOGLE_REDIRECT_URI = "http://localhost:3000/oauth/callback";
```

**Where to Get These?**
1. Go to Google Cloud Console
2. Create OAuth 2.0 Client ID
3. Copy Client ID and Client Secret
4. Add redirect URI: `http://localhost:3000/oauth/callback`

#### 2. GoogleOAuthService.java
**Purpose:** Implement OAuth protocol

**Methods:**
- `getAuthorizationUrl()`: Build consent screen URL
- `getAccessToken(code)`: Exchange code for token
- `getUserInfo(token)`: Fetch user profile

#### 3. OAuthCallbackServer.java
**Purpose:** Receive OAuth redirect

**Why Needed?**
- Desktop apps can't receive HTTP callbacks normally
- We create temporary HTTP server on localhost:3000
- Receives redirect from Google
- Extracts authorization code
- Shuts down after receiving code

**Implementation:**
```java
public String waitForCallback(int timeoutSeconds) {
    // Create HTTP server
    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

    // Handle /oauth/callback requests
    server.createContext("/oauth/callback", exchange -> {
        // Extract code from query string
        String query = exchange.getRequestURI().getQuery();
        authCode = parseCode(query);

        // Send success page
        sendSuccessPage(exchange);

        // Signal completion
        latch.countDown();
    });

    server.start();

    // Wait for callback (with timeout)
    latch.await(timeoutSeconds, TimeUnit.SECONDS);

    server.stop(0);
    return authCode;
}
```

#### 4. OAuthLoginDialog.java
**Purpose:** UI for OAuth flow

**Steps:**
1. Show "Opening browser..." dialog
2. Start callback server
3. Open system browser to Google
4. Wait for user to approve
5. Process callback
6. Close dialog and return user

---

## 8. Security Implementation

### Security Measures

#### 1. Password Hashing with BCrypt

**Why Hash Passwords?**
- Storing plain passwords is dangerous
- If database is compromised, all passwords leaked
- Hashing is one-way (can't reverse)

**BCrypt Features:**
- **Salt**: Random data added to password before hashing
- **Cost Factor**: Makes hashing slower (prevents brute force)
- **Adaptive**: Can increase cost over time as computers get faster

**Implementation:**
```java
// Registration
String hashedPassword = BCrypt.hashpw("user_password", BCrypt.gensalt());
// Stored in database: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// Login
boolean matches = BCrypt.checkpw("user_password", hashedPassword);
```

**Hash Breakdown:**
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
│  │  │                                                        │
│  │  │                                                        └─ Hash (31 chars)
│  │  └─ Salt (22 chars)
│  └─ Cost (10 = 2^10 iterations)
└─ Algorithm version (2a = BCrypt)
```

#### 2. SQL Injection Prevention

**Vulnerable Code (❌ DON'T DO THIS):**
```java
// BAD: Concatenating user input directly
String sql = "SELECT * FROM users WHERE email = '" + userEmail + "'";
Statement stmt = connection.createStatement();
ResultSet rs = stmt.executeQuery(sql);

// Attack: userEmail = "' OR '1'='1"
// Result: SELECT * FROM users WHERE email = '' OR '1'='1'
//         Returns ALL users!
```

**Secure Code (✅ CORRECT):**
```java
// GOOD: Using PreparedStatement
String sql = "SELECT * FROM users WHERE email = ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setString(1, userEmail);
ResultSet rs = ps.executeQuery();

// Attack: userEmail = "' OR '1'='1"
// Result: SELECT * FROM users WHERE email = '\' OR \'1\'=\'1'
//         Treats entire input as literal string, no injection!
```

**Why PreparedStatement Works:**
- Pre-compiles SQL with placeholders
- User input inserted as data, not code
- Automatically escapes special characters

#### 3. Session Management

**SessionManager Pattern:**
```java
public class SessionManager {
    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("Session started for: " + user.getName());
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("Session ended for: " + currentUser.getName());
            currentUser = null;
        }
    }
}
```

**Security Features:**
- ✅ Only one user logged in at a time
- ✅ Session cleared on logout
- ✅ Role-based access control via helper methods

#### 4. Blocked User Prevention

```java
if ("blocked".equals(user.getStatus())) {
    showError("Your account has been blocked. Please contact administrator.");
    return; // Prevent login
}
```

#### 5. Self-Block Prevention (Admin)

```java
if (selectedUser.getId() == currentUser.getId()) {
    Alert alert = new Alert(Alert.AlertType.WARNING,
        "You cannot block yourself!");
    alert.showAndWait();
    return;
}
```

#### 6. Input Validation

**Email Validation:**
```java
private static final Pattern EMAIL_PATTERN = Pattern.compile(
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
);

public static boolean isValidEmail(String email) {
    return EMAIL_PATTERN.matcher(email.trim()).matches();
}
```

**Prevents:**
- Invalid formats: `user@`, `@domain.com`, `user domain@test.com`
- SQL injection attempts in email field
- XSS attacks via email input

#### 7. OAuth Security

**State Parameter (CSRF Prevention):**
```java
String state = UUID.randomUUID().toString();
String authUrl = GOOGLE_AUTH_URL + "&state=" + state;
```

**Verification (should be implemented):**
```java
// On callback, verify state matches
if (!receivedState.equals(originalState)) {
    throw new SecurityException("CSRF attack detected!");
}
```

**HTTPS for Google:**
- All OAuth URLs use `https://`
- Prevents man-in-the-middle attacks
- Google handles all sensitive data

---

## 9. Key Features Explained

### Feature 1: User Registration

**User Story:**
"As a new user, I want to register an account so that I can access the system."

**Flow:**
```
1. User clicks "Register" on login screen
2. Fills form: name, email, phone, role (Farmer/Customer), password
3. Clicks "Register" button
4. System validates all inputs
5. Checks if email already exists
6. Hashes password with BCrypt
7. Inserts user into database with status='active'
8. Shows success message
9. Auto-redirects to login after 1.5 seconds
```

**Validation Rules:**
- Name: Minimum 2 characters
- Email: Valid format (regex)
- Phone: Optional but must be valid if provided
- Password: Minimum 6 characters
- Password confirmation must match
- Role: Must select Farmer or Customer (Admin not available)

**Code:**
```java
@FXML
private void handleRegister() {
    // Get input
    String name = nameField.getText().trim();
    String email = emailField.getText().trim();
    String password = passwordField.getText();

    // Validate
    if (!ValidationUtils.isValidName(name)) {
        showError("Name must be at least 2 characters.");
        return;
    }

    if (!ValidationUtils.isValidEmail(email)) {
        showError("Invalid email format.");
        return;
    }

    // Check duplicate email
    User existing = userService.getByEmail(email);
    if (existing != null) {
        showError("Email already registered.");
        return;
    }

    // Create user
    User newUser = new User();
    newUser.setRoleId(selectedRole.getId());
    newUser.setName(name);
    newUser.setEmail(email);
    newUser.setPassword(password); // Will be hashed in service
    userService.add(newUser);

    showSuccess("Registration successful! Please login.");
}
```

### Feature 2: Forgot Password Flow

**User Story:**
"As a user who forgot my password, I want to reset it via email so that I can regain access."

**Two-Step Process:**

**Step 1: Request Reset Code**
```
1. User clicks "Forgot Password?" on login screen
2. Enters email address
3. Clicks "Send Reset Code"
4. System validates email exists
5. Generates 6-digit random code
6. Sends email with code
7. UI switches to Step 2
```

**Code Generation:**
```java
String resetCode = String.format("%06d", new Random().nextInt(1000000));
// Example: "087342"
```

**Email Template:**
```html
<html>
<body>
    <h2 style='color: #2E7D32;'>Password Reset Request</h2>
    <p>Use this code to reset your password:</p>
    <div style='background: #f5f5f5; padding: 15px;'>
        <h1 style='color: #2E7D32; letter-spacing: 5px;'>087342</h1>
    </div>
    <p>This code will expire in 15 minutes.</p>
</body>
</html>
```

**Step 2: Reset Password**
```
1. User enters 6-digit code from email
2. Enters new password
3. Confirms new password
4. Clicks "Reset Password"
5. System validates code matches
6. Validates password strength
7. Validates passwords match
8. Updates password in database (hashed)
9. Shows success message
10. Auto-redirects to login
```

**Code:**
```java
@FXML
private void handleResetPassword() {
    String code = codeField.getText().trim();
    String newPassword = newPasswordField.getText();

    // Validate code
    if (!code.equals(sentCode)) {
        showError("Invalid reset code.");
        return;
    }

    // Validate password
    if (newPassword.length() < 6) {
        showError("Password must be at least 6 characters.");
        return;
    }

    // Update password
    User user = userService.getByEmail(userEmail);
    userService.updatePassword(user.getId(), newPassword);

    showSuccess("Password reset successful!");
}
```

### Feature 3: Role-Based Dashboard

**User Story:**
"As a user with a specific role, I want to see only the features relevant to my role."

**Dashboard Layout:**
```
┌────────────────────────────────────────────────────────┐
│  AgriCloud                                   [Logout]  │
├──────────────┬─────────────────────────────────────────┤
│              │                                         │
│  SIDEBAR     │         CONTENT AREA                    │
│  (dynamic)   │         (dynamic views)                 │
│              │                                         │
│  - Home      │  ┌───────────────────────────────────┐ │
│  - Profile   │  │                                   │ │
│  - Users*    │  │   Current view displayed here     │ │
│  - Roles*    │  │   (Users table, Profile form,     │ │
│  - Stats*    │  │    Statistics charts, etc.)       │ │
│  ...         │  │                                   │ │
│              │  └───────────────────────────────────┘ │
│  * = Admin   │                                         │
│  only        │                                         │
└──────────────┴─────────────────────────────────────────┘
```

**Menu by Role:**

**Admin:**
- Home, Users (CRUD), Roles (CRUD), Statistics, Profile

**Farmer:**
- Home, Profile, My Farms, My Products, Shop, Cart

**Customer:**
- Home, Profile, Shop, Cart, My Orders

**Guest:**
- Home, Shop, Cart (view only, limited)

**Dynamic Menu Building:**
```java
private void buildSidebar() {
    sidebarMenu.getChildren().clear();

    // Always show Home
    addMenuItem("🏠 Home", () -> showHomeView());

    if (SessionManager.isAdmin()) {
        addMenuItem("👥 Users", () -> showUsersView());
        addMenuItem("🔐 Roles", () -> showRolesView());
        addMenuItem("📊 Statistics", () -> showStatisticsView());
    } else if (SessionManager.isFarmer()) {
        addMenuItem("👤 Profile", () -> showProfileView());
        addMenuItem("🌾 My Farms", () -> showFarmsView());
        addMenuItem("📦 My Products", () -> showProductsView());
    } else if (SessionManager.isCustomer()) {
        addMenuItem("👤 Profile", () -> showProfileView());
        addMenuItem("🛒 Shop", () -> showShopView());
        addMenuItem("🛍️ Cart", () -> showCartView());
    }
}

private void addMenuItem(String text, Runnable action) {
    Label menuItem = new Label(text);
    menuItem.getStyleClass().add("sidebar-button");
    menuItem.setOnMouseClicked(e -> action.run());
    sidebarMenu.getChildren().add(menuItem);
}
```

### Feature 4: User CRUD (Admin Only)

**Create User:**
```java
@FXML
private void handleAddUser() {
    // Create dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Add New User");

    // Build form
    GridPane grid = new GridPane();
    TextField nameField = new TextField();
    TextField emailField = new TextField();
    PasswordField passwordField = new PasswordField();
    ComboBox<Role> roleCombo = new ComboBox<>();

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Email:"), 0, 1);
    grid.add(emailField, 1, 1);
    grid.add(new Label("Password:"), 0, 2);
    grid.add(passwordField, 1, 2);
    grid.add(new Label("Role:"), 0, 3);
    grid.add(roleCombo, 1, 3);

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // Validation loop
    while (true) {
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Validate
            if (!ValidationUtils.isValidEmail(emailField.getText())) {
                showAlert("Invalid email!");
                continue; // Re-show dialog
            }

            // Save
            User newUser = new User();
            newUser.setName(nameField.getText());
            newUser.setEmail(emailField.getText());
            newUser.setPassword(passwordField.getText());
            newUser.setRoleId(roleCombo.getValue().getId());
            userService.add(newUser);

            refreshTable();
            break;
        } else {
            break; // Cancel clicked
        }
    }
}
```

**Why Validation Loop?**
- If validation fails, re-show same dialog
- User doesn't lose entered data
- Can fix errors without starting over

**Update User:**
```java
@FXML
private void handleEditUser() {
    User selected = usersTable.getSelectionModel().getSelectedItem();

    // Pre-fill form with existing values
    nameField.setText(selected.getName());
    emailField.setText(selected.getEmail());
    roleCombo.setValue(selected.getRole());

    // Show dialog, validate, update
    userService.update(selected);
    refreshTable();
}
```

**Delete User:**
```java
@FXML
private void handleDeleteUser() {
    User selected = usersTable.getSelectionModel().getSelectedItem();

    // Confirmation dialog
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
        "Are you sure you want to delete " + selected.getName() + "?",
        ButtonType.YES, ButtonType.NO);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.YES) {
        userService.delete(selected.getId());
        refreshTable();
    }
}
```

**Block/Unblock User:**
```java
@FXML
private void handleBlockUser() {
    User selected = usersTable.getSelectionModel().getSelectedItem();

    // Prevent self-block
    if (selected.getId() == SessionManager.getCurrentUser().getId()) {
        showAlert("You cannot block yourself!");
        return;
    }

    // Toggle status
    String newStatus = "active".equals(selected.getStatus()) ? "blocked" : "active";
    selected.setStatus(newStatus);
    userService.update(selected);
    refreshTable();
}
```

### Feature 5: Search and Filter

**Search Implementation:**
```java
// Create FilteredList wrapper around ObservableList
ObservableList<User> usersList = FXCollections.observableArrayList(users);
FilteredList<User> filteredData = new FilteredList<>(usersList, p -> true);

// Add search listener
searchField.textProperty().addListener((observable, oldValue, newValue) -> {
    filteredData.setPredicate(user -> {
        // If search field is empty, show all users
        if (newValue == null || newValue.isEmpty()) {
            return true;
        }

        String lowerCaseFilter = newValue.toLowerCase();

        // Match name or email
        if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
            return true;
        } else if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
            return true;
        }

        return false; // No match
    });
});

// Wrap FilteredList in SortedList for table sorting
SortedList<User> sortedData = new SortedList<>(filteredData);
sortedData.comparatorProperty().bind(usersTable.comparatorProperty());
usersTable.setItems(sortedData);
```

**How It Works:**
1. User types in search field
2. Listener fires on every keystroke
3. setPredicate() filters the list
4. Table automatically updates with filtered results
5. Original list unchanged

**Status Filter:**
```java
ComboBox<String> statusFilter = new ComboBox<>();
statusFilter.getItems().addAll("All", "Active", "Blocked", "Inactive");
statusFilter.setValue("All");

statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
    filteredData.setPredicate(user -> {
        // Apply search filter first
        if (!matchesSearchText(user)) return false;

        // Then apply status filter
        if ("All".equals(newVal)) return true;
        return user.getStatus().equalsIgnoreCase(newVal);
    });
});
```

### Feature 6: Statistics Dashboard (Admin)

**Statistics Cards:**
```java
private VBox createStatCard(String title, String value, String color) {
    VBox card = new VBox(10);
    card.getStyleClass().add("stat-card");
    card.setStyle("-fx-background-color: linear-gradient(to bottom right, " + color + ", " + color + "cc);");

    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

    Label valueLabel = new Label(value);
    valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 36; -fx-font-weight: bold;");

    card.getChildren().addAll(titleLabel, valueLabel);
    return card;
}

// Usage
VBox totalUsersCard = createStatCard("Total Users", "42", "#2196F3");
VBox activeUsersCard = createStatCard("Active Users", "38", "#4CAF50");
VBox blockedUsersCard = createStatCard("Blocked Users", "4", "#F44336");
```

**Pie Chart (Users by Role):**
```java
private PieChart createRoleDistributionChart() {
    Map<String, Long> roleCount = new HashMap<>();
    for (User user : users) {
        roleCount.put(user.getRoleName(),
            roleCount.getOrDefault(user.getRoleName(), 0L) + 1);
    }

    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    for (Map.Entry<String, Long> entry : roleCount.entrySet()) {
        pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
    }

    PieChart chart = new PieChart(pieData);
    chart.setTitle("Users by Role");
    return chart;
}
```

**Bar Chart (Registrations Last 7 Days):**
```java
private BarChart<String, Number> createRegistrationChart() {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("New Registrations");

    LocalDate today = LocalDate.now();
    for (int i = 6; i >= 0; i--) {
        LocalDate date = today.minusDays(i);
        long count = users.stream()
            .filter(u -> u.getCreatedAt().toLocalDate().equals(date))
            .count();
        series.getData().add(new XYChart.Data<>(date.toString(), count));
    }

    chart.getData().add(series);
    return chart;
}
```

---

## 10. Code Flow Examples

### Example 1: Complete Login Flow

**User Action:** User enters `farmer@test.com` / `farmer123` and clicks "Login"

**Step-by-Step Execution:**

```
1. LoginController.handleLogin() called
   ↓
2. Get input values:
   email = "farmer@test.com"
   password = "farmer123"
   ↓
3. Validate input not empty
   if (email.isEmpty() || password.isEmpty()) → Show error
   ✓ Pass
   ↓
4. Call UserService.authenticate(email, password)
   ↓
5. UserService.getByEmail("farmer@test.com")
   SQL: SELECT * FROM users WHERE email = ?
   ↓
6. Database returns:
   id=2, name="Test Farmer", email="farmer@test.com",
   password="$2a$10$92IXUNpkjO0rOQ5byMi.Ye...", role_id=2, status="active"
   ↓
7. UserService.mapRow(ResultSet) → User object
   ↓
8. BCrypt.checkpw("farmer123", "$2a$10$92IXUNpkjO...")
   ✓ Match!
   ↓
9. Return User object to controller
   ↓
10. Check user.getStatus() == "blocked"?
    ✓ No, status is "active"
   ↓
11. SessionManager.setCurrentUser(user)
    Static field currentUser = user object
   ↓
12. Navigate to dashboard:
    FXMLLoader.load("dashboard.fxml")
    DashboardController.setCurrentUser(user)
   ↓
13. DashboardController.initialize()
    ↓
14. Load user's role from database:
    RoleService.getById(user.getRoleId())
    Returns: Role(id=2, name="Farmer")
    user.setRoleName("Farmer")
   ↓
15. Build sidebar menu:
    if "Farmer" → Show Farmer menu items
   ↓
16. Show home view:
    contentArea.getChildren().add(homeView)
   ↓
17. Dashboard displayed!
    Sidebar shows: Home, Profile, My Farms, Shop, etc.
```

**Database Queries Executed:**
```sql
-- Query 1 (UserService.getByEmail)
SELECT * FROM users WHERE email = 'farmer@test.com';

-- Query 2 (RoleService.getById)
SELECT * FROM roles WHERE id = 2;
```

**Objects Created:**
```java
User(id=2, name="Test Farmer", email="farmer@test.com", roleId=2, roleName="Farmer", status="active")
Role(id=2, name="Farmer", description="Farm and product management")
```

### Example 2: Complete Google OAuth Flow

**User Action:** User clicks "🔐 Sign in with Google"

**Step-by-Step Execution:**

```
1. LoginController.handleGoogleLogin() called
   ↓
2. Create OAuthLoginDialog instance
   ↓
3. Call showGoogleLogin(stage)
   ↓
4. GoogleOAuthService.getAuthorizationUrl()
   Returns: "https://accounts.google.com/o/oauth2/v2/auth?
             client_id=178628032183...&
             redirect_uri=http://localhost:3000/oauth/callback&
             response_type=code&
             scope=email%20profile&
             state=7f3d92ac-..."
   ↓
5. Show "Opening browser..." dialog
   ↓
6. Start background thread:
   Thread {
     // Step A: Start callback server
     OAuthCallbackServer server = new OAuthCallbackServer()
     server.waitForCallback(60)

     // Step B: Open browser
     Desktop.getDesktop().browse(authUrl)
   }
   ↓
7. Browser opens to Google consent screen:
   "AgriCloud wants to access your Google Account
    Email: john@gmail.com
    [Choose an account] [Cancel] [Allow]"
   ↓
8. User clicks "Allow"
   ↓
9. Google redirects browser to:
   http://localhost:3000/oauth/callback?code=4/0AanRRrvYXXX&state=7f3d92ac-...
   ↓
10. OAuthCallbackServer receives HTTP GET request:
    GET /oauth/callback?code=4/0AanRRrvYXXX&state=7f3d92ac-...
   ↓
11. Extract code from query string:
    authCode = "4/0AanRRrvYXXX"
   ↓
12. Send success HTML to browser:
    HTTP 200 OK
    <html><body><h1>✅ Login Successful!</h1></body></html>
   ↓
13. User sees success page in browser
   ↓
14. Server.stop(), latch.countDown()
   ↓
15. Background thread continues with authCode
   ↓
16. GoogleOAuthService.getAccessToken(authCode)
    POST https://oauth2.googleapis.com/token
    Body: {
      code: "4/0AanRRrvYXXX",
      client_id: "178628032183...",
      client_secret: "GOCSPX-2bKFqjr...",
      redirect_uri: "http://localhost:3000/oauth/callback",
      grant_type: "authorization_code"
    }
   ↓
17. Google responds:
    {
      "access_token": "ya29.a0AfH6SMBx...",
      "expires_in": 3599,
      "scope": "email profile",
      "token_type": "Bearer"
    }
   ↓
18. GoogleOAuthService.getUserInfo(accessToken)
    GET https://www.googleapis.com/oauth2/v2/userinfo
    Header: Authorization: Bearer ya29.a0AfH6SMBx...
   ↓
19. Google responds:
    {
      "id": "115432198765432109876",
      "email": "john@gmail.com",
      "name": "John Doe",
      "picture": "https://lh3.googleusercontent.com/a/..."
    }
   ↓
20. UserService.createOrUpdateOAuthUser("google", "115432...", "john@gmail.com", "John Doe", "https://...")
   ↓
21. Check if OAuth ID exists:
    SQL: SELECT * FROM users WHERE oauth_provider='google' AND oauth_id='115432...'
    Result: NULL (first time login)
   ↓
22. Check if email exists:
    SQL: SELECT * FROM users WHERE email='john@gmail.com'
    Result: NULL (new user)
   ↓
23. Create new user:
    INSERT INTO users (role_id, name, email, password, profile_picture, oauth_provider, oauth_id, status)
    VALUES (3, 'John Doe', 'john@gmail.com', NULL, 'https://...', 'google', '115432...', 'active')
   ↓
24. Retrieve created user:
    SQL: SELECT * FROM users WHERE email='john@gmail.com'
   ↓
25. Return user object to dialog
   ↓
26. Dialog closes, return user to LoginController
   ↓
27. SessionManager.setCurrentUser(user)
   ↓
28. Navigate to dashboard
   ↓
29. User logged in!
```

**Network Requests:**
```
1. Browser → Google: GET https://accounts.google.com/o/oauth2/v2/auth?...
2. Google → Browser: Redirect to http://localhost:3000?code=XXX
3. Browser → Localhost: GET http://localhost:3000/oauth/callback?code=XXX
4. JavaFX → Google: POST https://oauth2.googleapis.com/token (get token)
5. Google → JavaFX: Response with access_token
6. JavaFX → Google: GET https://www.googleapis.com/oauth2/v2/userinfo (get user)
7. Google → JavaFX: Response with user data
```

### Example 3: Complete User Creation (Admin)

**User Action:** Admin clicks "Add User" button

**Step-by-Step Execution:**

```
1. UsersController.handleAddUser() called
   ↓
2. Create Dialog
   dialog = new Dialog<>()
   dialog.setTitle("Add New User")
   ↓
3. Build form fields:
   nameField = new TextField()
   emailField = new TextField()
   passwordField = new PasswordField()
   phoneField = new TextField()
   roleCombo = new ComboBox<>()
   statusCombo = new ComboBox<>()
   ↓
4. Load roles from database:
   RoleService.getAll()
   SQL: SELECT * FROM roles
   Returns: [Admin, Farmer, Customer, Guest]
   roleCombo.setItems(roles)
   ↓
5. Add form to dialog:
   GridPane grid = new GridPane()
   grid.add("Name:", nameField)
   grid.add("Email:", emailField)
   ...
   dialog.getDialogPane().setContent(grid)
   ↓
6. Show dialog and wait for user:
   Optional<ButtonType> result = dialog.showAndWait()
   ↓
7. User fills form:
   Name: "New Farmer"
   Email: "new@farmer.com"
   Password: "test123"
   Phone: "+1234567890"
   Role: Farmer
   Status: Active
   ↓
8. User clicks "OK"
   result.get() == ButtonType.OK ✓
   ↓
9. Get form values:
   String name = nameField.getText().trim() → "New Farmer"
   String email = emailField.getText().trim() → "new@farmer.com"
   String password = passwordField.getText() → "test123"
   ↓
10. Validate name:
    ValidationUtils.isValidName(name)
    name.length() >= 2? ✓ Yes (11 characters)
   ↓
11. Validate email:
    ValidationUtils.isValidEmail(email)
    EMAIL_PATTERN.matcher(email).matches()? ✓ Yes
   ↓
12. Validate phone:
    ValidationUtils.isValidPhone(phone)
    PHONE_PATTERN.matcher(phone).matches()? ✓ Yes
   ↓
13. Validate password:
    ValidationUtils.isValidPassword(password)
    password.length() >= 6? ✓ Yes (7 characters)
   ↓
14. Check duplicate email:
    UserService.getByEmail("new@farmer.com")
    SQL: SELECT * FROM users WHERE email = 'new@farmer.com'
    Result: NULL ✓ (email available)
   ↓
15. Create User object:
    User newUser = new User()
    newUser.setRoleId(2) // Farmer
    newUser.setName("New Farmer")
    newUser.setEmail("new@farmer.com")
    newUser.setPassword("test123")
    newUser.setPhone("+1234567890")
    newUser.setStatus("active")
   ↓
16. Call UserService.add(newUser)
   ↓
17. Hash password:
    BCrypt.hashpw("test123", BCrypt.gensalt())
    Returns: "$2a$10$AbCdEf..." (60-character hash)
   ↓
18. Insert into database:
    SQL: INSERT INTO users (role_id, name, email, password, phone, status)
         VALUES (?, ?, ?, ?, ?, ?)
    Parameters: [2, "New Farmer", "new@farmer.com", "$2a$10$...", "+1234567890", "active"]
   ↓
19. Database assigns auto-increment ID:
    New user ID = 43
   ↓
20. Return to controller
   ↓
21. Show success message:
    Alert("User added successfully!")
   ↓
22. Refresh table:
    refreshUsersTable()
    → Re-query all users from database
    → Update TableView
   ↓
23. New user appears in table!
```

**Database State After:**
```sql
-- New row in users table:
id  | role_id | name        | email              | password       | phone         | status
----|---------|-------------|--------------------| ---------------|---------------|--------
43  | 2       | New Farmer  | new@farmer.com     | $2a$10$AbC... | +1234567890   | active
```

---

## Summary

### Key Takeaways for Teachers

1. **Architecture:**
   - Clean 3-tier MVC pattern
   - Separation of concerns (UI, Business Logic, Data)
   - Service layer for database access

2. **Security:**
   - BCrypt password hashing (industry standard)
   - PreparedStatement (SQL injection prevention)
   - OAuth 2.0 integration (Google Sign-In)
   - Input validation (regex patterns)
   - Session management

3. **Database:**
   - Normalized schema (users, roles, password_resets)
   - Foreign key relationships
   - Indexes for performance
   - ENUM types for status
   - Nullable password (OAuth support)

4. **Authentication:**
   - Traditional (email/password)
   - OAuth (Google)
   - Guest mode (unique per session)
   - Role-based access control

5. **Code Quality:**
   - Consistent naming conventions
   - Code reuse (mapRow, validation loops)
   - Error handling
   - Comments and documentation

6. **Technologies:**
   - JavaFX 17 (modern desktop UI)
   - Maven (dependency management)
   - MySQL (relational database)
   - OAuth 2.0 (authentication protocol)
   - SMTP (email sending)

### Defense Questions You Should Be Prepared For

**Q1: Why did you use BCrypt instead of SHA-256?**
A: BCrypt is designed for password hashing with built-in salt and adjustable cost factor. SHA-256 is too fast and vulnerable to brute force attacks. BCrypt automatically handles salting and is slow by design.

**Q2: What is the purpose of PreparedStatement?**
A: Prevents SQL injection by separating SQL code from data. User input is treated as data, not executable code.

**Q3: How does OAuth work?**
A: 3-step process: 1) User authorizes app at Google, 2) Google redirects with authorization code, 3) App exchanges code for access token to fetch user info.

**Q4: Why is password nullable in the database?**
A: Users who sign in with Google don't need a password. They authenticate through OAuth, so password field can be NULL.

**Q5: What is the singleton pattern in DatabaseConnection?**
A: Ensures only one database connection exists. The `connection` field is static and shared across all service instances.

**Q6: How do you handle role-based access?**
A: SessionManager stores current user with roleName. Controllers check role before showing features (e.g., if isAdmin() → show Users management).

**Q7: What happens if the email server is down?**
A: EmailUtils.sendEmail() returns false, and we show an error message to the user. The password reset flow doesn't complete.

**Q8: Why use FilteredList for search?**
A: Reactive UI - automatically updates table when user types. No need to manually re-query database on every keystroke.

---

**This document was created to help explain the AgriCloud User Management system architecture and implementation details. Use it to prepare for project defense questions from your teachers.**
