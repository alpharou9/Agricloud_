package esprit.farouk;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.SessionManager;
import esprit.farouk.utils.ValidationUtils;

/**
 * Foundation Classes Test
 * Tests all service layer methods, authentication, validation, and session management
 *
 * Prerequisites:
 * 1. Database must be set up (run DatabaseTest first)
 * 2. Default roles and users must exist
 */
public class FoundationTest {

    private static UserService userService = new UserService();
    private static RoleService roleService = new RoleService();
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  FOUNDATION CLASSES TEST SUITE");
        System.out.println("========================================\n");

        // Run all test suites
        testValidationUtils();
        testRoleService();
        testUserService();
        testAuthentication();
        testSessionManager();

        // Print summary
        printSummary();
    }

    // ==================== VALIDATION TESTS ====================
    private static void testValidationUtils() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  VALIDATION UTILS TESTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Email validation
        assertTest("Valid email test", ValidationUtils.isValidEmail("test@example.com"), true);
        assertTest("Invalid email test", ValidationUtils.isValidEmail("invalid-email"), false);
        assertTest("Empty email test", ValidationUtils.isValidEmail(""), false);

        // Phone validation
        assertTest("Valid phone test", ValidationUtils.isValidPhone("+1234567890"), true);
        assertTest("Valid phone (optional)", ValidationUtils.isValidPhone(""), true);
        assertTest("Invalid phone test", ValidationUtils.isValidPhone("abc"), false);

        // Name validation
        assertTest("Valid name test", ValidationUtils.isValidName("John Doe"), true);
        assertTest("Short name test", ValidationUtils.isValidName("J"), false);
        assertTest("Empty name test", ValidationUtils.isValidName(""), false);

        // Password validation
        assertTest("Valid password test", ValidationUtils.isValidPassword("password123"), true);
        assertTest("Short password test", ValidationUtils.isValidPassword("12345"), false);
        assertTest("Empty password test", ValidationUtils.isValidPassword(""), false);

        System.out.println();
    }

    // ==================== ROLE SERVICE TESTS ====================
    private static void testRoleService() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  ROLE SERVICE TESTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Get all roles
        var roles = roleService.getAll();
        assertTest("Get all roles", roles != null && roles.size() >= 4, true);

        if (roles != null && roles.size() > 0) {
            System.out.println("  Found roles:");
            for (Role role : roles) {
                System.out.println("    - " + role.getName() + ": " + role.getDescription());
            }
        }

        // Get role by name
        Role adminRole = roleService.getByName("Admin");
        assertTest("Get Admin role by name", adminRole != null && "Admin".equals(adminRole.getName()), true);

        Role farmerRole = roleService.getByName("Farmer");
        assertTest("Get Farmer role by name", farmerRole != null && "Farmer".equals(farmerRole.getName()), true);

        // Get registration roles (should only return Farmer and Customer)
        var regRoles = roleService.getRegistrationRoles();
        assertTest("Get registration roles", regRoles != null && regRoles.size() == 2, true);

        System.out.println();
    }

    // ==================== USER SERVICE TESTS ====================
    private static void testUserService() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  USER SERVICE TESTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Get all users
        var users = userService.getAll();
        assertTest("Get all users", users != null && users.size() >= 3, true);

        if (users != null && users.size() > 0) {
            System.out.println("  Found users:");
            for (User user : users) {
                System.out.println("    - " + user.getName() + " (" + user.getEmail() + ") - " + user.getRoleName());
            }
        }

        // Get user by email
        User adminUser = userService.getByEmail("admin@admin.com");
        assertTest("Get admin user by email", adminUser != null && "admin@admin.com".equals(adminUser.getEmail()), true);

        // Email exists check
        assertTest("Check if admin email exists", userService.emailExists("admin@admin.com"), true);
        assertTest("Check if fake email exists", userService.emailExists("fake@fake.com"), false);

        // Create a test user
        Role customerRole = roleService.getByName("Customer");
        if (customerRole != null) {
            User testUser = new User(customerRole.getId(), "Test User", "test@test.com", "password123");
            testUser.setPhone("+9876543210");

            // Check if user already exists (from previous test run)
            if (userService.emailExists("test@test.com")) {
                System.out.println("  ℹ Test user already exists from previous run");
                testUser = userService.getByEmail("test@test.com");
                assertTest("Get existing test user", testUser != null, true);
            } else {
                boolean created = userService.create(testUser);
                assertTest("Create new test user", created && testUser.getId() > 0, true);
            }

            // Update test user
            if (testUser != null) {
                testUser.setName("Updated Test User");
                boolean updated = userService.update(testUser);
                assertTest("Update test user", updated, true);

                // Verify update
                User updatedUser = userService.getById(testUser.getId());
                assertTest("Verify user update", updatedUser != null && "Updated Test User".equals(updatedUser.getName()), true);
            }

            // Search users
            var searchResults = userService.search("test");
            assertTest("Search users by keyword", searchResults != null && searchResults.size() > 0, true);
        }

        System.out.println();
    }

    // ==================== AUTHENTICATION TESTS ====================
    private static void testAuthentication() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  AUTHENTICATION TESTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Test with default credentials from CLAUDE.md
        User authenticatedAdmin = userService.authenticate("admin@admin.com", "farouk");
        assertTest("Admin login with correct password", authenticatedAdmin != null, true);

        User authenticatedFarmer = userService.authenticate("farmer@farmer.com", "farouk");
        assertTest("Farmer login with correct password", authenticatedFarmer != null, true);

        // Test with wrong password
        User failedAuth = userService.authenticate("admin@admin.com", "wrongpassword");
        assertTest("Login with wrong password", failedAuth == null, true);

        // Test with non-existent user
        User nonExistent = userService.authenticate("nonexistent@email.com", "password");
        assertTest("Login with non-existent user", nonExistent == null, true);

        System.out.println();
    }

    // ==================== SESSION MANAGER TESTS ====================
    private static void testSessionManager() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SESSION MANAGER TESTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Initially no user logged in
        assertTest("No user logged in initially", !SessionManager.isLoggedIn(), true);

        // Login admin user
        User adminUser = userService.authenticate("admin@admin.com", "farouk");
        if (adminUser != null) {
            SessionManager.setCurrentUser(adminUser);

            assertTest("User logged in", SessionManager.isLoggedIn(), true);
            assertTest("Is admin check", SessionManager.isAdmin(), true);
            assertTest("Is not farmer check", !SessionManager.isFarmer(), true);
            assertTest("Is not customer check", !SessionManager.isCustomer(), true);

            User currentUser = SessionManager.getCurrentUser();
            assertTest("Get current user", currentUser != null && currentUser.getId() == adminUser.getId(), true);

            // Logout
            SessionManager.logout();
            assertTest("User logged out", !SessionManager.isLoggedIn(), true);
        }

        // Test with farmer role
        User farmerUser = userService.authenticate("farmer@farmer.com", "farouk");
        if (farmerUser != null) {
            SessionManager.setCurrentUser(farmerUser);

            assertTest("Farmer logged in", SessionManager.isLoggedIn(), true);
            assertTest("Is farmer check", SessionManager.isFarmer(), true);
            assertTest("Is not admin check", !SessionManager.isAdmin(), true);

            SessionManager.logout();
        }

        System.out.println();
    }

    // ==================== TEST UTILITIES ====================
    private static void assertTest(String testName, boolean actual, boolean expected) {
        testsRun++;
        if (actual == expected) {
            testsPassed++;
            System.out.println("✓ PASS: " + testName);
        } else {
            testsFailed++;
            System.err.println("✗ FAIL: " + testName + " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    private static void printSummary() {
        System.out.println("========================================");
        System.out.println("  TEST SUMMARY");
        System.out.println("========================================");
        System.out.println("Total tests run:    " + testsRun);
        System.out.println("Tests passed:       " + testsPassed + " ✓");
        if (testsFailed > 0) {
            System.out.println("Tests failed:       " + testsFailed + " ✗");
        }

        double successRate = (testsRun > 0) ? (testsPassed * 100.0 / testsRun) : 0;
        System.out.println("Success rate:       " + String.format("%.1f", successRate) + "%");
        System.out.println("========================================");

        if (testsFailed == 0) {
            System.out.println("\n🎉 ALL TESTS PASSED! Foundation is solid!");
            System.out.println("Ready to proceed with UI development.");
        } else {
            System.err.println("\n⚠ Some tests failed. Review the errors above.");
        }
        System.out.println("========================================\n");
    }
}
