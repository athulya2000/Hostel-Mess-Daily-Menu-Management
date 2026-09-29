package com.hostel.mess.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.hostel.mess.database.DatabaseManager;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.Feedback;

public class FeedbackDAOImpl implements FeedbackDAO {

    private final DatabaseManager dbManager;

    public FeedbackDAOImpl() throws MessDatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }

    private Feedback mapRow(ResultSet rs) throws SQLException {
        return new Feedback(
            rs.getInt("id"),
            rs.getString("student_name"),
            rs.getString("room_number"),
            rs.getString("meal_type"),
            rs.getInt("rating"),
            rs.getString("comments"),
            rs.getTimestamp("created_at")
        );
    }

    @Override
    public boolean addFeedback(Feedback feedback) throws MessDatabaseException {
        String sql = "INSERT INTO feedbacks (student_name, room_number, meal_type, rating, comments) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feedback.getStudentName());
            pstmt.setString(2, feedback.getRoomNumber());
            pstmt.setString(3, feedback.getMealType());
            pstmt.setInt(4, feedback.getRating());
            pstmt.setString(5, feedback.getComments());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to save feedback: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Feedback> getAllFeedbacks() throws MessDatabaseException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedbacks ORDER BY id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to fetch feedbacks: " + e.getMessage(), e);
        }
    }

    @Override
    public double getAverageRating() throws MessDatabaseException {
        String sql = "SELECT AVG(rating) as avg_rating FROM feedbacks";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
            return 0.0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to calculate average rating: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFeedback(int id) throws MessDatabaseException {
        String sql = "DELETE FROM feedbacks WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to delete feedback: " + e.getMessage(), e);
        }
    }
}
