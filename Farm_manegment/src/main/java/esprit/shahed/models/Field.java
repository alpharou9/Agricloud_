package esprit.shahed.models;

public class Field {
    private int id, farmId;
    private String name, soilType, cropType, coordinates, status;
    private double area;

    // FIX: Assigned parameters to class fields so data isn't lost
    public Field(int id, int farmId, String name, double area, String soilType, String cropType, String coordinates, String status) {
        this.id = id;
        this.farmId = farmId;
        this.name = name;
        this.area = area;
        this.soilType = soilType;
        this.cropType = cropType;
        this.coordinates = coordinates;
        this.status = status;
    }

    public Field(int farmId, String name, double area, String soilType, String cropType, String coordinates, String status) {
        this.farmId = farmId;
        this.name = name;
        this.area = area;
        this.soilType = soilType;
        this.cropType = cropType;
        this.coordinates = coordinates;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFarmId() { return farmId; }
    public void setFarmId(int farmId) { this.farmId = farmId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }
    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }
    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}