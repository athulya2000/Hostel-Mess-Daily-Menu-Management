# TECHNICAL DOCUMENTATION: HOSTEL MESS DAILY MENU MANAGEMENT SYSTEM

**Academic Program:** B.Tech Computer Science and Engineering (Semester 3)  
**Subject:** Object-Oriented Programming with Java Lab  
**Application Type:** Dual-Interface Desktop (Java Swing) & Cloud-Hosted Web Application  
**GitHub Repository:** [https://github.com/athulya2000/Hostel-Mess-Daily-Menu-Management](https://github.com/athulya2000/Hostel-Mess-Daily-Menu-Management)  
**Live Cloud Deployment:** [https://hostel-mess-daily-menu-management-1.onrender.com](https://hostel-mess-daily-menu-management-1.onrender.com)  

---

## 1. Executive Summary & Problem Definition

In traditional student hostels, daily mess menus, special meals, and meal timings are communicated through physical whiteboards or static paper charts. This introduces several recurring issues:
* **Lack of Real-Time Information:** Changes due to ingredient availability are difficult to broadcast promptly to hundreds of hostellers.
* **Manual Record Inefficiency:** Adding, editing, and archiving meal records requires error-prone manual bookkeeping.
* **No Unified Access:** Students have to physically visit the dining hall to check the menu, causing crowding during peak hours.

### Solution Overview
The **Hostel Mess Daily Menu Management System** is a robust, full-stack Java application engineered to digitize meal registers. It offers:
1. **Desktop Client/Admin Dashboard (Java Swing GUI):** A unified two-tab desktop application for mess supervisors and administrators with full CRUD capabilities, pagination, and real-time alerts.
2. **Zero-Dependency Cloud-Hosted Web Application:** Built directly using Java's built-in `com.sun.net.httpserver.HttpServer`, serving responsive modern web interfaces for both hostellers and mess staff without requiring external servlet containers like Tomcat.
3. **Resilient Dual-Mode Database Architecture:** Automatically interfaces with **MySQL (XAMPP)** during local execution and seamlessly fails over to an embedded **SQLite** engine (`hostel_mess.db`) when deployed to cloud environments.

---

## 2. Technology Stack & Technical Specifications

| Component | Technology / Library | Purpose |
| :--- | :--- | :--- |
| **Language & Runtime** | Java SE 17 (LTS) | Core application logic, OOP implementation, concurrency |
| **GUI Framework** | Java Swing & AWT (`javax.swing`, `java.awt`) | Multi-tab desktop user interface, table pagination, dialogs |
| **Database Engines** | MySQL 8.x / MariaDB (XAMPP) & SQLite 3.x | Persistent relational storage for menus, feedback, and notices |
| **Database Connectivity**| JDBC API with `PreparedStatement` | Secure SQL execution and SQL injection defense |
| **Embedded Web Server** | `com.sun.net.httpserver.HttpServer` | Built-in HTTP micro-server serving live HTML/CSS endpoints |
| **Containerization** | Docker (`eclipse-temurin:17-jdk-jammy`) | Cloud runtime environment encapsulation |
| **Cloud Hosting** | Render.com PaaS | Global 24/7 web hosting with dynamic `$PORT` routing |
| **Version Control** | Git & GitHub | Source tracking, commits, automated cloud builds |

---

## 3. System Architecture & Design Patterns

The project strictly follows professional software engineering architectural paradigms:

```
                      +-----------------------------------+
                      |      Main Application Entry       |
                      |        (com.hostel.mess)          |
                      +-----------------+-----------------+
                                        |
                 +----------------------+----------------------+
                 |                                             |
                 v                                             v
    +--------------------------+                 +---------------------------+
    | Desktop Presentation GUI |                 |  Embedded HTTP Web Server |
    |   (Swing EDT Thread)     |                 |  (Port 8080 / Cloud $PORT)|
    +------------+-------------+                 +-------------+-------------+
                 |                                             |
                 +----------------------+----------------------+
                                        |
                                        v
                      +-----------------------------------+
                      |       Data Access Layer (DAO)     |
                      |   MenuDAO / FeedbackDAO / Notice  |
                      +-----------------+-----------------+
                                        |
                                        v
                      +-----------------------------------+
                      |   Database Manager (Singleton)    |
                      +--------+-----------------+--------+
                               |                 |
                   (Primary)   v                 v  (Fallback)
                         +-----------+     +------------+
                         | MySQL DB  |     | SQLite DB  |
                         |  (XAMPP)  |     | (Embedded) |
                         +-----------+     +------------+
```

### Applied Software Design Patterns
1. **Singleton Design Pattern (`DatabaseManager.java`):**
   - Guarantees that only a single instance of the database connection manager is instantiated throughout the JVM's lifecycle.
   - Prevents connection exhaustion, locks, and thread contention.
2. **Data Access Object (DAO) Pattern (`MenuDAO`, `MenuDAOImpl`):**
   - Decouples high-level business/presentation logic from low-level database SQL operations.
   - Enables changing the underlying database engine without modifying the UI layer.
3. **Model-View-Controller (MVC) Pattern:**
   - **Model:** Domain entities (`MenuItem`, `Feedback`, `Notice`, `MealType`).
   - **View:** Swing GUI (`MainAppFrame`, `AdminDashboardFrame`, `StudentPortalFrame`) and HTML templates in `MessWebServer`.
   - **Controller:** Action listeners, event handlers, and HTTP context handlers (`AdminAddHandler`, `AdminUpdateHandler`, `AdminDeleteHandler`).
4. **Resilient Failover Strategy:**
   - On startup, the system attempts a connection to MySQL at `localhost:3306`. If unavailable (e.g. XAMPP not running or deployed on cloud), it automatically activates the embedded SQLite database engine.

---

## 4. Key Functional Modules & Implementation Details

### 4.1. Dual-Tab Unified Desktop Interface
- **Tab 1: Student / Public Portal:**
  - Read-only real-time view of daily mess menus.
  - Interactive day filter with **Monday pre-selected by default**.
  - Category filters: All, Breakfast, Lunch, Snacks, Dinner, and Chef Specials.
  - Integrated Student Feedback submission form with star ratings.
  - Live Notice Board displaying mess announcements.
- **Tab 2: Admin Management Console:**
  - Protected by administrative authentication (`admin@gmail.com` / `admin123`).
  - Full CRUD operations with auto-dismissing notifications.
  - Dynamic table pagination with page size selector.

### 4.2. Complete CRUD Lifecycle
* **Create (Add Dish):**
  - Form inputs: Dish Name, Category (Breakfast, Lunch, Snacks, Dinner), Day (Monday–Sunday), Price (₹), Dietary Type (Veg/Non-Veg), Chef Special flag, and Availability Status.
  - Validates non-empty names and non-negative numeric pricing.
  - Displays instant alert: `✔ Dish '<Name>' added successfully! [×]`.
* **Read (Search & Filter):**
  - Instant SQL filtering by day of the week and meal type.
  - Sorted display prioritizing Chef Specials and availability.
* **Update (Edit Selected Item):**
  - Selecting any row in the table and clicking **"Edit Selected Item"** loads all existing attributes into the editing form.
  - Admin modifies desired values and clicks **"Update Item"**.
  - Displays instant alert: `✔ Dish '<Name>' updated successfully! [×]`.
* **Delete (Remove Dish):**
  - Admin selects an item and triggers deletion with a confirmation dialog.
  - Displays instant alert: `✔ Dish '<Name>' deleted successfully! [×]`.

### 4.3. Dynamic Table Pagination Engine
To ensure high GUI performance when hundreds of dish records exist:
* Displays items in structured chunks (10 rows per page on Desktop; 8 rows per page on Web).
* Controls: **"◀ Previous"**, **"Next ▶"**, and dynamic label: `Page X of Y (Total: N items)`.
* Automatically recalculates total pages upon filtering or data modifications.

### 4.4. Auto-Dismissing Success Notification Banner
* Replaces blocking modal popups with a non-intrusive banner.
* Displays dish-specific confirmation (e.g., `✔ Dish 'Idli Sambar' added successfully!`).
* Features an explicit manual dismiss button `[×]` and a background timer thread that automatically fades out after 4 seconds.

---

## 5. Database Schema & Relational Design

The system manages relational data across three primary tables:

### Table 1: `menu_items`
```sql
CREATE TABLE IF NOT EXISTS menu_items (
    id INTEGER PRIMARY KEY AUTO_INCREMENT, -- (AUTOINCREMENT in SQLite)
    name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,         -- BREAKFAST, LUNCH, SNACKS, DINNER
    day_of_week VARCHAR(15) NOT NULL,      -- Monday through Sunday
    price DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    is_veg BOOLEAN NOT NULL DEFAULT 1,
    is_special BOOLEAN NOT NULL DEFAULT 0,
    is_available BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Table 2: `feedback`
```sql
CREATE TABLE IF NOT EXISTS feedback (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    student_name VARCHAR(100) NOT NULL,
    rating INTEGER NOT NULL,               -- 1 to 5 Stars
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Table 3: `notices`
```sql
CREATE TABLE IF NOT EXISTS notices (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    posted_date DATE NOT NULL
);
```

---

## 6. Cloud Deployment & Container Architecture

### 6.1. Headless Cloud Detection
Desktop GUI frameworks (`javax.swing.JFrame`) fail in cloud servers that lack an X11/screen display. The application incorporates intelligent headless detection in `AppMain.java`:
```java
if (java.awt.GraphicsEnvironment.isHeadless() || isServerOnlyFlag) {
    System.out.println("Headless Cloud Server Mode detected: Starting Web Server only.");
    Thread.currentThread().join(); // Keeps cloud process active indefinitely
    return;
}
```

### 6.2. Dynamic Port Binding
Cloud platforms like Render assign random port numbers at runtime via the `PORT` environment variable:
```java
String envPort = System.getenv("PORT");
int port = (envPort != null) ? Integer.parseInt(envPort) : 8080;
server = HttpServer.create(new InetSocketAddress(port), 0);
```

### 6.3. Docker Packaging
Packaged via an official Eclipse Temurin JDK 17 container:
```dockerfile
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY . .
RUN mkdir -p bin && javac -encoding UTF-8 -d bin -cp "lib/*" $(find src -name "*.java")
EXPOSE 8080
CMD ["java", "-Djava.awt.headless=true", "-cp", "bin:lib/*", "com.hostel.mess.AppMain", "--server-only"]
```

---

## 7. Verification & Testing Results

The application includes an automated test harness (`TestVerification.java`) verifying 7 distinct execution phases:

| Phase | Test Description | Expected Result | Status |
| :---: | :--- | :--- | :---: |
| 1 | Database Driver & Connection Pool | MySQL or SQLite connection active | **PASSED** |
| 2 | Table Schema Generation | All 3 tables created / verified | **PASSED** |
| 3 | DAO Create (Insert Dish) | Auto-generated ID returned | **PASSED** |
| 4 | DAO Read & Filter (Monday Default) | Correct subset of items returned | **PASSED** |
| 5 | DAO Update (Modify Dish Details) | Updated fields match database state | **PASSED** |
| 6 | DAO Pagination Math | Accurate page count and sublist slice | **PASSED** |
| 7 | DAO Delete (Remove Dish) | Record deleted without foreign key locks | **PASSED** |

---

## 8. Summary of Academic Learning Outcomes

Through the development and submission of this project, the following core Computer Science competencies were mastered:
1. **Object-Oriented Programming:** Implementation of inheritance (`BaseEntity` base class), encapsulation (private attributes, validated getters/setters), polymorphism (interface-driven DAOs), and abstraction.
2. **Graphical User Interfaces:** Layout managers (`BorderLayout`, `GridBagLayout`), custom table models, event listener architectures, and EDT concurrency safety.
3. **Database Connectivity:** JDBC connection pooling, precompiled statements, SQL injection resilience, and multi-dialect compatibility (MySQL + SQLite).
4. **Network Programming:** Lightweight HTTP servers, query parameter parsing, session simulation, and RESTful routing.
5. **Modern DevOps & Cloud Delivery:** Containerization with Docker, Git version control, and production cloud deployment on Render.com.
