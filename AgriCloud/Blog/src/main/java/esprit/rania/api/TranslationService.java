package esprit.rania.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class TranslationService {

    private static final String API_URL = "https://api.mymemory.translated.net/get";

    /**
     * Translate text to target language using MyMemory (free, no key needed)
     * @param text Text to translate
     * @param targetLang Target language code (e.g., "fr", "es", "ar")
     * @return Translated text
     */
    public static String translate(String text, String targetLang) {
        try {
            System.out.println("🌍 Translating to: " + targetLang);

            String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = API_URL + "?q=" + encodedText + "&langpair=en|" + targetLang;

            System.out.println("🔗 Calling: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            System.out.println("📡 Response code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);

                System.out.println("📦 Raw response: " + response);

                JSONObject json = new JSONObject(response.toString());
                String translated = json.getJSONObject("responseData").getString("translatedText");

                System.out.println("✅ Translation successful: " + translated);
                return translated;

            } else {
                System.err.println("❌ Translation failed with code: " + responseCode);
                // ✅ Null check to avoid NullPointerException
                if (conn.getErrorStream() != null) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) err.append(line);
                    System.err.println("❌ Error details: " + err);
                } else {
                    System.err.println("❌ No error stream — possible network issue");
                }
                return text;
            }

        } catch (Exception e) {
            System.err.println("❌ Translation error: " + e.getMessage());
            e.printStackTrace();
            return text;
        }
    }

    /**
     * Get language name from code
     */
    public static String getLanguageName(String code) {
        switch (code) {
            case "fr": return "French";
            case "es": return "Spanish";
            case "ar": return "Arabic";
            case "de": return "German";
            case "it": return "Italian";
            case "pt": return "Portuguese";
            case "ru": return "Russian";
            case "zh": return "Chinese";
            case "ja": return "Japanese";
            case "hi": return "Hindi";
            default: return "English";
        }
    }
}