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

public class GoogleOAuthService {

    private final Gson gson = new Gson();

    /**
     * Generates the Google OAuth authorization URL
     */
    public String getAuthorizationUrl() {
        try {
            String params = "client_id=" + URLEncoder.encode(OAuthConfig.GOOGLE_CLIENT_ID, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(OAuthConfig.GOOGLE_REDIRECT_URI, "UTF-8") +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(OAuthConfig.GOOGLE_SCOPE, "UTF-8") +
                    "&access_type=offline" +
                    "&prompt=consent";

            return OAuthConfig.GOOGLE_AUTH_URL + "?" + params;
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
            URL url = new URL(OAuthConfig.GOOGLE_TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "code=" + URLEncoder.encode(authorizationCode, "UTF-8") +
                    "&client_id=" + URLEncoder.encode(OAuthConfig.GOOGLE_CLIENT_ID, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(OAuthConfig.GOOGLE_CLIENT_SECRET, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(OAuthConfig.GOOGLE_REDIRECT_URI, "UTF-8") +
                    "&grant_type=authorization_code";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

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
                System.err.println("Failed to get access token. Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets user information from Google using access token
     */
    public Map<String, String> getUserInfo(String accessToken) {
        try {
            URL url = new URL(OAuthConfig.GOOGLE_USER_INFO_URL + "?access_token=" + accessToken);
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
                userInfo.put("email", jsonResponse.get("email").getAsString());
                userInfo.put("name", jsonResponse.get("name").getAsString());

                if (jsonResponse.has("picture")) {
                    userInfo.put("picture", jsonResponse.get("picture").getAsString());
                }

                System.out.println("✓ Google user info retrieved: " + userInfo.get("email"));
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
