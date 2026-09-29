package com.hostel.mess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import com.hostel.mess.util.AppConstants;
import com.hostel.mess.util.UIHelper;

/**
 * Main Application Launcher Window.
 * 
 * Provides entrance to both roles:
 * 1. Resident / Student Menu View
 * 2. Mess Warden / Admin CRUD Management
 * 
 * Also contains an Academic Overview dialog for viva and presentation evaluation.
 */
public class MainLauncherFrame extends JFrame {

    public MainLauncherFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle(AppConstants.APP_TITLE);
        setSize(780, 520);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel header = UIHelper.createHeaderBanner(
                "Hostel Mess Daily Menu Register",
                "Daily Meal Schedules, Dietary Management & Menu Register"
        );
        add(header, BorderLayout.NORTH);

        // Center Content with Role Cards
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(AppConstants.COLOR_BG_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // Card 1: Client Side Menu List
        JPanel cardStudent = createRoleCard(
                "Client Side: Daily Menu List",
                "View daily breakfast, lunch, snacks, and dinner schedules. Filter by days of the week (Monday - Sunday), search dishes, and see veg/non-veg categories.",
                "View Daily Menu",
                AppConstants.COLOR_PRIMARY,
                e -> {
                    StudentPortalFrame studentFrame = new StudentPortalFrame();
                    studentFrame.setVisible(true);
                }
        );
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        centerPanel.add(cardStudent, gbc);

        // Card 2: Mess Admin Panel
        JPanel cardAdmin = createRoleCard(
                "Mess Admin Panel",
                "Manage daily menus, add new dishes, update schedule items, and maintain meal details with database connectivity.",
                "Admin Login",
                AppConstants.COLOR_ACCENT,
                e -> {
                    AdminLoginDialog dialog = new AdminLoginDialog(this);
                    dialog.setVisible(true);
                    if (dialog.isAuthenticated()) {
                        AdminDashboardFrame adminFrame = new AdminDashboardFrame();
                        adminFrame.setVisible(true);
                    }
                }
        );
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(cardAdmin, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        JLabel lblCopyright = new JLabel("Hostel Mess Register System - Built with Java, Swing, HTML/CSS, MySQL & SQLite");
        lblCopyright.setFont(AppConstants.FONT_SMALL);
        lblCopyright.setForeground(AppConstants.COLOR_TEXT_MUTED);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        JButton btnWebPortal = UIHelper.createStyledButton("Open HTML/CSS Web View", new Color(40, 167, 69), Color.WHITE);
        btnWebPortal.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080/"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open browser. Please visit http://localhost:8080/ manually.", "Web Portal", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnVivaInfo = UIHelper.createSecondaryButton("Syllabus & Viva Highlights");
        btnVivaInfo.addActionListener(e -> showProjectInfoDialog());

        rightBtns.add(btnWebPortal);
        rightBtns.add(btnVivaInfo);

        footer.add(lblCopyright, BorderLayout.WEST);
        footer.add(rightBtns, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createRoleCard(String title, String description, String buttonText, Color themeColor, java.awt.event.ActionListener action) {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 222, 235), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppConstants.FONT_HEADER_MEDIUM);
        lblTitle.setForeground(themeColor);

        JLabel lblDesc = new JLabel("<html><p style='line-height:1.4;'>" + description + "</p></html>");
        lblDesc.setFont(AppConstants.FONT_BODY);
        lblDesc.setForeground(AppConstants.COLOR_TEXT_DARK);

        JButton btnAction = UIHelper.createStyledButton(buttonText, themeColor, Color.WHITE);
        btnAction.setPreferredSize(new Dimension(160, 40));
        btnAction.addActionListener(action);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnAction);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void showProjectInfoDialog() {
        JDialog dialog = new JDialog(this, "Academic Syllabus & Viva Highlights", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = UIHelper.createHeaderBanner("B.Tech S3 Java Requirements Checklist", "Mapping implementation to KTU/University Syllabus");
        dialog.add(header, BorderLayout.NORTH);

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txt.setMargin(new Insets(12, 14, 12, 14));
        txt.setText(
            "========================================================================\n" +
            "  HOSTEL MESS DAILY MENU REGISTER - S3 JAVA PROJECT REPORT CHECKLIST\n" +
            "========================================================================\n\n" +
            "1. OBJECT-ORIENTED PROGRAMMING (OOP) CONCEPTS:\n" +
            "   • Data Abstraction: MenuDAO, NoticeDAO, FeedbackDAO interfaces.\n" +
            "   • Encapsulation: Private fields with validated getters and setters.\n" +
            "   • Inheritance: BaseEntity superclass inherited by MenuItem, Notice, and Feedback.\n" +
            "   • Polymorphism:\n" +
            "       - Method Overloading: getAllMenuItems(), getMenuItemsByDay(), getMenuItems(day, meal)\n" +
            "       - Method Overriding: getEntitySummary(), toString(), paintComponent().\n" +
            "   • 'this' and 'super': Used in constructors and method invocations.\n" +
            "   • Static Members & Final Variables: AppConstants.java with UI constants.\n\n" +
            "2. DESIGN PATTERNS & ARCHITECTURE:\n" +
            "   • Singleton Pattern: DatabaseManager ensures a single synchronized DB connection.\n" +
            "   • DAO (Data Access Object) Pattern: Decouples UI from persistence.\n" +
            "   • MVC Architecture: Model (Entities), View (Swing Frames), Controller/DAO.\n\n" +
            "3. DATABASE & SQL REQUIREMENTS (CRUD OPERATIONS):\n" +
            "   • Database: SQLite (Zero-config embedded SQL database) / MySQL compatible.\n" +
            "   • SQL Queries Implemented:\n" +
            "       - CREATE: INSERT INTO menu_items / notices / feedbacks\n" +
            "       - READ: SELECT * FROM menu_items with filtering, sorting, and search\n" +
            "       - UPDATE: UPDATE menu_items SET ... WHERE id = ?\n" +
            "       - DELETE: DELETE FROM menu_items WHERE id = ?\n" +
            "   • JDBC API: DriverManager, Connection, PreparedStatement, Statement, ResultSet.\n\n" +
            "4. AWT & SWING GUI COMPONENTS:\n" +
            "   • Components: JFrame, JDialog, JTable, JScrollPane, JTabbedPane, JSplitPane,\n" +
            "                 JLabel, JButton, JTextField, JTextArea, JComboBox, JCheckBox, JSlider.\n" +
            "   • Layout Managers: BorderLayout, GridLayout, GridBagLayout, BoxLayout, FlowLayout.\n" +
            "   • Event Handling: Delegation Event Model (ActionListener, MouseListener, KeyAdapter).\n\n" +
            "5. EXCEPTION HANDLING:\n" +
            "   • Custom Checked Exceptions: MessDatabaseException, ValidationException.\n" +
            "   • Try-catch-finally and Try-with-resources for reliable DB resource cleanup.\n"
        );

        dialog.add(new JScrollPane(txt), BorderLayout.CENTER);

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnClose = UIHelper.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        closePanel.add(btnClose);
        dialog.add(closePanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
