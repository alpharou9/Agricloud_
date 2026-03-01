package esprit.farouk.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotService {

    // ↓↓↓ PASTE YOUR CLAUDE API KEY HERE ↓↓↓
    private static final String API_KEY = "YOUR_CLAUDE_API_KEY_HERE";
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final int MAX_TOKENS = 1024;

    private static final String SYSTEM_PROMPT =
        "You are AgriBot, an intelligent farming assistant for AgriCloud, a smart farm management platform. " +
        "You help farmers, customers, and administrators with:\n" +
        "- Crop cultivation, planting schedules, and harvest timing\n" +
        "- Plant disease identification and treatment options\n" +
        "- Soil health, fertilization, and composting\n" +
        "- Irrigation strategies and water management\n" +
        "- Pest control and prevention\n" +
        "- Livestock and poultry management\n" +
        "- Market pricing strategies for farm products\n" +
        "- Organic and sustainable farming practices\n" +
        "- Weather impact on crops and seasonal planning\n" +
        "- Farm equipment usage and maintenance tips\n\n" +
        "Be concise, practical, and friendly. Give actionable advice. " +
        "If asked about something unrelated to farming or agriculture, politely redirect. " +
        "Respond in the same language the user writes in.";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public String sendMessage(List<Map<String, String>> conversationHistory) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("system", SYSTEM_PROMPT);
        requestBody.put("messages", conversationHistory);

        String requestJson = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JsonObject err = gson.fromJson(response.body(), JsonObject.class);
            String errMsg = err.has("error")
                    ? err.getAsJsonObject("error").get("message").getAsString()
                    : response.body();
            throw new Exception("API error " + response.statusCode() + ": " + errMsg);
        }

        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        JsonArray content = jsonResponse.getAsJsonArray("content");
        if (content != null && content.size() > 0) {
            return content.get(0).getAsJsonObject().get("text").getAsString();
        }
        throw new Exception("Empty response from API");
    }
}
