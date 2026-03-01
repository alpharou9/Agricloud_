package esprit.farouk.utils;

import esprit.farouk.models.User;

public class SessionManager {
    private static User currentUser = null;

    /**
     * Sets the currently logged-in user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("✓ User session started: " + user.getName());
    }

    /**
     * Gets the currently logged-in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if current user is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && "Admin".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is farmer
     */
    public static boolean isFarmer() {
        return currentUser != null && "Farmer".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is customer
     */
    public static boolean isCustomer() {
        return currentUser != null && "Customer".equals(currentUser.getRoleName());
    }

    /**
     * Checks if current user is guest
     */
    public static boolean isGuest() {
        return currentUser != null && "Guest".equals(currentUser.getRoleName());
    }

    /**
     * Logs out the current user
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("✓ User logged out: " + currentUser.getName());
            currentUser = null;
        }
    }
}
