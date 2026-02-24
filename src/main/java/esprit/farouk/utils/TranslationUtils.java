package esprit.farouk.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TranslationUtils {

    private static final String API_URL = "https://api.mymemory.translated.net/get";
    // Max chars MyMemory accepts per free request
    private static final int MAX_CHARS = 500;

    public static String translate(String text, String langPair) throws Exception {
        if (text == null || text.isBlank()) return "";
        String textToSend = text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) + "..." : text;
        String encoded = URLEncoder.encode(textToSend, "UTF-8");
        String urlStr = API_URL + "?q=" + encoded + "&langpair=" + langPair;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "AgriCloud/1.0");

        int status = conn.getResponseCode();
        if (status != 200) throw new Exception("Translation API returned HTTP " + status);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
        return json.getAsJsonObject("responseData").get("translatedText").getAsString();
    }
}
