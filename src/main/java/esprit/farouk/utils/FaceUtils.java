package esprit.farouk.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import esprit.farouk.models.FaceEmbedding;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility class for face embedding serialization and deserialization.
 * Handles conversion between FaceEmbedding objects and JSON storage format.
 */
public class FaceUtils {
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Converts list of FaceEmbedding objects to JSON string for database storage.
     *
     * Format: [{"embedding": "base64...", "capturedAt": "2026-02-14T10:30:00"}, ...]
     *
     * @param embeddings List of face embeddings
     * @return JSON string representation
     */
    public static String embeddingsToJson(List<FaceEmbedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return null;
        }

        JsonArray jsonArray = new JsonArray();
        for (FaceEmbedding embedding : embeddings) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("embedding", floatArrayToBase64(embedding.getEmbedding()));
            jsonObject.addProperty("capturedAt", embedding.getCapturedAt().format(formatter));
            jsonArray.add(jsonObject);
        }

        return gson.toJson(jsonArray);
    }

    /**
     * Parses JSON string from database into list of FaceEmbedding objects.
     *
     * @param json JSON string from database
     * @return List of FaceEmbedding objects
     */
    public static List<FaceEmbedding> embeddingsFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<FaceEmbedding> embeddings = new ArrayList<>();
        JsonArray jsonArray = gson.fromJson(json, JsonArray.class);

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            float[] embeddingArray = base64ToFloatArray(jsonObject.get("embedding").getAsString());
            LocalDateTime capturedAt = LocalDateTime.parse(
                    jsonObject.get("capturedAt").getAsString(),
                    formatter
            );
            embeddings.add(new FaceEmbedding(embeddingArray, capturedAt));
        }

        return embeddings;
    }

    /**
     * Encodes float array (128D embedding) to Base64 string.
     *
     * @param floatArray Face embedding vector
     * @return Base64 encoded string
     */
    public static String floatArrayToBase64(float[] floatArray) {
        if (floatArray == null) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(floatArray.length * 4); // 4 bytes per float
        for (float f : floatArray) {
            buffer.putFloat(f);
        }

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Decodes Base64 string back to float array (128D embedding).
     *
     * @param base64 Base64 encoded embedding
     * @return Float array (128D vector)
     */
    public static float[] base64ToFloatArray(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        float[] floatArray = new float[bytes.length / 4]; // 4 bytes per float
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = buffer.getFloat();
        }

        return floatArray;
    }
}
