package esprit.farouk.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import esprit.farouk.config.OAuthConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FacebookOAuthService {

    private final Gson gson = new Gson();

    // Facebook OAuth URLs
    private static final String FACEBOOK_AUTH_URL = "https://www.facebook.com/v18.0/dialog/oauth";
    private static final String FACEBOOK_TOKEN_URL = "https://graph.facebook.com/v18.0/oauth/access_token";
    private static final String FACEBOOK_USER_INFO_URL = "https://graph.facebook.com/me";
    private static final String FACEBOOK_SCOPE = "email,public_profile";

    /**
     * Generates the Facebook OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        try {
            String params = "client_id=" + URLEncoder.encode(OAuthConfig.FACEBOOK_APP_ID, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(OAuthConfig.GOOGLE_REDIRECT_URI, "UTF-8") +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(FACEBOOK_SCOPE, "UTF-8");

            return FACEBOOK_AUTH_URL + "?" + params;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Exchanges authorization code for access token
     */
    public String getAccessToken(String authorizationCode) {
        try {
            String urlString = FACEBOOK_TOKEN_URL +
                    "?client_id=" + URLEncoder.encode(OAuthConfig.FACEBOOK_APP_ID, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(OAuthConfig.FACEBOOK_APP_SECRET, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(OAuthConfig.GOOGLE_REDIRECT_URI, "UTF-8") +
                    "&code=" + URLEncoder.encode(authorizationCode, "UTF-8");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse.get("access_token").getAsString();
            } else {
                // Read error response
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                System.err.println("Failed to get access token. Response code: " + responseCode);
                System.err.println("Error: " + errorResponse.toString());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets user information from Facebook using access token
     */
    public Map<String, String> getUserInfo(String accessToken) {
        try {
            String urlString = FACEBOOK_USER_INFO_URL +
                    "?fields=id,name,email,picture.type(large)" +
                    "&access_token=" + URLEncoder.encode(accessToken, "UTF-8");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("id", jsonResponse.get("id").getAsString());
                userInfo.put("name", jsonResponse.get("name").getAsString());

                // Email is optional in Facebook
                if (jsonResponse.has("email")) {
                    userInfo.put("email", jsonResponse.get("email").getAsString());
                } else {
                    userInfo.put("email", jsonResponse.get("id").getAsString() + "@facebook.com");
                }

                // Profile picture
                if (jsonResponse.has("picture")) {
                    JsonObject picture = jsonResponse.getAsJsonObject("picture");
                    JsonObject pictureData = picture.getAsJsonObject("data");
                    userInfo.put("picture", pictureData.get("url").getAsString());
                }

                System.out.println("✓ Facebook user info retrieved: " + userInfo.get("email"));
                return userInfo;
            } else {
                System.err.println("Failed to get user info. Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
