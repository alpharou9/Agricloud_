package esprit.shahed.services;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    // API Key from your newest screenshot (image_ef7cfa.png)
    private static final String API_KEY = "0ae2de9d20cb28d37de0ce4ae729f77e";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static JSONObject getWeather(double lat, double lon) {
        try {
            String urlString = String.format("%s?lat=%f&lon=%f&units=metric&appid=%s",
                    BASE_URL, lat, lon, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();
                return new JSONObject(response.toString());
            } else {
                System.out.println("Weather API Error: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("Weather Connection Failed: " + e.getMessage());
        }
        return null;
    }
}