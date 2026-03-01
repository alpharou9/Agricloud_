package esprit.farouk.models;

import java.time.LocalDateTime;

/**
 * Represents a 128-dimensional face embedding vector.
 * Face embeddings are mathematical representations of facial features
 * that can be compared to determine if two faces belong to the same person.
 */
public class FaceEmbedding {
    private float[] embedding; // 128D feature vector
    private LocalDateTime capturedAt;

    public FaceEmbedding() {
    }

    public FaceEmbedding(float[] embedding, LocalDateTime capturedAt) {
        this.embedding = embedding;
        this.capturedAt = capturedAt;
    }

    /**
     * Calculates Euclidean distance between this embedding and another.
     * Lower distance means more similar faces.
     * Typical threshold for same person: < 0.6
     *
     * @param other The embedding to compare with
     * @return Euclidean distance between embeddings
     */
    public double distanceTo(FaceEmbedding other) {
        if (this.embedding == null || other.embedding == null) {
            return Double.MAX_VALUE;
        }

        if (this.embedding.length != other.embedding.length) {
            throw new IllegalArgumentException("Embeddings must have same dimension");
        }

        double sum = 0.0;
        for (int i = 0; i < this.embedding.length; i++) {
            double diff = this.embedding[i] - other.embedding[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    // Getters and Setters
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
        return "FaceEmbedding{" +
                "dimension=" + (embedding != null ? embedding.length : 0) +
                ", capturedAt=" + capturedAt +
                '}';
    }
}
