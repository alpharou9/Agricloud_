package esprit.rania.test;

import esprit.rania.models.Comment;
import esprit.rania.models.Post;
import esprit.rania.services.CommentService;
import esprit.rania.services.PostService;
import java.util.List;

/**
 * Simple test class to verify Comment CRUD operations
 * Run this class to test comment service methods
 */
public class CommentServiceTest {

    public static void main(String[] args) {
        CommentService commentService = new CommentService();
        PostService postService = new PostService();
        
        System.out.println("=== JavaBlog Comment Service Test ===\n");

        // First, get a post to comment on
        List<Post> posts = postService.getAllPosts();
        if (posts.isEmpty()) {
            System.out.println("No posts found. Please create a post first!");
            return;
        }
        
        Post testPost = posts.get(0);
        System.out.println("Using post: " + testPost.getTitle() + " (ID: " + testPost.getId() + ")\n");

        // Test 1: Create a new comment
        System.out.println("1. Testing CREATE operation...");
        Comment testComment = new Comment(
            "This is a test comment to verify the create operation.",
            "TestCommenter",
            testPost.getId(),
            1
        );
        
        boolean created = commentService.createComment(testComment);
        System.out.println("   Comment created: " + created);
        System.out.println("   Comment ID: " + testComment.getId());
        System.out.println();

        // Test 2: Read comments for post
        System.out.println("2. Testing READ BY POST ID operation...");
        List<Comment> comments = commentService.getCommentsByPostId(testPost.getId());
        System.out.println("   Total comments found: " + comments.size());
        for (Comment comment : comments) {
            System.out.println("   - " + comment.getAuthor() + ": " + 
                             comment.getContent().substring(0, Math.min(50, comment.getContent().length())) + "...");
        }
        System.out.println();

        // Test 3: Read specific comment
        if (testComment.getId() > 0) {
            System.out.println("3. Testing READ BY ID operation...");
            Comment retrievedComment = commentService.getCommentById(testComment.getId());
            if (retrievedComment != null) {
                System.out.println("   Retrieved comment by: " + retrievedComment.getAuthor());
                System.out.println("   Content: " + retrievedComment.getContent());
            }
            System.out.println();

            // Test 4: Update comment
            System.out.println("4. Testing UPDATE operation...");
            retrievedComment.setContent("This comment has been updated!");
            boolean updated = commentService.updateComment(retrievedComment);
            System.out.println("   Comment updated: " + updated);
            System.out.println();

            // Test 5: Get comment count
            System.out.println("5. Testing COMMENT COUNT operation...");
            int count = commentService.getCommentCountByPostId(testPost.getId());
            System.out.println("   Total comments for this post: " + count);
            System.out.println();

            // Test 6: Delete comment
            System.out.println("6. Testing DELETE operation...");
            boolean deleted = commentService.deleteComment(testComment.getId());
            System.out.println("   Comment deleted: " + deleted);
            System.out.println();
        }

        System.out.println("=== All tests completed! ===");
    }
}
