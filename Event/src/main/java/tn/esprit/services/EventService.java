package tn.esprit.services;
import tn.esprit.entities.Event;
import tn.esprit.utils.mydb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {
    private Connection cnx = mydb.getInstance().getConx();

    public void add(Event e) {
        String req = "INSERT INTO events (title, description, location, event_date, slots) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, e.getTitle());
            pst.setString(2, e.getDescription());
            pst.setString(3, e.getLocation());
            pst.setTimestamp(4, e.getEventDate());
            pst.setInt(5, e.getSlots());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List<Event> getAll() {
        List<Event> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM events");
            while (rs.next()) {
                list.add(new Event(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                        rs.getString("location"), rs.getTimestamp("event_date"), rs.getInt("slots")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    public void update(Event e) {
        String req = "UPDATE events SET title=?, description=?, location=?, event_date=?, slots=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, e.getTitle());
            pst.setString(2, e.getDescription());
            pst.setString(3, e.getLocation());
            pst.setTimestamp(4, e.getEventDate());
            pst.setInt(5, e.getSlots());
            pst.setInt(6, e.getId());
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void delete(int id) {
        try {
            PreparedStatement pst = cnx.prepareStatement("DELETE FROM events WHERE id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}