package esprit.rania.test;

import esprit.rania.database.DatabaseConnection;
import esprit.rania.services.PostService;
import esprit.rania.models.Post;

import java.sql.Connection;
import java.util.List;

public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("=== DATABASE CONNECTION TEST ===");
        
        // Test 1: Check connection
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("❌ FAILED: Database connection is NULL");
            System.err.println("Check:");
            System.err.println("  - MySQL is running (XAMPP/WAMP)");
            System.err.println("  - Database name is 'javablog'");
            System.err.println("  - Username is 'root'");
            System.err.println("  - Password is empty");
            return;
        }
        System.out.println("✅ Database connection successful!");
        
        // Test 2: Try to fetch posts
        PostService postService = new PostService();
        List<Post> posts = postService.getAllPosts();
        
        System.out.println("\n=== POSTS IN DATABASE ===");
        System.out.println("Total posts found: " + posts.size());
        
        if (posts.isEmpty()) {
            System.err.println("❌ No posts found in database!");
            System.err.println("Solutions:");
            System.err.println("  1. Run database_schema.sql in MySQL Workbench");
            System.err.println("  2. Check if 'posts' table exists");
            System.err.println("  3. Check if 'users' table has at least one user");
        } else {
            System.out.println("✅ Posts found:");
            for (Post post : posts) {
                System.out.println("  - ID: " + post.getId() + " | Title: " + post.getTitle() + " | Author: " + post.getAuthor());
            }
        }
        
        // Test 3: Connection info
        System.out.println("\n=== CONNECTION INFO ===");
        try {
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("URL: " + conn.getMetaData().getURL());
            System.out.println("Username: " + conn.getMetaData().getUserName());
        } catch (Exception e) {
            System.err.println("Error getting connection info: " + e.getMessage());
        }
        
        System.out.println("\n=== TEST COMPLETE ===");
    }
}
