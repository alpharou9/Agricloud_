-- AgriCloud Database Setup
-- Run this in phpMyAdmin SQL tab

USE agricloud;

-- Create password_resets table
CREATE TABLE IF NOT EXISTS password_resets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    reset_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_reset_code (reset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles (if not exists)
INSERT IGNORE INTO roles (name, description, permissions) VALUES
('Admin', 'Full system access with all permissions', '["all"]'),
('Farmer', 'Farm and product management', '["farms", "products", "orders"]'),
('Customer', 'Shopping and order management', '["shop", "cart", "orders"]'),
('Guest', 'Limited read-only access', '["shop_view", "blog_view"]');

-- Insert default users (if not exists)
-- Password for all: farouk
INSERT IGNORE INTO users (role_id, name, email, password, status) VALUES
(1, 'Admin User', 'admin@admin.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'active'),
(2, 'Farmer User', 'farmer@farmer.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'active'),
(3, 'Customer User', 'customer@customer.com', '$2a$10$YC5J0hclRGp0nZ6CYR3t5.JQqmhVHfLGWPx8PdJxRqH3fYE6JVLCe', 'active');

SELECT '✓ Database setup complete!' as Status;
