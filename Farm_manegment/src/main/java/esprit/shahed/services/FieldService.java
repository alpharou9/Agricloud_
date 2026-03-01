package esprit.shahed.services;

import esprit.shahed.database.DatabaseConnection;
import esprit.shahed.models.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FieldService {
    private final Connection connection = DatabaseConnection.getInstance().getConnection();

    public void addField(Field field) {
        String query = "INSERT INTO field (farm_id, name, area, soil_type, crop_type, coordinates, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, field.getFarmId());
            ps.setString(2, field.getName());
            ps.setDouble(3, field.getArea());
            ps.setString(4, field.getSoilType());
            ps.setString(5, field.getCropType());
            ps.setString(6, field.getCoordinates());
            ps.setString(7, field.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // FIX: Added this method to solve the "cannot find symbol" error
    public void updateField(Field field) {
        String query = "UPDATE field SET name=?, area=?, soil_type=?, crop_type=?, coordinates=?, status=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, field.getName());
            ps.setDouble(2, field.getArea());
            ps.setString(3, field.getSoilType());
            ps.setString(4, field.getCropType());
            ps.setString(5, field.getCoordinates());
            ps.setString(6, field.getStatus());
            ps.setInt(7, field.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteField(int id) {
        String query = "DELETE FROM field WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Field> getFieldsByFarm(int farmId) {
        List<Field> fields = new ArrayList<>();
        String query = "SELECT * FROM field WHERE farm_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, farmId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // FIX: Now using the corrected constructor in the model
                fields.add(new Field(
                        rs.getInt("id"), rs.getInt("farm_id"), rs.getString("name"),
                        rs.getDouble("area"), rs.getString("soil_type"),
                        rs.getString("crop_type"), rs.getString("coordinates"), rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return fields;
    }
}