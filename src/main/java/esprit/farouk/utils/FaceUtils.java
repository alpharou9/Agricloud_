package esprit.farouk.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import esprit.farouk.models.FaceEmbedding;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Utility class for face embedding serialization and deserialization.
 */
public class FaceUtils {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    /**
     * Converts a list of FaceEmbedding objects to JSON string for database storage.
     * @param embeddings List of face embeddings
     * @return JSON string representation
     */
    public static String embeddingsToJson(List<FaceEmbedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return null;
        }

        List<EmbeddingData> dataList = new ArrayList<>();
        for (FaceEmbedding emb : embeddings) {
            EmbeddingData data = new EmbeddingData();
            data.embedding = floatArrayToBase64(emb.getEmbedding());
            data.capturedAt = emb.getCapturedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            dataList.add(data);
        }

        return gson.toJson(dataList);
    }

    /**
     * Parses JSON string from database to list of FaceEmbedding objects.
     * @param json JSON string from database
     * @return List of face embeddings
     */
    public static List<FaceEmbedding> embeddingsFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<EmbeddingData>>(){}.getType();
        List<EmbeddingData> dataList = gson.fromJson(json, listType);

        List<FaceEmbedding> embeddings = new ArrayList<>();
        for (EmbeddingData data : dataList) {
            float[] embArray = base64ToFloatArray(data.embedding);
            LocalDateTime capturedAt = LocalDateTime.parse(data.capturedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            embeddings.add(new FaceEmbedding(embArray, capturedAt));
        }

        return embeddings;
    }

    /**
     * Encodes a float array to base64 string.
     * @param array Float array to encode
     * @return Base64 encoded string
     */
    public static String floatArrayToBase64(float[] array) {
        if (array == null) return null;

        byte[] bytes = new byte[array.length * 4]; // 4 bytes per float
        for (int i = 0; i < array.length; i++) {
            int intBits = Float.floatToIntBits(array[i]);
            bytes[i * 4] = (byte) (intBits >> 24);
            bytes[i * 4 + 1] = (byte) (intBits >> 16);
            bytes[i * 4 + 2] = (byte) (intBits >> 8);
            bytes[i * 4 + 3] = (byte) intBits;
        }

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodes base64 string to float array.
     * @param base64 Base64 encoded string
     * @return Float array
     */
    public static float[] base64ToFloatArray(String base64) {
        if (base64 == null) return null;

        byte[] bytes = Base64.getDecoder().decode(base64);
        float[] array = new float[bytes.length / 4];

        for (int i = 0; i < array.length; i++) {
            int intBits = ((bytes[i * 4] & 0xFF) << 24) |
                          ((bytes[i * 4 + 1] & 0xFF) << 16) |
                          ((bytes[i * 4 + 2] & 0xFF) << 8) |
                          (bytes[i * 4 + 3] & 0xFF);
            array[i] = Float.intBitsToFloat(intBits);
        }

        return array;
    }

    /**
     * Data structure for JSON serialization.
     */
    private static class EmbeddingData {
        String embedding; // base64 encoded float array
        String capturedAt; // ISO format datetime
    }

    /**
     * GSON adapter for LocalDateTime serialization.
     */
    private static class LocalDateTimeAdapter implements com.google.gson.JsonSerializer<LocalDateTime>,
            com.google.gson.JsonDeserializer<LocalDateTime> {
        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, Type typeOfSrc,
                                                      com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, Type typeOfT,
                                          com.google.gson.JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
