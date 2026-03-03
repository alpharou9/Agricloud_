package esprit.rania.utilities;

public class Validator {

    // Validate if string is not empty
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    // Validate minimum length
    public static boolean hasMinLength(String text, int minLength) {
        return text != null && text.length() >= minLength;
    }

    // Validate maximum length
    public static boolean hasMaxLength(String text, int maxLength) {
        return text != null && text.length() <= maxLength;
    }

    // Validate email format
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // Validate if string contains only letters
    public static boolean isAlphabetic(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return text.matches("[a-zA-Z\\s]+");
    }

    // Validate if string contains only numbers
    public static boolean isNumeric(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return text.matches("\\d+");
    }

    // Validate if string is alphanumeric
    public static boolean isAlphanumeric(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return text.matches("[a-zA-Z0-9\\s]+");
    }

    // Validate post title
    public static ValidationResult validatePostTitle(String title) {
        if (!isNotEmpty(title)) {
            return new ValidationResult(false, "Title cannot be empty");
        }
        if (!hasMinLength(title, 3)) {
            return new ValidationResult(false, "Title must be at least 3 characters long");
        }
        if (!hasMaxLength(title, 200)) {
            return new ValidationResult(false, "Title cannot exceed 200 characters");
        }
        return new ValidationResult(true, "Valid");
    }

    // Validate post content
    public static ValidationResult validatePostContent(String content) {
        if (!isNotEmpty(content)) {
            return new ValidationResult(false, "Content cannot be empty");
        }
        if (!hasMinLength(content, 10)) {
            return new ValidationResult(false, "Content must be at least 10 characters long");
        }
        if (!hasMaxLength(content, 10000)) {
            return new ValidationResult(false, "Content cannot exceed 10,000 characters");
        }
        return new ValidationResult(true, "Valid");
    }

    // Validate comment content
    public static ValidationResult validateCommentContent(String content) {
        if (!isNotEmpty(content)) {
            return new ValidationResult(false, "Comment cannot be empty");
        }
        if (!hasMinLength(content, 1)) {
            return new ValidationResult(false, "Comment must be at least 1 character long");
        }
        if (!hasMaxLength(content, 1000)) {
            return new ValidationResult(false, "Comment cannot exceed 1,000 characters");
        }
        return new ValidationResult(true, "Valid");
    }

    // Validation result class
    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
