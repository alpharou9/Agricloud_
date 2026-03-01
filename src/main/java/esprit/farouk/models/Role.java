package esprit.farouk.models;

import java.time.LocalDateTime;

public class Role {
    private long id;
    private String name;
    private String description;
    private String permissions; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Empty constructor
    public Role() {}

    // Minimal constructor
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Full constructor
    public Role(long id, String name, String description, String permissions,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name; // For ComboBox display
    }
}
