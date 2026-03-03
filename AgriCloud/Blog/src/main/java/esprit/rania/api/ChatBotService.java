package esprit.rania.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatBotService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    // This special model auto-picks any available free model for you
    private static final String MODEL = "openrouter/free";

    public static String chat(String userMessage, String blogContext) {
        try {
            String apiKey = System.getenv("OPENROUTER_API_KEY");

            System.out.println("[ChatBot] API Key present: " + (apiKey != null && !apiKey.isEmpty()));

            if (apiKey == null || apiKey.isEmpty()) {
                return "Error: OPENROUTER_API_KEY not set. Please set it and restart IntelliJ.";
            }

            String systemPrompt = "You are a helpful assistant for the AgriCloud blog. " +
                    "Help users understand, summarize, and explore blog posts. " +
                    "Be friendly and concise.\n\nCurrent blog posts:\n\n" + blogContext;

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.put(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            requestBody.put("messages", messages);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("HTTP-Referer", "https://agricloud.app");
            conn.setRequestProperty("X-Title", "AgriCloud Blog");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("[ChatBot] Response code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                System.out.println("[ChatBot] Raw response: " + response);

                JSONObject json = new JSONObject(response.toString());
                String result = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                System.out.println("[ChatBot] Success!");
                return result;

            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) err.append(line);
                    errorBody = err.toString();
                }
                System.err.println("[ChatBot] Error " + responseCode + ": " + errorBody);
                return "Sorry, could not get a response. Error " + responseCode + ": " + errorBody;
            }

        } catch (Exception e) {
            System.err.println("[ChatBot] Exception: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, something went wrong: " + e.getMessage();
        }
    }
}