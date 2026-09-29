package com.hostel.mess.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.hostel.mess.database.DatabaseManager;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;

/**
 * JDBC Implementation of MenuDAO.
 * 
 * Demonstrates:
 * - Interface Implementation
 * - JDBC PreparedStatements and SQL CRUD execution
 * - Exception Handling (Checked Exception handling & wrapping)
 * - Delegation of database connections via Singleton DatabaseManager
 */
public class MenuDAOImpl implements MenuDAO {

    private final DatabaseManager dbManager;

    public MenuDAOImpl() throws MessDatabaseException {
        this.dbManager = DatabaseManager.getInstance();
    }

    // Helper method to extract MenuItem from ResultSet
    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String day = rs.getString("day_of_week");
        MealType mealType = MealType.fromString(rs.getString("meal_type"));
        String name = rs.getString("item_name");
        String cat = rs.getString("category");
        String desc = rs.getString("description");
        double extraCost = rs.getDouble("extra_cost");
        boolean available = rs.getInt("is_available") == 1;
        Timestamp createdAt = rs.getTimestamp("created_at");

        return new MenuItem(id, day, mealType, name, cat, desc, extraCost, available, createdAt);
    }

    @Override
    public boolean addMenuItem(MenuItem item) throws MessDatabaseException {
        String sql = "INSERT INTO menu_items (day_of_week, meal_type, item_name, category, description, extra_cost, is_available) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, item.getDayOfWeek());
            pstmt.setString(2, item.getMealType().name());
            pstmt.setString(3, item.getItemName());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getDescription());
            pstmt.setDouble(6, item.getExtraCost());
            pstmt.setInt(7, item.isAvailable() ? 1 : 0);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to add menu item: " + e.getMessage(), e);
        }
    }

    @Override
    public MenuItem getMenuItemById(int id) throws MessDatabaseException {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMenuItem(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to retrieve menu item by ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<MenuItem> getAllMenuItems() throws MessDatabaseException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items ORDER BY "
                   + "CASE day_of_week "
                   + "  WHEN 'Monday' THEN 1 "
                   + "  WHEN 'Tuesday' THEN 2 "
                   + "  WHEN 'Wednesday' THEN 3 "
                   + "  WHEN 'Thursday' THEN 4 "
                   + "  WHEN 'Friday' THEN 5 "
                   + "  WHEN 'Saturday' THEN 6 "
                   + "  WHEN 'Sunday' THEN 7 "
                   + "  ELSE 8 END, "
                   + "CASE meal_type "
                   + "  WHEN 'BREAKFAST' THEN 1 "
                   + "  WHEN 'LUNCH' THEN 2 "
                   + "  WHEN 'SNACKS' THEN 3 "
                   + "  WHEN 'DINNER' THEN 4 "
                   + "  ELSE 5 END, id ASC";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToMenuItem(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to fetch all menu items: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MenuItem> getMenuItemsByDay(String dayOfWeek) throws MessDatabaseException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE day_of_week = ? ORDER BY "
                   + "CASE meal_type "
                   + "  WHEN 'BREAKFAST' THEN 1 "
                   + "  WHEN 'LUNCH' THEN 2 "
                   + "  WHEN 'SNACKS' THEN 3 "
                   + "  WHEN 'DINNER' THEN 4 "
                   + "  ELSE 5 END, id ASC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dayOfWeek);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMenuItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to fetch menu items for day " + dayOfWeek + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<MenuItem> getMenuItems(String dayOfWeek, MealType mealType) throws MessDatabaseException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE day_of_week = ? AND meal_type = ? ORDER BY id ASC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dayOfWeek);
            pstmt.setString(2, mealType.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMenuItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to fetch menu items for day " + dayOfWeek + " & meal " + mealType + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<MenuItem> searchMenuItems(String keyword) throws MessDatabaseException {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE item_name LIKE ? OR category LIKE ? OR description LIKE ? OR day_of_week LIKE ? ORDER BY id ASC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            pstmt.setString(4, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToMenuItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to search menu items: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateMenuItem(MenuItem item) throws MessDatabaseException {
        String sql = "UPDATE menu_items SET day_of_week = ?, meal_type = ?, item_name = ?, category = ?, description = ?, extra_cost = ?, is_available = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getDayOfWeek());
            pstmt.setString(2, item.getMealType().name());
            pstmt.setString(3, item.getItemName());
            pstmt.setString(4, item.getCategory());
            pstmt.setString(5, item.getDescription());
            pstmt.setDouble(6, item.getExtraCost());
            pstmt.setInt(7, item.isAvailable() ? 1 : 0);
            pstmt.setInt(8, item.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to update menu item with ID " + item.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteMenuItem(int id) throws MessDatabaseException {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to delete menu item with ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalItemsCount() throws MessDatabaseException {
        String sql = "SELECT COUNT(*) AS total FROM menu_items";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to count menu items: " + e.getMessage(), e);
        }
    }
}
