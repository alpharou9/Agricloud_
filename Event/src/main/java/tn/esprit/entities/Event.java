package tn.esprit.entities;
import java.sql.Timestamp;

public class Event {
    private int id;
    private String title, description, location;
    private Timestamp eventDate;
    private int slots;

    public Event(int id, String title, String description, String location, Timestamp eventDate, int slots) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.slots = slots;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Timestamp getEventDate() { return eventDate; }
    public int getSlots() { return slots; }

    @Override
    public String toString() { return title; } // Crucial for the Dropdown list
}