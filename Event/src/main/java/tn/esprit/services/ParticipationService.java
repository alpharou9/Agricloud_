package tn.esprit.services;
import tn.esprit.entities.Participation;
import tn.esprit.utils.mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {
    private Connection cnx = mydb.getInstance().getConx();

    public void add(Participation p) {
        String req = "INSERT INTO participations (event_id, full_name, email, phone, status) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, p.getEventId());
            pst.setString(2, p.getFullName());
            pst.setString(3, p.getEmail());
            pst.setString(4, p.getPhone());
            pst.setString(5, p.getStatus());
            pst.executeUpdate();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
    }

    public List<Participation> getByEvent(int eventId) {
        List<Participation> list = new ArrayList<>();
        // SQL JOIN to get participation details + the event title from the events table
        String req = "SELECT p.*, e.title AS event_title FROM participations p " +
                "JOIN events e ON p.event_id = e.id " +
                "WHERE p.event_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, eventId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Participation p = new Participation(
                        rs.getInt("id"),
                        rs.getInt("event_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("status")
                );
                // This fills the helper field we added to the entity for the TableView
                p.setEventTitle(rs.getString("event_title"));
                list.add(p);
            }
        } catch (SQLException ex) {
            System.out.println("Error fetching participations: " + ex.getMessage());
        }
        return list;
    }
    public void update(Participation p) {
        String req = "UPDATE participations SET full_name=?, email=?, phone=?, status=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, p.getFullName());
            pst.setString(2, p.getEmail());
            pst.setString(3, p.getPhone());
            pst.setString(4, p.getStatus());
            pst.setInt(5, p.getId());
            pst.executeUpdate();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
    }

    public void delete(int id) {
        try {
            PreparedStatement pst = cnx.prepareStatement("DELETE FROM participations WHERE id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException ex) { System.out.println(ex.getMessage()); }
    }
}