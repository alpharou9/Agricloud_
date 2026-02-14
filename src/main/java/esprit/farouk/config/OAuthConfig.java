package esprit.farouk.config;

public class OAuthConfig {

    // Google OAuth 2.0 Configuration
    // TODO: Replace with your own credentials from Google Cloud Console
    public static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = "YOUR_GOOGLE_CLIENT_SECRET";
    public static final String GOOGLE_REDIRECT_URI = "http://localhost:3000/oauth/callback";
    public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    public static final String GOOGLE_SCOPE = "email profile";

    // Facebook OAuth Configuration (for future)
    public static final String FACEBOOK_APP_ID = "YOUR_FACEBOOK_APP_ID";
    public static final String FACEBOOK_APP_SECRET = "YOUR_FACEBOOK_APP_SECRET";

    // Apple OAuth Configuration (for future)
    public static final String APPLE_CLIENT_ID = "YOUR_APPLE_CLIENT_ID";
    public static final String APPLE_TEAM_ID = "YOUR_APPLE_TEAM_ID";
}
