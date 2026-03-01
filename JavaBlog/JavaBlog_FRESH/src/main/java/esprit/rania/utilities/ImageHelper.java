package esprit.rania.utilities;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageHelper {
    
    private static final String UPLOADS_DIR = "uploads/post_images/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    static {
        // Create uploads directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOADS_DIR));
        } catch (IOException e) {
            System.err.println("Could not create uploads directory: " + e.getMessage());
        }
    }
    
    /**
     * Open file chooser for image selection
     */
    public static File chooseImage(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Post Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        return fileChooser.showOpenDialog(ownerWindow);
    }
    
    /**
     * Save uploaded image and return relative path
     */
    public static String saveImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        // Check file size
        if (sourceFile.length() > MAX_FILE_SIZE) {
            throw new IOException("Image file is too large. Maximum size is 5MB.");
        }
        
        // Generate unique filename
        String extension = getFileExtension(sourceFile.getName());
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        // Copy file to uploads directory
        Path targetPath = Paths.get(UPLOADS_DIR + uniqueFileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return UPLOADS_DIR + uniqueFileName;
    }
    
    /**
     * Delete image file
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(imagePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Could not delete image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if image file exists
     */
    public static boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(imagePath));
    }
    
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return "";
    }
}
