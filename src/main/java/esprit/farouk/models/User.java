package esprit.farouk.models;

import java.time.LocalDateTime;

public class User {
    private long id;
    private long roleId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String profilePicture;
    private String status; // active, inactive, blocked
    private LocalDateTime emailVerifiedAt;
    private String faceEmbeddings; // JSON array of face embeddings
    private LocalDateTime faceEnrolledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(long roleId, String name, String email, String password, String phone) {
        this.roleId = roleId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.status = "active";
    }

    public User(long id, long roleId, String name, String email, String password,
                String phone, String profilePicture, String status,
                LocalDateTime emailVerifiedAt, String faceEmbeddings, LocalDateTime faceEnrolledAt,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roleId = roleId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.profilePicture = profilePicture;
        this.status = status;
        this.emailVerifiedAt = emailVerifiedAt;
        this.faceEmbeddings = faceEmbeddings;
        this.faceEnrolledAt = faceEnrolledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public String getFaceEmbeddings() { return faceEmbeddings; }
    public void setFaceEmbeddings(String faceEmbeddings) { this.faceEmbeddings = faceEmbeddings; }

    public LocalDateTime getFaceEnrolledAt() { return faceEnrolledAt; }
    public void setFaceEnrolledAt(LocalDateTime faceEnrolledAt) { this.faceEnrolledAt = faceEnrolledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', status='" + status + "'}";
    }
}
