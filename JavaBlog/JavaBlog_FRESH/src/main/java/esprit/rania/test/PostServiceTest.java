package esprit.rania.test;

import esprit.rania.models.Post;
import esprit.rania.services.PostService;
import java.util.List;

/**
 * Simple test class to verify Post CRUD operations
 * Run this class to test database connectivity and service methods
 */
public class PostServiceTest {

    public static void main(String[] args) {
        PostService postService = new PostService();
        
        System.out.println("=== JavaBlog Post Service Test ===\n");

        // Test 1: Create a new post
        System.out.println("1. Testing CREATE operation...");
        Post testPost = new Post(
            "Test Post - " + System.currentTimeMillis(),
            "This is a test post content to verify the create operation is working correctly.",
            "TestUser",
            1
        );
        
        boolean created = postService.createPost(testPost);
        System.out.println("   Post created: " + created);
        System.out.println("   Post ID: " + testPost.getId());
        System.out.println();

        // Test 2: Read all posts
        System.out.println("2. Testing READ ALL operation...");
        List<Post> allPosts = postService.getAllPosts();
        System.out.println("   Total posts found: " + allPosts.size());
        for (Post post : allPosts) {
            System.out.println("   - " + post.getTitle() + " by " + post.getAuthor());
        }
        System.out.println();

        // Test 3: Read specific post
        if (testPost.getId() > 0) {
            System.out.println("3. Testing READ BY ID operation...");
            Post retrievedPost = postService.getPostById(testPost.getId());
            if (retrievedPost != null) {
                System.out.println("   Retrieved post: " + retrievedPost.getTitle());
                System.out.println("   Content: " + retrievedPost.getContent());
            }
            System.out.println();

            // Test 4: Update post
            System.out.println("4. Testing UPDATE operation...");
            retrievedPost.setTitle("Updated Test Post");
            retrievedPost.setContent("This content has been updated!");
            boolean updated = postService.updatePost(retrievedPost);
            System.out.println("   Post updated: " + updated);
            System.out.println();

            // Test 5: Search posts
            System.out.println("5. Testing SEARCH operation...");
            List<Post> searchResults = postService.searchPostsByTitle("Test");
            System.out.println("   Search results for 'Test': " + searchResults.size());
            for (Post post : searchResults) {
                System.out.println("   - " + post.getTitle());
            }
            System.out.println();

            // Test 6: Delete post
            System.out.println("6. Testing DELETE operation...");
            boolean deleted = postService.deletePost(testPost.getId());
            System.out.println("   Post deleted: " + deleted);
            System.out.println();
        }

        System.out.println("=== All tests completed! ===");
    }
}
