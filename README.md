# 🍽 Hostel Mess Daily Menu Register

> **B.Tech Computer Science & Engineering — S3 Java Subject Project**  
> Developed using **Java 17**, **Java Swing & AWT**, **JDBC**, **MySQL (XAMPP) & SQLite**, and **HTML5/CSS3**.

---

## 📌 Project Overview

**Hostel Mess Daily Menu Register** is a digital menu management system designed for college hostels. It provides a real-time, interactive solution for mess daily operations:
1. **Client Panel (Resident & Student Daily Board)**: Displays the daily meal schedule (Breakfast, Lunch, Evening Tea & Snacks, Dinner) categorized into Vegetarian, Non-Vegetarian, and Special Feast, with extra pricing and live dish search.
2. **Mess Admin Panel**: Allows authorized mess wardens to manage the daily menu with complete **CRUD operations** (Create, Read, Update, Delete), dedicated Edit/Update controls, dynamic table **pagination**, and auto-dismissing confirmation banners.

---

## 🎯 Syllabus & Evaluation Requirements Checklist

This project strictly adheres to the **B.Tech S3 Java Object-Oriented Programming** guidelines:

| Syllabus Requirement | Implementation in Project | Source Code Location |
| :--- | :--- | :--- |
| **Object-Oriented Programming (OOP)** | Inheritance (`BaseEntity` base class), Encapsulation (private fields, getters/setters), Abstraction (Interfaces & abstract classes), Polymorphism (overloaded queries, interface implementations). | `src/com/hostel/mess/model/` |
| **Packages & CLASSPATH** | Clean modular packages: `model`, `dao`, `database`, `exception`, `view`, `web`, `util`. | `src/com/hostel/mess/` |
| **Design Patterns** | **Singleton Pattern** for database connection manager (`DatabaseManager`); **DAO Pattern** for data persistence abstraction (`MenuDAO`). | `DatabaseManager.java`, `MenuDAO.java` |
| **Exception Handling** | Custom checked exceptions (`MessDatabaseException`, `ValidationException`), `try-catch-finally`, try-with-resources. | `src/com/hostel/mess/exception/` |
| **AWT & Swing GUI** | `JFrame`, `JTable`, `JScrollPane`, `JTabbedPane`, `JSplitPane`, `JLabel`, `JButton`, `JTextField`, `JTextArea`, `JComboBox`, `JCheckBox`, `CardLayout`, `BorderLayout`, `FlowLayout`, `GridBagLayout`. | `src/com/hostel/mess/view/` |
| **HTML5 & CSS3 Web Portal** | Responsive web client (`http://localhost:8080/`) satisfying syllabus flexibility: *"മുഴുവൻ GUI-യും AWT/Swing ഉപയോഗിച്ച് ചെയ്യണമെന്ന് നിർബന്ധമില്ല. GUI-യുടെ മറ്റു ഭാഗങ്ങൾക്ക് വേറെ technologies ഉപയോഗിക്കാം (HTML/CSS)"*. | `src/com/hostel/mess/web/` |
| **SQL Database (CRUD)** | **Create** (`INSERT`), **Read** (`SELECT`), **Update** (`UPDATE`), **Delete** (`DELETE`) using JDBC `PreparedStatement`. Supports **MySQL (XAMPP)** and **SQLite**. | `MenuDAOImpl.java`, `DatabaseManager.java`, `db.properties` |

---

## 🗂 Project Architecture

```
hostel-mess-daily-menu-management/
├── bin/                             # Compiled Java bytecode (.class files)
├── documents/                       # University syllabus & evaluation criteria references
├── lib/                             # External JDBC & logging JARs
│   ├── sqlite-jdbc.jar              # Embedded zero-config SQLite SQL database engine
│   ├── mysql-connector-j.jar        # MySQL JDBC Driver (for optional XAMPP/MySQL)
│   ├── slf4j-api.jar                # Logger API
│   └── slf4j-simple.jar             # Logger implementation
├── src/
│   └── com/hostel/mess/
│       ├── AppMain.java             # Main application entry point & Web server bootstrap
│       ├── TestVerification.java   # 7-phase automated unit & CRUD verification test
│       ├── model/                   # Domain entities (OOP Encapsulation & Inheritance)
│       │   ├── BaseEntity.java      # Abstract base class with id and timestamps
│       │   ├── MealType.java        # Enum (BREAKFAST, LUNCH, SNACKS, DINNER)
│       │   └── MenuItem.java        # Menu entity model
│       ├── exception/               # Custom Exception Handling
│       │   ├── MessDatabaseException.java
│       │   └── ValidationException.java
│       ├── database/                # Database layer
│       │   └── DatabaseManager.java # Singleton JDBC manager with auto-table seeding
│       ├── dao/                     # Data Access Object Pattern (CRUD)
│       │   ├── MenuDAO.java         # Interface with overloaded queries
│       │   └── MenuDAOImpl.java     # JDBC PreparedStatement implementation
│       ├── util/                    # Constants & UI Styling
│       │   ├── AppConstants.java    # Colors, Fonts, Days, Meal Types, Categories
│       │   └── UIHelper.java        # Custom rendering & styled Swing components
│       ├── view/                    # AWT & Swing GUI Presentation
│       │   └── MainAppFrame.java    # Unified 2-Tab window (Client Panel + Admin Panel)
│       └── web/                     # Built-in Lightweight HTTP Web Server
│           └── MessWebServer.java   # Serves Client Board & Web Admin CRUD at port 8080
├── db.properties                    # Database configuration (MySQL / SQLite)
├── hostel_mess.db                   # SQLite database (auto-generated)
├── run.bat                          # One-click Windows batch runner for Desktop GUI
├── run_web_browser.bat              # One-click Windows batch runner for Web Client
├── run.ps1                          # PowerShell runner
├── PROJECT_REPORT_VIVA_GUIDE.md     # Viva questions & academic answers guide
└── README.md                        # Documentation
```

---

## 💡 Key Features & Recent Enhancements

### 1. Unified 2-Tab Desktop Window (`MainAppFrame`)
- **Tab 1: Client Panel (Daily Menu List)**:
  - Interactive day selector buttons (Monday through Sunday).
  - Defaults directly to **Monday** on application launch.
  - 4 distinct meal cards: **Breakfast**, **Lunch**, **Evening Tea & Snacks**, **Dinner**.
  - Dish search bar for instant filtering.
- **Tab 2: Admin Panel (Manage Menu)**:
  - Admin login with `admin@gmail.com` / `admin123`.
  - Comprehensive daily menu management with full CRUD operations.

### 2. Complete CRUD Operations with Dedicated Edit Controls
- **Create (+ Add Menu Item)**: Clean input form for Day, Meal Type, Dish Name, Category, Extra Cost, Side Dishes, and Availability.
- **Read (List / View)**: Interactive table with Day filter, Meal filter, and live Search.
- **Update (Edit Item)**:
  - Clicking on any row or clicking the dedicated **`[Edit Selected Item]`** button under the table auto-populates the form.
  - Form switches into visual edit mode (`Editing Item #ID` highlighted in orange).
  - Clicking **`Update Item`** commits changes to the SQL database.
  - A **`Cancel / Clear`** button allows discarding edits.
- **Delete**: Safe deletion confirmation dialog with instant database removal.

### 3. Clean & Standardized Confirmation Messages
All actions display clear, professional database confirmation messages:
- **Add**: `Record added successfully!`
- **Update**: `Record updated successfully!`
- **Delete**: `Record deleted successfully!`

### 4. Auto-Dismiss Notifications & Manual Close Icon
- **In Desktop Swing App**: A persistent bottom status bar confirms actions and automatically resets to `Status: Ready.` after **4 seconds** via `javax.swing.Timer`.
- **In Web Admin Panel**:
  - Notification alert banner includes a manual **`×`** close button.
  - Automatically fades out and disappears after **3.5 seconds**.
  - Browser URL parameter is cleanly replaced (`window.history.replaceState`) so page refreshes don't re-trigger alerts.

### 5. Dynamic Pagination
- **Desktop Swing Table**: Displays **10 records per page** with interactive controls:  
  `[<< First]` `[< Prev]` **Showing 1–10 of 33 records (Page 1 of 4)** `[Next >]` `[Last >>]`.
- **Web Admin Panel**: Displays **8 records per page** with clickable page numbers (`1`, `2`, `3`, `4`, etc.) and Next/Previous navigation.

### 6. Default Startup Day: Monday
Both the Desktop application and the Web browser view default directly to **Monday** upon opening.

---

## 🚀 How to Run the Project

### Option 1: Desktop Application (Windows Batch)
Double-click `run.bat` or run:
```cmd
run.bat
```

### Option 2: Web Browser Interface (Desktop GUI + Web View)
Double-click `run_web_browser.bat` or run:
```cmd
run_web_browser.bat
```
This automatically compiles the project, starts the background server, and opens your browser directly to:
```
http://localhost:8080/?day=Monday
```

### Option 3: PowerShell
```powershell
.\run.ps1
```

### Option 4: Manual Compilation & Execution
```bash
# 1. Compile all source files
javac -encoding UTF-8 -cp "lib/*;src" -d "bin" src/com/hostel/mess/model/*.java src/com/hostel/mess/exception/*.java src/com/hostel/mess/dao/*.java src/com/hostel/mess/database/*.java src/com/hostel/mess/util/*.java src/com/hostel/mess/web/*.java src/com/hostel/mess/view/*.java src/com/hostel/mess/*.java

# 2. Run application
java -cp "lib/*;bin" com.hostel.mess.AppMain
```

### Running the Automated Verification Test Suite
To verify that all 7 test phases (Connection, Create, Read, Search, Update, Delete, Seeded records) pass with 100% success:
```bash
java -cp "lib/*;bin" com.hostel.mess.TestVerification
```

---

## 🔑 Default Login Credentials

- **Admin Portal**: Accessible under **Admin Panel** tab or at `http://localhost:8080/admin`
- **Username**: `admin@gmail.com`
- **Password**: `admin123`

*(The Client Menu View is open for all hostel residents without login).*

---

## 🛢 Database Configuration

The application is pre-configured with **dual database support** in `db.properties`:
1. **MySQL (XAMPP)**: Connects to `jdbc:mysql://localhost:3306/hostel_mess_db` using username `root` and empty password.
2. **SQLite (Zero-Config Fallback)**: If MySQL/XAMPP is not running, the application automatically connects to the embedded SQLite database (`hostel_mess.db`), creating all tables and seeding sample records with no configuration needed.
