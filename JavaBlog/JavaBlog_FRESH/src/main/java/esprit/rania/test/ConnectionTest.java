package esprit.rania.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectionTest {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("DATABASE CONNECTION TROUBLESHOOTER");
        System.out.println("=".repeat(60));

        // Test connection parameters
        String host = "localhost";
        String port = "3306";
        String user = "root";
        String pass = "";
        
        System.out.println("\n📋 CONNECTION PARAMETERS:");
        System.out.println("   Host: " + host);
        System.out.println("   Port: " + port);
        System.out.println("   User: " + user);
        System.out.println("   Pass: " + (pass.isEmpty() ? "(empty)" : "***"));

        // Test 1: Can we load the MySQL driver?
        System.out.println("\n🔌 TEST 1: Loading MySQL Driver...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("   ✅ MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("   ❌ MySQL Driver NOT found!");
            System.err.println("   Solution: Check pom.xml has mysql-connector-j dependency");
            return;
        }

        // Test 2: Can we connect to MySQL server (without database)?
        System.out.println("\n🔌 TEST 2: Connecting to MySQL Server...");
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(serverUrl, user, pass)) {
            System.out.println("   ✅ Connected to MySQL Server!");
            
            // List all databases
            System.out.println("\n📦 Databases on server:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW DATABASES");
            boolean foundJavaBlog = false;
            while (rs.next()) {
                String dbName = rs.getString(1);
                System.out.println("   - " + dbName);
                if (dbName.equals("javablog")) {
                    foundJavaBlog = true;
                }
            }
            
            if (!foundJavaBlog) {
                System.err.println("\n   ❌ 'javablog' database NOT FOUND!");
                System.err.println("   Solution: Run FRESH_START.sql in MySQL Workbench");
                return;
            } else {
                System.out.println("\n   ✅ 'javablog' database exists!");
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Cannot connect to MySQL Server!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("\n   SOLUTIONS:");
            System.err.println("   1. Make sure XAMPP/WAMP MySQL is running");
            System.err.println("   2. Check MySQL is on port 3306");
            System.err.println("   3. Check username is 'root' with empty password");
            System.err.println("   4. Try running this in Command Prompt:");
            System.err.println("      mysql -u root -p");
            return;
        }

        // Test 3: Can we connect to javablog database?
        System.out.println("\n🔌 TEST 3: Connecting to 'javablog' database...");
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/javablog?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            System.out.println("   ✅ Connected to 'javablog' database!");
            
            // Check tables
            System.out.println("\n📊 Tables in database:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            int tableCount = 0;
            while (rs.next()) {
                System.out.println("   - " + rs.getString(1));
                tableCount++;
            }
            
            if (tableCount == 0) {
                System.err.println("\n   ❌ No tables found!");
                System.err.println("   Solution: Run FRESH_START.sql");
                return;
            }
            
            // Check posts count
            System.out.println("\n📝 Checking posts table...");
            rs = stmt.executeQuery("SELECT COUNT(*) FROM posts");
            if (rs.next()) {
                int postCount = rs.getInt(1);
                System.out.println("   Posts in database: " + postCount);
                if (postCount == 0) {
                    System.err.println("   ⚠️  No posts! Run FRESH_START.sql to add sample posts");
                } else {
                    System.out.println("   ✅ Posts found!");
                }
            }
            
            // Show posts
            rs = stmt.executeQuery("SELECT id, title, author FROM posts LIMIT 5");
            System.out.println("\n📰 Sample posts:");
            while (rs.next()) {
                System.out.println("   [" + rs.getInt("id") + "] " + rs.getString("title") + " by " + rs.getString("author"));
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Cannot connect to 'javablog' database!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   Solution: Run FRESH_START.sql in MySQL Workbench");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ ALL TESTS PASSED!");
        System.out.println("Your database is set up correctly.");
        System.out.println("If the JavaFX app still doesn't work, the issue is in");
        System.out.println("DatabaseConnection.java or the JavaFX controllers.");
        System.out.println("=".repeat(60));
    }
}
