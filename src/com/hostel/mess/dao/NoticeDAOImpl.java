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
import com.hostel.mess.model.Notice;

public class NoticeDAOImpl implements NoticeDAO {

    private final DatabaseManager dbManager;

    public NoticeDAOImpl() throws MessDatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }

    private Notice mapRow(ResultSet rs) throws SQLException {
        return new Notice(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("content"),
            rs.getString("priority"),
            rs.getInt("is_active") == 1,
            rs.getTimestamp("created_at")
        );
    }

    @Override
    public boolean addNotice(Notice notice) throws MessDatabaseException {
        String sql = "INSERT INTO notices (title, content, priority, is_active) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, notice.getTitle());
            pstmt.setString(2, notice.getContent());
            pstmt.setString(3, notice.getPriority());
            pstmt.setInt(4, notice.isActive() ? 1 : 0);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to add notice: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notice> getAllNotices() throws MessDatabaseException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT * FROM notices ORDER BY id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to load notices: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Notice> getActiveNotices() throws MessDatabaseException {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT * FROM notices WHERE is_active = 1 ORDER BY id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to load active notices: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteNotice(int id) throws MessDatabaseException {
        String sql = "DELETE FROM notices WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to delete notice: " + e.getMessage(), e);
        }
    }
}
