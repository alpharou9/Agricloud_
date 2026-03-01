# AgriCloud — Smart Farm Management System

> A full-featured desktop application built with JavaFX for managing farms, products, blog posts, events, and users in a unified agricultural ecosystem.

---

## Team

| Module | Developer | Entities |
|--------|-----------|----------|
| Module 1 — User Management | Farouk | User, Role |
| Module 2 — Farm Management | Shahed | Farm, Field |
| Module 3 — Market Management | Ghada | Product, Order, Cart |
| Module 4 — Blog Management | Rania | Post, Comment |
| Module 5 — Event Management | Ayman | Event, Participation |

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| UI Framework | JavaFX 17+ |
| Build Tool | Maven |
| Database | MySQL 8.0+ via WAMP |
| DB GUI | phpMyAdmin |
| Auth | BCrypt password hashing |
| Email | Gmail SMTP (JavaMail) |
| SMS | SMSUtils (Twilio) |
| AI / Biometrics | OpenCV + JavaCV (YuNet + SFace) |
| AI / Chatbot | ChatbotService |
| OAuth | Google OAuth 2.0 |
| QR Code | ZXing |
| Translation | TranslationUtils |
| JSON | Gson |
| Theme | AtlantaFX PrimerLight |

---

## Features

### Module 1 — User Management (Farouk)
- Login with email/password (BCrypt) + blocked user prevention
- Register with role selection (Farmer / Customer)
- Forgot password — 6-digit code via Gmail SMTP
- **Google OAuth 2.0** — sign in with Google (system browser flow)
- **Face Recognition Login** — biometric authentication using OpenCV YuNet detection + SFace embeddings (128D vectors), 0.6 Euclidean distance threshold
- Face enrollment via Profile (5 captures from different angles)
- Guest login — UUID-isolated sessions, auto-cleanup after 24h
- Admin: Users CRUD, Roles CRUD, Block/Unblock, live search + status filter
- Admin: Statistics dashboard (pie chart by role, bar chart last 7 days)
- Profile editing (name, email, phone, password)
- **Contact QR Code** on profile — vCard 3.0 format, iPhone-compatible
- Role-based sidebar (Admin / Farmer / Customer / Guest)
- Input validation on all forms (email regex, phone regex, name min 2 chars)

### Module 2 — Farm Management (Shahed)
- Farm CRUD with approval workflow (pending → approved / rejected)
- Field management nested inside each farm
- Interactive map picker (WebView + Leaflet / OpenStreetMap) for coordinates
- Admin: view and approve/reject all farms
- Farmer: manage own farms and fields
- Farm status: pending, approved, rejected, inactive

### Module 3 — Market Management (Ghada)
- Product CRUD with approval workflow
- Shopping cart (add, update quantity, remove, checkout)
- Order flow with full shipping details
- Order confirmation emails sent automatically (Gmail SMTP)
- Admin: approve/reject products, manage all orders
- Farmer: manage own products and incoming orders
- Customer: browse shop, cart, own orders
- Categories: Fruits, Vegetables, Dairy, Meat, Grains, Herbs, Honey, Eggs, Other

### Module 4 — Blog Management (Rania)
- Post CRUD with publish/unpublish workflow
- Comment system (add, delete own, post owner deletes any)
- Admin: block/unblock users directly from comment section
- Author names displayed via JOIN
- Categories: Agriculture, Technology, Farming Tips, Livestock, Organic, Weather, Equipment, Success Stories, Other
- Guest: view only (no commenting)

### Module 5 — Event Management (Ayman)
- Event CRUD with capacity management and registration deadline
- Participation management (register, cancel, mark attended)
- Admin: manage all events, view and manage participants
- Farmer/Customer: browse upcoming events, register, view own registrations
- Capacity check on registration

---

## Role Permissions

| Feature | Admin | Farmer | Customer | Guest |
|---------|-------|--------|----------|-------|
| Users / Roles CRUD | ✅ | ❌ | ❌ | ❌ |
| Statistics | ✅ | ❌ | ❌ | ❌ |
| Profile editing | ✅ | ✅ | ✅ | ❌ |
| Face Recognition | ✅ | ✅ | ✅ | ❌ |
| Farm management | ✅ | ✅ | ❌ | ❌ |
| Products / Market | ✅ | ✅ | ✅ | ✅ |
| Shopping cart | ✅ | ✅ | ✅ | ✅ |
| Blog (view) | ✅ | ✅ | ✅ | ✅ |
| Blog (comment) | ✅ | ✅ | ✅ | ❌ |
| My Posts | ✅ | ✅ | ✅ | ❌ |
| Events | ✅ | ✅ | ✅ | ✅ |

---

## Database Schema

**14 tables total:**

`roles` · `users` · `farms` · `fields` · `products` · `orders` · `shopping_cart` · `posts` · `comments` · `events` · `participations` · `password_resets` · `user_activity_logs`

### Setup

**1. Start WAMP** (icon must be green — both Apache and MySQL running)

**2. Create the database**
```sql
CREATE DATABASE agricloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agricloud;
```

**3. Run the schema**

Open phpMyAdmin at `http://localhost/phpmyadmin`, select the `agricloud` database, go to the **SQL** tab and run the full schema from `SQL_COMMANDS_GUIDE.txt`.

**4. Verify**
```sql
SHOW TABLES; -- should return 14 tables
```

### Connection settings

```
Host:     localhost
Port:     3306
User:     root
Password: (empty — WAMP default)
Database: agricloud
```

---

## Installation & Running

### Prerequisites
- JDK 17+
- Apache Maven 3.6+
- WAMP Server (MySQL on port 3306)
- IntelliJ IDEA (recommended)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/alpharou9/Agricloud_.git
cd Agricloud_

# 2. Install dependencies
mvn clean install

# 3. Run the application
mvn javafx:run
```

### Gmail SMTP setup (for password reset and order emails)
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an **App Password** (Google Account → Security → App Passwords)
3. Update `EmailUtils.java`:
```java
private static final String SENDER_EMAIL = "your-email@gmail.com";
private static final String SENDER_PASSWORD = "your-app-password";
```

### Google OAuth setup
1. Create a project at [Google Cloud Console](https://console.cloud.google.com)
2. Enable the Google+ API
3. Create OAuth 2.0 credentials
4. Add `http://localhost:3000/oauth/callback` as an authorized redirect URI
5. Update `OAuthConfig.java` with your Client ID and Secret

---

## Project Structure

```
src/
├── main/
│   ├── java/esprit/farouk/
│   │   ├── Main.java
│   │   ├── config/
│   │   │   └── DatabaseConfig.java
│   │   ├── controllers/
│   │   │   ├── LoginController.java
│   │   │   ├── RegisterController.java
│   │   │   ├── ForgotPasswordController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── UserManagementController.java
│   │   │   ├── FarmController.java
│   │   │   ├── MarketController.java
│   │   │   ├── BlogController.java
│   │   │   ├── EventController.java
│   │   │   ├── FaceLoginController.java
│   │   │   └── FaceEnrollmentController.java
│   │   ├── database/
│   │   │   └── DatabaseConnection.java
│   │   ├── models/
│   │   │   ├── User.java         ├── Role.java
│   │   │   ├── Farm.java         ├── Field.java
│   │   │   ├── Product.java      ├── Order.java
│   │   │   ├── CartItem.java     ├── Post.java
│   │   │   ├── Comment.java      ├── Event.java
│   │   │   ├── Participation.java
│   │   │   └── FaceEmbedding.java
│   │   ├── services/
│   │   │   ├── UserService.java        ├── RoleService.java
│   │   │   ├── FarmService.java        ├── FieldService.java
│   │   │   ├── ProductService.java     ├── OrderService.java
│   │   │   ├── CartService.java        ├── PostService.java
│   │   │   ├── CommentService.java     ├── EventService.java
│   │   │   ├── ParticipationService.java
│   │   │   ├── FaceRecognitionService.java
│   │   │   └── ChatbotService.java
│   │   └── utils/
│   │       ├── ValidationUtils.java    ├── EmailUtils.java
│   │       ├── SMSUtils.java           ├── QRCodeUtils.java
│   │       ├── FaceUtils.java          ├── CameraUtils.java
│   │       ├── TranslationUtils.java   ├── UIUtils.java
│   └── resources/
│       ├── fxml/
│       │   ├── login.fxml          ├── register.fxml
│       │   ├── forgot_password.fxml
│       │   ├── dashboard.fxml
│       │   ├── face_login.fxml
│       │   └── face_enrollment.fxml
│       ├── css/style.css
│       └── images/
└── uploads/
```

---

## Test Credentials

```
Admin:    admin@admin.com       / farouk
Farmer:   farmer@farmer.com     / farouk
Customer: customer@customer.com / farouk
```

---

## Database Tables Reference

### Module 1 — User Management
| Table | Description |
|-------|-------------|
| `roles` | System roles (Admin, Farmer, Customer, Guest) |
| `users` | User accounts with BCrypt passwords and OAuth support |
| `password_resets` | 6-digit reset codes with expiry |

### Module 2 — Farm Management
| Table | Description |
|-------|-------------|
| `farms` | Farm records with geolocation and approval status |
| `fields` | Sub-areas within a farm with soil/crop data |

### Module 3 — Market Management
| Table | Description |
|-------|-------------|
| `products` | Marketplace listings with approval workflow |
| `orders` | Purchase orders with full shipping details |
| `shopping_cart` | Per-user cart with unique user-product constraint |

### Module 4 — Blog Management
| Table | Description |
|-------|-------------|
| `posts` | Blog articles with publish/draft status |
| `comments` | Nested comment system with moderation |

### Module 5 — Event Management
| Table | Description |
|-------|-------------|
| `events` | Events with capacity and geolocation |
| `participations` | User registrations with attendance tracking |

---

## Architecture

The application follows a **3-layer architecture**:

- **Model layer** — plain Java objects mapping to database entities
- **Service layer** — all database logic using `PreparedStatement` (SQL injection prevention), business rules, `mapRow()` helpers
- **Controller layer** — JavaFX controllers handling UI only, delegating all logic to services

All dashboard views are built **programmatically** in `DashboardController` (no separate FXML per view), with content swapped via `contentArea.getChildren().clear()`. Tables use `FilteredList` + `SortedList` for live search without additional database calls.

---

## Common Issues

| Issue | Solution |
|-------|----------|
| MySQL not connecting | Verify WAMP is running (green icon), port 3306 free |
| JavaFX not found | Add VM options: `--module-path <javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml` |
| Email not sending | Use Gmail App Password, not your regular password |
| OAuth port conflict | Ensure port 3000 is free: `netstat -ano \| findstr :3000` |
| Face recognition fails | Ensure webcam is connected and ONNX models are in `src/main/resources/models/` |
