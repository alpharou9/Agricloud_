package esprit.shahed.models;

import java.sql.Timestamp;

public class Farm {
    private int id;
    private String name, location, farmType, status, description;
    private double latitude, longitude, area;
    private Timestamp createdAt, updatedAt;

    public Farm() {}

    public Farm(String name, String location, double latitude, double longitude, double area, String farmType, String status, String description) {
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.farmType = farmType;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public String getFarmType() { return farmType; }
    public void setFarmType(String farmType) { this.farmType = farmType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}