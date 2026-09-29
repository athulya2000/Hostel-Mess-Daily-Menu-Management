package com.hostel.mess.database;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import com.hostel.mess.exception.MessDatabaseException;

/**
 * Singleton Database Manager supporting both MySQL (XAMPP) and SQLite.
 * 
 * Demonstrates:
 * - Singleton Design Pattern
 * - JDBC API for MySQL & SQLite
 * - Graceful fallback: If MySQL is selected but XAMPP is offline, falls back to SQLite.
 */
public class DatabaseManager {

    private static volatile DatabaseManager instance;
    private Connection connection;

    private String dbType = "mysql"; // Default to MySQL (XAMPP), falls back to sqlite
    private String mysqlUrl;
    private String mysqlUser;
    private String mysqlPassword;
    private static final String SQLITE_FILE = "hostel_mess.db";
    private static final String SQLITE_URL = "jdbc:sqlite:" + SQLITE_FILE;

    private DatabaseManager() throws MessDatabaseException {
        loadConfig();
        initDatabase();
    }

    private void loadConfig() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
                dbType = props.getProperty("db.type", "mysql").trim().toLowerCase();
                String host = props.getProperty("mysql.host", "localhost").trim();
                String port = props.getProperty("mysql.port", "3306").trim();
                String dbName = props.getProperty("mysql.database", "hostel_mess_db").trim();
                mysqlUser = props.getProperty("mysql.username", "root").trim();
                mysqlPassword = props.getProperty("mysql.password", "").trim();
                mysqlUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            } catch (Exception e) {
                System.err.println("Could not load db.properties, using defaults: " + e.getMessage());
                setDefaultMysqlUrl();
            }
        } else {
            setDefaultMysqlUrl();
        }
    }

    private void setDefaultMysqlUrl() {
        mysqlUrl = "jdbc:mysql://localhost:3306/hostel_mess_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        mysqlUser = "root";
        mysqlPassword = "";
    }

    public static synchronized DatabaseManager getInstance() throws MessDatabaseException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if ("mysql".equalsIgnoreCase(dbType)) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
                } catch (Exception e) {
                    System.err.println("Notice: Could not connect to MySQL/XAMPP (" + e.getMessage() + "). Falling back to SQLite.");
                    dbType = "sqlite";
                    return getSqliteConnection();
                }
            } else {
                return getSqliteConnection();
            }
        }
        return connection;
    }

    private Connection getSqliteConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }
        connection = DriverManager.getConnection(SQLITE_URL);
        return connection;
    }

    public String getActiveDatabaseType() {
        return "mysql".equalsIgnoreCase(dbType) ? "MySQL (XAMPP)" : "SQLite (Embedded)";
    }

    private void initDatabase() throws MessDatabaseException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            boolean isMySql = "mysql".equalsIgnoreCase(dbType);

            String autoInc = isMySql ? "INT AUTO_INCREMENT PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";

            // 1. Menu Items Table
            String createMenuTable = "CREATE TABLE IF NOT EXISTS menu_items ("
                    + "id " + autoInc + ", "
                    + "day_of_week VARCHAR(50) NOT NULL, "
                    + "meal_type VARCHAR(50) NOT NULL, "
                    + "item_name VARCHAR(255) NOT NULL, "
                    + "category VARCHAR(100) NOT NULL, "
                    + "description TEXT, "
                    + "extra_cost DOUBLE DEFAULT 0.0, "
                    + "is_available INT DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createMenuTable);

            // 2. Notices Table
            String createNoticesTable = "CREATE TABLE IF NOT EXISTS notices ("
                    + "id " + autoInc + ", "
                    + "title VARCHAR(255) NOT NULL, "
                    + "content TEXT NOT NULL, "
                    + "priority VARCHAR(50) DEFAULT 'Normal', "
                    + "is_active INT DEFAULT 1, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createNoticesTable);

            // 3. Feedback Table
            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedbacks ("
                    + "id " + autoInc + ", "
                    + "student_name VARCHAR(255) NOT NULL, "
                    + "room_number VARCHAR(50) NOT NULL, "
                    + "meal_type VARCHAR(50) NOT NULL, "
                    + "rating INT NOT NULL, "
                    + "comments TEXT, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createFeedbackTable);

            if (isTableEmpty(conn, "menu_items")) {
                seedInitialData(conn);
            }

        } catch (SQLException e) {
            throw new MessDatabaseException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private boolean isTableEmpty(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) AS total FROM " + tableName;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total") == 0;
            }
        } catch (SQLException ignored) {
        }
        return true;
    }

    private void seedInitialData(Connection conn) {
        String sql = "INSERT INTO menu_items (day_of_week, meal_type, item_name, category, description, extra_cost, is_available) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Object[][] sampleMenu = {
            // Monday
            {"Monday", "BREAKFAST", "Idli with Sambar & Coconut Chutney", "Vegetarian", "Steamed rice cakes served with hot lentil stew & fresh chutney. Filter Coffee / Tea.", 0.0, 1},
            {"Monday", "LUNCH", "Kerala Meals with Fish Curry & Thoran", "Non-Vegetarian", "Boiled Kerala Matta rice, Sardine/Mackerel curry, Cabbage thoran, Moru & Pappadam.", 0.0, 1},
            {"Monday", "LUNCH", "Veg Meals with Dal Curry & Avial", "Vegetarian", "Kerala rice, yellow dal tadka, traditional mixed vegetable avial, pickle & papad.", 0.0, 1},
            {"Monday", "SNACKS", "Parippu Vada & Hot Tea", "Vegetarian", "Crispy lentil fritters served with freshly brewed cardamom tea.", 0.0, 1},
            {"Monday", "DINNER", "Chapati with Veg Kurma & Salad", "Vegetarian", "Soft whole wheat chapatis served with mildly spiced rich vegetable kurma.", 0.0, 1},

            // Tuesday
            {"Tuesday", "BREAKFAST", "Poori Masala with Tea/Coffee", "Vegetarian", "Golden puffed whole wheat pooris with spiced potato onion bhaji.", 0.0, 1},
            {"Tuesday", "LUNCH", "South Indian Meals with Sambar & Rasam", "Vegetarian", "Rice, Drumstick sambar, Pepper rasam, Beetroot thoran, Curd & Pappad.", 0.0, 1},
            {"Tuesday", "SNACKS", "Banana Fritters (Pazham Pori) & Tea", "Vegetarian", "Ripe plantains dipped in golden batter and fried crisp.", 0.0, 1},
            {"Tuesday", "DINNER", "Egg Roast with Parotta / Chapati", "Non-Vegetarian", "Spicy Kerala style caramelized onion egg gravy with choice of parotta or chapati.", 0.0, 1},
            {"Tuesday", "DINNER", "Paneer Butter Masala with Chapati", "Vegetarian", "Cottage cheese cubes simmered in creamy buttery tomato gravy.", 0.0, 1},

            // Wednesday
            {"Wednesday", "BREAKFAST", "Appam with Vegetable Stew / Egg Roast", "Vegetarian", "Lacy fermented rice pancakes with coconut milk vegetable stew.", 0.0, 1},
            {"Wednesday", "LUNCH", "Chicken Biryani with Raita & Pickle", "Non-Vegetarian", "Aromatic spiced basmati rice layered with tender marinated chicken pieces.", 0.0, 1},
            {"Wednesday", "LUNCH", "Veg Biryani with Paneer & Raita", "Vegetarian", "Fragrant basmati rice tossed with fresh garden vegetables and paneer cubes.", 0.0, 1},
            {"Wednesday", "SNACKS", "Samosa with Mint Chutney & Tea", "Vegetarian", "Crispy pastry crust stuffed with spiced potato and green peas.", 0.0, 1},
            {"Wednesday", "DINNER", "Ghee Rice with Chicken Curry", "Non-Vegetarian", "Kaima rice cooked with pure ghee, garnished with fried onions & cashews.", 20.0, 1},
            {"Wednesday", "DINNER", "Ghee Rice with Mushroom Masala", "Vegetarian", "Fragrant ghee rice served with mushroom curry and salad.", 0.0, 1},

            // Thursday
            {"Thursday", "BREAKFAST", "Dosa with Tomato Chutney & Sambar", "Vegetarian", "Crisp fermented crepes with tangy tomato chutney and hot sambar.", 0.0, 1},
            {"Thursday", "LUNCH", "Curd Rice & Lemon Rice with Pickle", "Vegetarian", "Cooling tempered yoghurt rice, tangy lemon rice, and potato roast.", 0.0, 1},
            {"Thursday", "SNACKS", "Uzhunnu Vada (Medu Vada) & Tea", "Vegetarian", "Crisp exterior, soft interior black gram doughnuts with coconut chutney.", 0.0, 1},
            {"Thursday", "DINNER", "Idiyappam with Kadala Curry", "Vegetarian", "Steamed rice string hoppers with aromatic roasted coconut black chickpea curry.", 0.0, 1},

            // Friday
            {"Friday", "BREAKFAST", "Puttu with Kadala Curry & Pappadam", "Vegetarian", "Steamed cylinder rice flour cake layered with grated coconut.", 0.0, 1},
            {"Friday", "LUNCH", "Kerala Feast (Sadhya style) Meals", "Vegetarian", "Rice, Sambar, Pulissery, Olan, Kalan, Thoran, Achar, and Payasam.", 0.0, 1},
            {"Friday", "SNACKS", "Sukhiyan / Bonda & Black Tea", "Vegetarian", "Sweet green gram jaggery dumpling / spiced potato bonda.", 0.0, 1},
            {"Friday", "DINNER", "Fried Rice with Chili Chicken / Gobi", "Special Feast", "Indo-Chinese egg/veg fried rice served with chili gravy.", 25.0, 1},

            // Saturday
            {"Saturday", "BREAKFAST", "Upma with Coconut Chutney & Banana", "Vegetarian", "Roasted semolina cooked with mustard seeds, ginger, and curry leaves.", 0.0, 1},
            {"Saturday", "LUNCH", "Variety Rice (Tomato Rice & Curd Rice)", "Vegetarian", "Flavorful tomato rice with potato crisps, papad, and fresh curd.", 0.0, 1},
            {"Saturday", "SNACKS", "Biscuits & Filter Coffee", "Beverage", "Assorted bakery biscuits with hot brewed coffee.", 0.0, 1},
            {"Saturday", "DINNER", "Phulka with Mixed Veg Sabzi & Dal Tadka", "Vegetarian", "Light oil-free puffed rotis with homestyle dal and dry sabzi.", 0.0, 1},

            // Sunday
            {"Sunday", "BREAKFAST", "Masala Dosa with Chutney Trio & Sambar", "Vegetarian", "Golden crisp dosa stuffed with spiced mashed potato filling.", 0.0, 1},
            {"Sunday", "LUNCH", "Special Sunday Chicken Curry with Meals & Ice Cream", "Special Feast", "Sunday Grand Lunch: Spiced Kerala chicken curry, curd, and vanilla cup.", 0.0, 1},
            {"Sunday", "SNACKS", "Cake Slice & Tea", "Vegetarian", "Fresh vanilla sponge cake slice with evening tea.", 0.0, 1},
            {"Sunday", "DINNER", "Veg Pulao with Raita & Boiled Egg", "Vegetarian", "Mildly spiced fragrant rice with mixed veggies, raita, and boiled egg option.", 0.0, 1}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Object[] row : sampleMenu) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setString(3, (String) row[2]);
                pstmt.setString(4, (String) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setDouble(6, (Double) row[5]);
                pstmt.setInt(7, (Integer) row[6]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Warning: Error inserting seed menu: " + e.getMessage());
        }

        // Seed Sample Notice
        String noticeSql = "INSERT INTO notices (title, content, priority, is_active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement npstmt = conn.prepareStatement(noticeSql)) {
            npstmt.setString(1, "Welcome to Semester Mess Management");
            npstmt.setString(2, "Students are requested to register meal preferences before 8:00 AM daily. Sunday Special Biryani coupons available at mess counter.");
            npstmt.setString(3, "High");
            npstmt.setInt(4, 1);
            npstmt.executeUpdate();

            npstmt.setString(1, "Mess Timings Reminder");
            npstmt.setString(2, "Breakfast: 7:30 - 9:30 AM | Lunch: 12:30 - 2:30 PM | Evening Tea: 4:30 - 5:45 PM | Dinner: 7:45 - 9:30 PM. Please maintain punctuality.");
            npstmt.setString(3, "Normal");
            npstmt.setInt(4, 1);
            npstmt.executeUpdate();
        } catch (SQLException ignored) {
        }

        // Seed Sample Feedback
        String fbSql = "INSERT INTO feedbacks (student_name, room_number, meal_type, rating, comments) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement fpstmt = conn.prepareStatement(fbSql)) {
            fpstmt.setString(1, "Rahul Sharma");
            fpstmt.setString(2, "B-204");
            fpstmt.setString(3, "LUNCH");
            fpstmt.setInt(4, 5);
            fpstmt.setString(5, "Wednesday Biryani was exceptionally tasty! Keep it up.");
            fpstmt.executeUpdate();

            fpstmt.setString(1, "Anjali Menon");
            fpstmt.setString(2, "A-112");
            fpstmt.setString(3, "BREAKFAST");
            fpstmt.setInt(4, 4);
            fpstmt.setString(5, "Poori bhaji was warm and fresh. Chutney could be a little spicier.");
            fpstmt.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
