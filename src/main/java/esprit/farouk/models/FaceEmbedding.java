package esprit.farouk.models;

import java.time.LocalDateTime;

/**
 * Represents a 128-dimensional face embedding vector.
 * Used for face recognition and comparison.
 */
public class FaceEmbedding {
    private float[] embedding; // 128D feature vector
    private LocalDateTime capturedAt;

    public FaceEmbedding() {
        this.capturedAt = LocalDateTime.now();
    }

    public FaceEmbedding(float[] embedding) {
        this.embedding = embedding;
        this.capturedAt = LocalDateTime.now();
    }

    public FaceEmbedding(float[] embedding, LocalDateTime capturedAt) {
        this.embedding = embedding;
        this.capturedAt = capturedAt;
    }

    /**
     * Calculates Euclidean distance to another face embedding.
     * Lower distance means more similar faces.
     * @param other The other face embedding to compare with
     * @return Distance value (0.0 = identical, higher = more different)
     */
    public double distanceTo(FaceEmbedding other) {
        if (this.embedding == null || other.embedding == null) {
            return Double.MAX_VALUE;
        }
        if (this.embedding.length != other.embedding.length) {
            throw new IllegalArgumentException("Embedding dimensions must match");
        }

        double sum = 0.0;
        for (int i = 0; i < this.embedding.length; i++) {
            double diff = this.embedding[i] - other.embedding[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    @Override
    public String toString() {
        return "FaceEmbedding{dimension=" + (embedding != null ? embedding.length : 0) +
               ", capturedAt=" + capturedAt + "}";
    }
}
