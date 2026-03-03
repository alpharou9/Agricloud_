package esprit.shahed.services;

import esprit.shahed.database.DatabaseConnection;
import esprit.shahed.models.Farm;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmService {
    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    public void addFarm(Farm farm) {
        String sql = "INSERT INTO farm (name, location, latitude, longitude, area, farm_type, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, farm.getName());
            pst.setString(2, farm.getLocation());
            pst.setDouble(3, farm.getLatitude());
            pst.setDouble(4, farm.getLongitude());
            pst.setDouble(5, farm.getArea());
            pst.setString(6, farm.getFarmType());
            pst.setString(7, farm.getStatus()); // This will be "Pending" from the controller
            pst.setString(8, farm.getDescription());
            pst.executeUpdate();
            System.out.println("Farm added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding farm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Farm> getAllFarms() {
        List<Farm> farms = new ArrayList<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM farm")) {
            while (rs.next()) {
                Farm f = new Farm();
                f.setId(rs.getInt("id"));
                f.setName(rs.getString("name"));
                f.setLocation(rs.getString("location"));
                f.setLatitude(rs.getDouble("latitude"));
                f.setLongitude(rs.getDouble("longitude"));
                f.setArea(rs.getDouble("area"));
                f.setFarmType(rs.getString("farm_type"));
                f.setStatus(rs.getString("status"));
                f.setDescription(rs.getString("description"));
                farms.add(f);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return farms;
    }

    public void updateFarm(Farm farm) {
        String sql = "UPDATE farm SET name=?, location=?, latitude=?, longitude=?, area=?, farm_type=?, description=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, farm.getName());
            pst.setString(2, farm.getLocation());
            pst.setDouble(3, farm.getLatitude());
            pst.setDouble(4, farm.getLongitude());
            pst.setDouble(5, farm.getArea());
            pst.setString(6, farm.getFarmType());
            pst.setString(7, farm.getDescription());
            pst.setInt(8, farm.getId());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteFarm(int id) {
        try (PreparedStatement pst = connection.prepareStatement("DELETE FROM farm WHERE id = ?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}