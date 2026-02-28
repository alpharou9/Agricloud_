package tn.esprit.entities;
public class Participation {
    private int id, eventId;
    private String fullName, email, phone, status, eventTitle; // Added eventTitle

    public Participation(int id, int eventId, String fullName, String email, String phone, String status) {
        this.id = id;
        this.eventId = eventId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    // Getters and Setters including getEventTitle and setEventTitle
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getId() { return id; }
    public int getEventId() { return eventId; }
}