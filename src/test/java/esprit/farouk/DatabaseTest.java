package esprit.farouk;

import esprit.farouk.services.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Database Connection Test
 * Run this AFTER setting up the database with SQL scripts
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  DATABASE CONNECTION TEST");
        System.out.println("========================================\n");

        // Test 1: Database Connection
        System.out.println("Test 1: Checking database connection...");
        Connection conn = DatabaseConnection.getConnection();

        if (conn == null) {
            System.err.println("✗ FAILED: Database connection is null");
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Make sure WAMP is running (icon should be GREEN)");
            System.err.println("2. Check if MySQL service is started");
            System.err.println("3. Verify database 'agricloud' exists in phpMyAdmin");
            System.err.println("4. Confirm connection details in DatabaseConnection.java");
            return;
        }

        System.out.println("✓ PASSED: Database connection successful\n");

        // Test 2: Check if tables exist
        System.out.println("Test 2: Checking if required tables exist...");
        try (Statement stmt = conn.createStatement()) {
            String[] tables = {"roles", "users", "password_resets"};
            boolean allTablesExist = true;

            for (String table : tables) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'agricloud' AND table_name = '" + table + "'"
                );

                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("  ✓ Table '" + table + "' exists");
                } else {
                    System.err.println("  ✗ Table '" + table + "' NOT FOUND");
                    allTablesExist = false;
                }
            }

            if (!allTablesExist) {
                System.err.println("\n✗ FAILED: Some tables are missing");
                System.err.println("\nAction Required:");
                System.err.println("Run the SQL scripts from CLAUDE.md (lines 212-278) in phpMyAdmin");
                return;
            }

            System.out.println("✓ PASSED: All required tables exist\n");

        } catch (Exception e) {
            System.err.println("✗ FAILED: Error checking tables");
            e.printStackTrace();
            return;
        }

        // Test 3: Check if default data exists
        System.out.println("Test 3: Checking if default roles exist...");
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM roles");

            if (rs.next()) {
                int roleCount = rs.getInt(1);
                if (roleCount >= 4) {
                    System.out.println("  ✓ Found " + roleCount + " roles");

                    // Show roles
                    ResultSet rolesRs = stmt.executeQuery("SELECT name FROM roles ORDER BY name");
                    System.out.println("  Roles:");
                    while (rolesRs.next()) {
                        System.out.println("    - " + rolesRs.getString("name"));
                    }
                    System.out.println("✓ PASSED: Default roles exist\n");
                } else {
                    System.err.println("✗ FAILED: Expected at least 4 roles, found " + roleCount);
                    System.err.println("Run the INSERT statements from CLAUDE.md");
                    return;
                }
            }

        } catch (Exception e) {
            System.err.println("✗ FAILED: Error checking roles");
            e.printStackTrace();
            return;
        }

        // Test 4: Check if default users exist
        System.out.println("Test 4: Checking if default users exist...");
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT u.name, u.email, r.name as role FROM users u " +
                "LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.id"
            );

            int userCount = 0;
            System.out.println("  Default users:");
            while (rs.next()) {
                userCount++;
                System.out.println("    - " + rs.getString("name") +
                                 " (" + rs.getString("email") + ") - Role: " + rs.getString("role"));
            }

            if (userCount >= 3) {
                System.out.println("✓ PASSED: Found " + userCount + " default users\n");
            } else {
                System.err.println("✗ WARNING: Expected at least 3 users, found " + userCount);
                System.err.println("You may need to run the INSERT statements for users\n");
            }

        } catch (Exception e) {
            System.err.println("✗ FAILED: Error checking users");
            e.printStackTrace();
            return;
        }

        // Summary
        System.out.println("========================================");
        System.out.println("  ✓ ALL TESTS PASSED!");
        System.out.println("========================================");
        System.out.println("Database is ready for the application.");
        System.out.println("\nYou can now run FoundationTest to test services.");
        System.out.println("========================================\n");

        DatabaseConnection.closeConnection();
    }
}
