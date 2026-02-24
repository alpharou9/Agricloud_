package esprit.farouk.services;

import esprit.farouk.database.DatabaseConnection;
import esprit.farouk.models.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private final Connection connection;

    public EventService() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void add(Event event) throws SQLException {
        String sql = "INSERT INTO events (user_id, title, slug, description, event_date, end_date, " +
                     "location, latitude, longitude, capacity, image, category, status, registration_deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, event.getUserId());
        ps.setString(2, event.getTitle());
        ps.setString(3, event.getSlug());
        ps.setString(4, event.getDescription());
        ps.setTimestamp(5, Timestamp.valueOf(event.getEventDate()));
        if (event.getEndDate() != null) ps.setTimestamp(6, Timestamp.valueOf(event.getEndDate())); else ps.setNull(6, Types.TIMESTAMP);
        ps.setString(7, event.getLocation());
        if (event.getLatitude() != null) ps.setDouble(8, event.getLatitude()); else ps.setNull(8, Types.DOUBLE);
        if (event.getLongitude() != null) ps.setDouble(9, event.getLongitude()); else ps.setNull(9, Types.DOUBLE);
        if (event.getCapacity() != null) ps.setInt(10, event.getCapacity()); else ps.setNull(10, Types.INTEGER);
        ps.setString(11, event.getImage());
        ps.setString(12, event.getCategory());
        ps.setString(13, event.getStatus() != null ? event.getStatus() : "upcoming");
        if (event.getRegistrationDeadline() != null) ps.setTimestamp(14, Timestamp.valueOf(event.getRegistrationDeadline())); else ps.setNull(14, Types.TIMESTAMP);
        ps.executeUpdate();
    }

    public void update(Event event) throws SQLException {
        String sql = "UPDATE events SET user_id = ?, title = ?, slug = ?, description = ?, event_date = ?, " +
                     "end_date = ?, location = ?, latitude = ?, longitude = ?, capacity = ?, image = ?, " +
                     "category = ?, status = ?, registration_deadline = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, event.getUserId());
        ps.setString(2, event.getTitle());
        ps.setString(3, event.getSlug());
        ps.setString(4, event.getDescription());
        ps.setTimestamp(5, Timestamp.valueOf(event.getEventDate()));
        if (event.getEndDate() != null) ps.setTimestamp(6, Timestamp.valueOf(event.getEndDate())); else ps.setNull(6, Types.TIMESTAMP);
        ps.setString(7, event.getLocation());
        if (event.getLatitude() != null) ps.setDouble(8, event.getLatitude()); else ps.setNull(8, Types.DOUBLE);
        if (event.getLongitude() != null) ps.setDouble(9, event.getLongitude()); else ps.setNull(9, Types.DOUBLE);
        if (event.getCapacity() != null) ps.setInt(10, event.getCapacity()); else ps.setNull(10, Types.INTEGER);
        ps.setString(11, event.getImage());
        ps.setString(12, event.getCategory());
        ps.setString(13, event.getStatus());
        if (event.getRegistrationDeadline() != null) ps.setTimestamp(14, Timestamp.valueOf(event.getRegistrationDeadline())); else ps.setNull(14, Types.TIMESTAMP);
        ps.setLong(15, event.getId());
        ps.executeUpdate();
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    public List<Event> getAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, u.name as user_name, " +
                     "(SELECT COUNT(*) FROM participations p WHERE p.event_id = e.id AND p.status != 'cancelled') as participant_count " +
                     "FROM events e LEFT JOIN users u ON e.user_id = u.id ORDER BY e.event_date DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Event event = mapRow(rs);
            event.setUserName(rs.getString("user_name"));
            event.setParticipantCount(rs.getInt("participant_count"));
            events.add(event);
        }
        return events;
    }

    public Event getById(long id) throws SQLException {
        String sql = "SELECT e.*, u.name as user_name, " +
                     "(SELECT COUNT(*) FROM participations p WHERE p.event_id = e.id AND p.status != 'cancelled') as participant_count " +
                     "FROM events e LEFT JOIN users u ON e.user_id = u.id WHERE e.id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Event event = mapRow(rs);
            event.setUserName(rs.getString("user_name"));
            event.setParticipantCount(rs.getInt("participant_count"));
            return event;
        }
        return null;
    }

    public List<Event> getByUserId(long userId) throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, u.name as user_name, " +
                     "(SELECT COUNT(*) FROM participations p WHERE p.event_id = e.id AND p.status != 'cancelled') as participant_count " +
                     "FROM events e LEFT JOIN users u ON e.user_id = u.id WHERE e.user_id = ? ORDER BY e.event_date DESC";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Event event = mapRow(rs);
            event.setUserName(rs.getString("user_name"));
            event.setParticipantCount(rs.getInt("participant_count"));
            events.add(event);
        }
        return events;
    }

    public List<Event> getUpcoming() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, u.name as user_name, " +
                     "(SELECT COUNT(*) FROM participations p WHERE p.event_id = e.id AND p.status != 'cancelled') as participant_count " +
                     "FROM events e LEFT JOIN users u ON e.user_id = u.id " +
                     "WHERE e.status IN ('upcoming', 'ongoing') ORDER BY e.event_date ASC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Event event = mapRow(rs);
            event.setUserName(rs.getString("user_name"));
            event.setParticipantCount(rs.getInt("participant_count"));
            events.add(event);
        }
        return events;
    }

    public void cancel(long id) throws SQLException {
        String sql = "UPDATE events SET status = 'cancelled' WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getLong("id"));
        event.setUserId(rs.getLong("user_id"));
        event.setTitle(rs.getString("title"));
        event.setSlug(rs.getString("slug"));
        event.setDescription(rs.getString("description"));
        Timestamp eventDate = rs.getTimestamp("event_date");
        if (eventDate != null) event.setEventDate(eventDate.toLocalDateTime());
        Timestamp endDate = rs.getTimestamp("end_date");
        if (endDate != null) event.setEndDate(endDate.toLocalDateTime());
        event.setLocation(rs.getString("location"));
        double lat = rs.getDouble("latitude");
        if (!rs.wasNull()) event.setLatitude(lat);
        double lng = rs.getDouble("longitude");
        if (!rs.wasNull()) event.setLongitude(lng);
        int capacity = rs.getInt("capacity");
        if (!rs.wasNull()) event.setCapacity(capacity);
        event.setImage(rs.getString("image"));
        event.setCategory(rs.getString("category"));
        event.setStatus(rs.getString("status"));
        Timestamp regDeadline = rs.getTimestamp("registration_deadline");
        if (regDeadline != null) event.setRegistrationDeadline(regDeadline.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) event.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) event.setUpdatedAt(updatedAt.toLocalDateTime());
        return event;
    }
}
