package com.hostel.mess.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.hostel.mess.dao.MenuDAO;
import com.hostel.mess.dao.MenuDAOImpl;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;
import com.hostel.mess.util.AppConstants;
import com.hostel.mess.util.UIHelper;

/**
 * Unified Single Window with 2 Tabs:
 * Tab 1: Client Panel (Daily Menu List)
 * Tab 2: Admin Panel (Add, Edit, Delete Menu Items)
 */
public class MainAppFrame extends JFrame {

    private MenuDAO menuDAO;
    private JTabbedPane mainTabbedPane;

    // --- Tab 1: Client Panel fields ---
    private String selectedClientDay;
    private JPanel clientMealCardsContainer;
    private JLabel lblClientDayBanner;
    private JTextField txtClientSearch;
    private JPanel daysBtnRow;

    // --- Tab 2: Admin Panel fields ---
    private JPanel adminCardContainer;
    private CardLayout adminCardLayout;
    private boolean adminAuthenticated = false;

    // Admin Login Card
    private JTextField txtAdminUser;
    private JPasswordField txtAdminPass;

    // Admin CRUD Card
    private JTable adminTable;
    private DefaultTableModel adminTableModel;
    private JComboBox<String> filterDayCombo;
    private JComboBox<String> filterMealCombo;
    private JTextField txtAdminSearch;
    private JLabel lblAdminStatus;

    // Admin Pagination
    private List<MenuItem> adminFullItemList = new ArrayList<>();
    private int adminCurrentPage = 1;
    private final int adminPageSize = 10;
    private JLabel lblPaginationInfo;
    private JButton btnFirstPage;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    private JButton btnLastPage;
    private javax.swing.Timer statusResetTimer;

    private JLabel lblFormMode;
    private int selectedMenuItemId = -1;
    private JComboBox<String> comboFormDay;
    private JComboBox<MealType> comboFormMealType;
    private JTextField txtFormItemName;
    private JComboBox<String> comboFormCategory;
    private JTextField txtFormExtraCost;
    private JTextArea txtFormDescription;
    private JCheckBox chkFormAvailable;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    public MainAppFrame() {
        initDAO();
        this.selectedClientDay = "Monday";

        initUI();
        loadClientMenu(selectedClientDay);
    }

    private void initDAO() {
        try {
            this.menuDAO = new MenuDAOImpl();
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setTitle(AppConstants.APP_TITLE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel header = UIHelper.createHeaderBanner(
                "Hostel Mess Daily Menu Register",
                "Daily Meal Schedules, Dietary Management & Menu Register"
        );
        add(header, BorderLayout.NORTH);

        // 2 TABS: Client Panel & Admin Panel
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(AppConstants.FONT_TITLE);

        // Tab 1: Client Panel
        mainTabbedPane.addTab("Client Panel (Daily Menu)", createClientPanel());

        // Tab 2: Admin Panel
        mainTabbedPane.addTab("Admin Panel", createAdminTabContainer());

        // Refresh data when switching tabs
        mainTabbedPane.addChangeListener(e -> {
            int selectedIndex = mainTabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadClientMenu(selectedClientDay);
            } else if (selectedIndex == 1 && adminAuthenticated) {
                loadAdminMenuData();
            }
        });

        add(mainTabbedPane, BorderLayout.CENTER);

        // Footer with Web Browser Link
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JButton btnOpenBrowser = UIHelper.createStyledButton("Open in Web Browser", new Color(40, 167, 69), Color.WHITE);
        btnOpenBrowser.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080/"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Visit http://localhost:8080/ in your browser.", "Web View", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        footer.add(btnOpenBrowser, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // =========================================================================
    // TAB 1: CLIENT PANEL (Daily Menu List)
    // =========================================================================
    private JPanel createClientPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AppConstants.COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Navigation Bar (Days + Search)
        JPanel navPanel = new JPanel(new BorderLayout(8, 8));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        daysBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        daysBtnRow.setOpaque(false);

        lblClientDayBanner = new JLabel("Day: " + selectedClientDay);
        lblClientDayBanner.setFont(AppConstants.FONT_BODY_BOLD);
        lblClientDayBanner.setForeground(AppConstants.COLOR_PRIMARY);
        lblClientDayBanner.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        daysBtnRow.add(lblClientDayBanner);

        for (String day : AppConstants.DAYS_OF_WEEK) {
            JButton btnDay = new JButton(day);
            btnDay.setFont(AppConstants.FONT_BODY);
            btnDay.setFocusPainted(false);
            btnDay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            if (day.equalsIgnoreCase(selectedClientDay)) {
                btnDay.setBackground(AppConstants.COLOR_PRIMARY);
                btnDay.setForeground(Color.WHITE);
            } else {
                btnDay.setBackground(new Color(240, 243, 248));
                btnDay.setForeground(AppConstants.COLOR_TEXT_DARK);
            }

            btnDay.addActionListener(e -> {
                selectedClientDay = day;
                lblClientDayBanner.setText("Day: " + selectedClientDay);
                for (java.awt.Component c : daysBtnRow.getComponents()) {
                    if (c instanceof JButton) {
                        JButton b = (JButton) c;
                        if (b.getText().equalsIgnoreCase(selectedClientDay)) {
                            b.setBackground(AppConstants.COLOR_PRIMARY);
                            b.setForeground(Color.WHITE);
                        } else {
                            b.setBackground(new Color(240, 243, 248));
                            b.setForeground(AppConstants.COLOR_TEXT_DARK);
                        }
                    }
                }
                loadClientMenu(selectedClientDay);
            });

            daysBtnRow.add(btnDay);
        }

        navPanel.add(daysBtnRow, BorderLayout.WEST);

        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        txtClientSearch = UIHelper.createStyledTextField(12);
        JButton btnSearch = UIHelper.createPrimaryButton("Search Dish");
        btnSearch.setPreferredSize(new Dimension(110, 32));

        btnSearch.addActionListener(e -> {
            String kw = txtClientSearch.getText().trim();
            if (!kw.isEmpty()) {
                searchClientDishes(kw);
            } else {
                loadClientMenu(selectedClientDay);
            }
        });

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtClientSearch);
        searchPanel.add(btnSearch);
        navPanel.add(searchPanel, BorderLayout.EAST);

        panel.add(navPanel, BorderLayout.NORTH);

        // 4 Meal Cards in 2x2 Grid
        clientMealCardsContainer = new JPanel(new GridLayout(2, 2, 14, 14));
        clientMealCardsContainer.setBackground(AppConstants.COLOR_BG_LIGHT);

        JScrollPane mealScroll = new JScrollPane(clientMealCardsContainer);
        mealScroll.setBorder(null);
        mealScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(mealScroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadClientMenu(String day) {
        clientMealCardsContainer.removeAll();
        try {
            List<MenuItem> allDayItems = menuDAO.getMenuItemsByDay(day);
            for (MealType mt : MealType.values()) {
                JPanel card = createClientMealCard(mt, allDayItems);
                clientMealCardsContainer.add(card);
            }
        } catch (MessDatabaseException e) {
            System.err.println("Failed to load client menu: " + e.getMessage());
        }
        clientMealCardsContainer.revalidate();
        clientMealCardsContainer.repaint();
    }

    private void searchClientDishes(String keyword) {
        clientMealCardsContainer.removeAll();
        try {
            List<MenuItem> results = menuDAO.searchMenuItems(keyword);
            if (results.isEmpty()) {
                JPanel emptyPanel = new JPanel(new BorderLayout());
                emptyPanel.setBackground(Color.WHITE);
                JLabel lblEmpty = new JLabel("No dishes found matching: '" + keyword + "'", JLabel.CENTER);
                lblEmpty.setFont(AppConstants.FONT_HEADER_MEDIUM);
                lblEmpty.setForeground(AppConstants.COLOR_TEXT_MUTED);
                emptyPanel.add(lblEmpty, BorderLayout.CENTER);
                clientMealCardsContainer.setLayout(new BorderLayout());
                clientMealCardsContainer.add(emptyPanel, BorderLayout.CENTER);
            } else {
                clientMealCardsContainer.setLayout(new GridLayout(2, 2, 14, 14));
                for (MealType mt : MealType.values()) {
                    JPanel card = createClientMealCard(mt, results);
                    clientMealCardsContainer.add(card);
                }
            }
        } catch (MessDatabaseException e) {
            System.err.println("Client search error: " + e.getMessage());
        }
        clientMealCardsContainer.revalidate();
        clientMealCardsContainer.repaint();
    }

    private JPanel createClientMealCard(MealType mealType, List<MenuItem> items) {
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel lblMealTitle = new JLabel(mealType.getDisplayName());
        lblMealTitle.setFont(AppConstants.FONT_HEADER_MEDIUM);
        lblMealTitle.setForeground(AppConstants.COLOR_PRIMARY);

        JLabel lblTime = new JLabel(mealType.getDefaultTiming());
        lblTime.setFont(AppConstants.FONT_SMALL);
        lblTime.setForeground(AppConstants.COLOR_TEXT_MUTED);

        cardHeader.add(lblMealTitle, BorderLayout.WEST);
        cardHeader.add(lblTime, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel dishesList = new JPanel();
        dishesList.setLayout(new BoxLayout(dishesList, BoxLayout.Y_AXIS));
        dishesList.setBackground(Color.WHITE);

        int count = 0;
        for (MenuItem item : items) {
            if (item.getMealType() == mealType) {
                count++;
                dishesList.add(createClientDishRow(item));
                dishesList.add(Box.createVerticalStrut(6));
            }
        }

        if (count == 0) {
            JLabel lblNoMenu = new JLabel("No menu items scheduled.");
            lblNoMenu.setFont(AppConstants.FONT_BODY);
            lblNoMenu.setForeground(AppConstants.COLOR_TEXT_MUTED);
            dishesList.add(lblNoMenu);
        }

        JScrollPane scroll = new JScrollPane(dishesList);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createClientDishRow(MenuItem item) {
        JPanel row = new JPanel(new BorderLayout(5, 2));
        row.setBackground(new Color(250, 251, 254));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblName = new JLabel(item.getItemName());
        lblName.setFont(AppConstants.FONT_BODY_BOLD);
        lblName.setForeground(item.isAvailable() ? AppConstants.COLOR_TEXT_DARK : AppConstants.COLOR_TEXT_MUTED);
        textPanel.add(lblName);

        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            JLabel lblDesc = new JLabel("<html><i>" + item.getDescription() + "</i></html>");
            lblDesc.setFont(AppConstants.FONT_SMALL);
            lblDesc.setForeground(AppConstants.COLOR_TEXT_MUTED);
            textPanel.add(lblDesc);
        }

        row.add(textPanel, BorderLayout.CENTER);

        JPanel badgePanel = new JPanel();
        badgePanel.setLayout(new BoxLayout(badgePanel, BoxLayout.Y_AXIS));
        badgePanel.setOpaque(false);

        Color catColor = "Non-Vegetarian".equalsIgnoreCase(item.getCategory()) ? new Color(180, 40, 40)
                       : "Special Feast".equalsIgnoreCase(item.getCategory()) ? new Color(160, 80, 0)
                       : new Color(30, 130, 45);

        JLabel lblCat = new JLabel("[" + item.getCategory() + "]");
        lblCat.setFont(AppConstants.FONT_SMALL);
        lblCat.setForeground(catColor);
        badgePanel.add(lblCat);

        if (item.getExtraCost() > 0) {
            JLabel lblCost = new JLabel(String.format("+ Rs %.1f", item.getExtraCost()));
            lblCost.setFont(AppConstants.FONT_SMALL);
            lblCost.setForeground(AppConstants.COLOR_PRIMARY);
            badgePanel.add(lblCost);
        }

        row.add(badgePanel, BorderLayout.EAST);
        return row;
    }

    // =========================================================================
    // TAB 2: ADMIN PANEL CONTAINER (Login View -> CRUD Dashboard View)
    // =========================================================================
    private JPanel createAdminTabContainer() {
        adminCardLayout = new CardLayout();
        adminCardContainer = new JPanel(adminCardLayout);

        // Card 1: Admin Login Panel
        JPanel loginPanel = createAdminLoginCard();
        adminCardContainer.add(loginPanel, "LOGIN");

        // Card 2: Admin CRUD Dashboard Panel
        JPanel crudPanel = createAdminCrudDashboardCard();
        adminCardContainer.add(crudPanel, "DASHBOARD");

        adminCardLayout.show(adminCardContainer, "LOGIN");
        return adminCardContainer;
    }

    private JPanel createAdminLoginCard() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(AppConstants.COLOR_BG_LIGHT);

        JPanel card = UIHelper.createCardPanel();
        card.setPreferredSize(new Dimension(380, 260));
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        JLabel lblTitle = new JLabel("Mess Admin Panel");
        lblTitle.setFont(AppConstants.FONT_HEADER_MEDIUM);
        lblTitle.setForeground(AppConstants.COLOR_PRIMARY);
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtAdminUser = UIHelper.createStyledTextField(14);
        form.add(txtAdminUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtAdminPass = new JPasswordField(14);
        txtAdminPass.setFont(AppConstants.FONT_BODY);
        txtAdminPass.setPreferredSize(new Dimension(txtAdminPass.getPreferredSize().width, 32));
        form.add(txtAdminPass, gbc);

        card.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        JButton btnLogin = UIHelper.createPrimaryButton("Login to Admin Panel");
        btnLogin.addActionListener(e -> performAdminLogin());

        // Press Enter to login
        txtAdminPass.addActionListener(e -> performAdminLogin());

        btnRow.add(btnLogin);
        card.add(btnRow, BorderLayout.SOUTH);

        wrapper.add(card);
        return wrapper;
    }

    private void performAdminLogin() {
        String u = txtAdminUser.getText().trim();
        String p = new String(txtAdminPass.getPassword());

        if (AppConstants.DEFAULT_ADMIN_USER.equals(u) && AppConstants.DEFAULT_ADMIN_PASS.equals(p)) {
            adminAuthenticated = true;
            txtAdminPass.setText("");
            loadAdminMenuData();
            adminCardLayout.show(adminCardContainer, "DASHBOARD");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            txtAdminPass.setText("");
            txtAdminPass.requestFocus();
        }
    }

    private JPanel createAdminCrudDashboardCard() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppConstants.COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Filter Bar with Logout Button
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterControls.setOpaque(false);

        filterControls.add(new JLabel("Day:"));
        filterDayCombo = new JComboBox<>();
        filterDayCombo.addItem("All Days");
        for (String day : AppConstants.DAYS_OF_WEEK) {
            filterDayCombo.addItem(day);
        }
        filterControls.add(filterDayCombo);

        filterControls.add(new JLabel("Meal:"));
        filterMealCombo = new JComboBox<>();
        filterMealCombo.addItem("All Meals");
        for (MealType mt : MealType.values()) {
            filterMealCombo.addItem(mt.name());
        }
        filterControls.add(filterMealCombo);

        filterControls.add(new JLabel("Search:"));
        txtAdminSearch = UIHelper.createStyledTextField(12);
        filterControls.add(txtAdminSearch);

        JButton btnFilter = UIHelper.createPrimaryButton("Filter / Search");
        JButton btnReset = UIHelper.createSecondaryButton("Reset");

        btnFilter.addActionListener(e -> applyAdminFilters());
        btnReset.addActionListener(e -> {
            filterDayCombo.setSelectedIndex(0);
            filterMealCombo.setSelectedIndex(0);
            txtAdminSearch.setText("");
            loadAdminMenuData();
        });

        filterControls.add(btnFilter);
        filterControls.add(btnReset);
        topBar.add(filterControls, BorderLayout.WEST);

        JButton btnLock = UIHelper.createDangerButton("Logout Admin");
        btnLock.addActionListener(e -> {
            adminAuthenticated = false;
            adminCardLayout.show(adminCardContainer, "LOGIN");
        });
        topBar.add(btnLock, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // Center Table (Read/List)
        String[] columns = {"ID", "Day", "Meal", "Dish Name", "Category", "Extra Cost (Rs)", "Available"};
        adminTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        adminTable = new JTable(adminTableModel);
        adminTable.setFont(AppConstants.FONT_BODY);
        adminTable.setRowHeight(28);
        adminTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adminTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);

        adminTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        adminTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        adminTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        adminTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        adminTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        adminTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        adminTable.getColumnModel().getColumn(6).setPreferredWidth(75);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        adminTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        adminTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        adminTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        adminTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        adminTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        adminTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = adminTable.getSelectedRow();
                if (row != -1) {
                    populateAdminForm(row);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(adminTable);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 6));
        tablePanel.setOpaque(false);
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        JPanel tableActionBar = new JPanel(new BorderLayout());
        tableActionBar.setOpaque(false);
        tableActionBar.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));

        JLabel lblTableHint = new JLabel("Select an item above to Edit or Delete:");
        lblTableHint.setFont(AppConstants.FONT_SMALL);
        lblTableHint.setForeground(AppConstants.COLOR_TEXT_MUTED);
        tableActionBar.add(lblTableHint, BorderLayout.WEST);

        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableButtons.setOpaque(false);

        JButton btnTableEdit = UIHelper.createPrimaryButton("Edit Selected Item");
        JButton btnTableDelete = UIHelper.createDangerButton("Delete Selected Item");

        btnTableEdit.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row != -1) {
                populateAdminForm(row);
                txtFormItemName.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the table to edit!", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnTableDelete.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row != -1) {
                populateAdminForm(row);
                performDeleteMenuItem();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the table to delete!", "Select Row", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        tableButtons.add(btnTableEdit);
        tableButtons.add(btnTableDelete);
        tableActionBar.add(tableButtons, BorderLayout.EAST);

        // Pagination Bar
        JPanel paginationPanel = new JPanel(new BorderLayout(8, 0));
        paginationPanel.setOpaque(false);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));

        lblPaginationInfo = new JLabel("Showing 0-0 of 0 records");
        lblPaginationInfo.setFont(AppConstants.FONT_SMALL);
        lblPaginationInfo.setForeground(AppConstants.COLOR_TEXT_DARK);
        paginationPanel.add(lblPaginationInfo, BorderLayout.WEST);

        JPanel pageButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pageButtons.setOpaque(false);

        btnFirstPage = UIHelper.createSecondaryButton("<< First");
        btnPrevPage = UIHelper.createSecondaryButton("< Prev");
        btnNextPage = UIHelper.createSecondaryButton("Next >");
        btnLastPage = UIHelper.createSecondaryButton("Last >>");

        Dimension pBtnDim = new Dimension(80, 26);
        btnFirstPage.setPreferredSize(pBtnDim);
        btnPrevPage.setPreferredSize(pBtnDim);
        btnNextPage.setPreferredSize(pBtnDim);
        btnLastPage.setPreferredSize(pBtnDim);

        btnFirstPage.addActionListener(e -> { adminCurrentPage = 1; updateAdminTablePage(); });
        btnPrevPage.addActionListener(e -> { if (adminCurrentPage > 1) { adminCurrentPage--; updateAdminTablePage(); } });
        btnNextPage.addActionListener(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) adminFullItemList.size() / adminPageSize));
            if (adminCurrentPage < totalPages) { adminCurrentPage++; updateAdminTablePage(); }
        });
        btnLastPage.addActionListener(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) adminFullItemList.size() / adminPageSize));
            adminCurrentPage = totalPages;
            updateAdminTablePage();
        });

        pageButtons.add(btnFirstPage);
        pageButtons.add(btnPrevPage);
        pageButtons.add(btnNextPage);
        pageButtons.add(btnLastPage);
        paginationPanel.add(pageButtons, BorderLayout.EAST);

        JPanel tableBottomControls = new JPanel(new GridLayout(2, 1, 0, 4));
        tableBottomControls.setOpaque(false);
        tableBottomControls.add(tableActionBar);
        tableBottomControls.add(paginationPanel);

        tablePanel.add(tableBottomControls, BorderLayout.SOUTH);

        // Right Side: Form (Create, Update, Delete)
        JPanel formContainer = createAdminForm();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, formContainer);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(6);
        panel.add(splitPane, BorderLayout.CENTER);

        // Persistent Status Notification Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        lblAdminStatus = new JLabel("Status: Ready. Select an item to Edit or Delete, or fill the form to add a new dish.");
        lblAdminStatus.setFont(AppConstants.FONT_BODY_BOLD);
        lblAdminStatus.setForeground(AppConstants.COLOR_PRIMARY);
        statusBar.add(lblAdminStatus);
        panel.add(statusBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAdminForm() {
        JPanel formCard = UIHelper.createCardPanel();
        formCard.setLayout(new BorderLayout(5, 5));
        formCard.setPreferredSize(new Dimension(380, 500));

        lblFormMode = new JLabel("[+] Add New Menu Item");
        lblFormMode.setFont(AppConstants.FONT_HEADER_MEDIUM);
        lblFormMode.setForeground(AppConstants.COLOR_PRIMARY);
        lblFormMode.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        formCard.add(lblFormMode, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fields.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        comboFormDay = new JComboBox<>(AppConstants.DAYS_OF_WEEK);
        fields.add(comboFormDay, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Meal Type:"), gbc);
        gbc.gridx = 1;
        comboFormMealType = new JComboBox<>(MealType.values());
        fields.add(comboFormMealType, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Dish Name:"), gbc);
        gbc.gridx = 1;
        txtFormItemName = UIHelper.createStyledTextField(15);
        fields.add(txtFormItemName, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        comboFormCategory = new JComboBox<>(AppConstants.CATEGORIES);
        fields.add(comboFormCategory, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        fields.add(new JLabel("Extra Cost (Rs):"), gbc);
        gbc.gridx = 1;
        txtFormExtraCost = UIHelper.createStyledTextField(8);
        txtFormExtraCost.setText("0.0");
        fields.add(txtFormExtraCost, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        fields.add(new JLabel("Side Dishes:"), gbc);
        gbc.gridx = 1;
        txtFormDescription = new JTextArea(3, 15);
        txtFormDescription.setFont(AppConstants.FONT_BODY);
        txtFormDescription.setLineWrap(true);
        txtFormDescription.setWrapStyleWord(true);
        fields.add(new JScrollPane(txtFormDescription), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkFormAvailable = new JCheckBox("Dish is Available Today", true);
        chkFormAvailable.setBackground(Color.WHITE);
        chkFormAvailable.setFont(AppConstants.FONT_BODY_BOLD);
        fields.add(chkFormAvailable, gbc);

        formCard.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnAdd = UIHelper.createPrimaryButton("+ Add Menu Item");
        btnUpdate = UIHelper.createSuccessButton("Update Item");
        btnDelete = UIHelper.createDangerButton("Delete Item");
        btnClear = UIHelper.createSecondaryButton("Cancel / Clear");

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> performAddMenuItem());
        btnUpdate.addActionListener(e -> performUpdateMenuItem());
        btnDelete.addActionListener(e -> performDeleteMenuItem());
        btnClear.addActionListener(e -> clearAdminForm());

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        return formCard;
    }

    private void loadAdminMenuData() {
        try {
            adminFullItemList = menuDAO.getAllMenuItems();
            adminCurrentPage = 1;
            updateAdminTablePage();
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load menu: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAdminTablePage() {
        int totalItems = adminFullItemList.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / adminPageSize));
        if (adminCurrentPage > totalPages) adminCurrentPage = totalPages;
        if (adminCurrentPage < 1) adminCurrentPage = 1;

        int start = (adminCurrentPage - 1) * adminPageSize;
        int end = Math.min(start + adminPageSize, totalItems);

        List<MenuItem> pageItems = (start < totalItems) ? adminFullItemList.subList(start, end) : Collections.emptyList();
        renderAdminTable(pageItems);

        if (lblPaginationInfo != null) {
            lblPaginationInfo.setText(String.format("Showing %d-%d of %d records (Page %d of %d)",
                totalItems == 0 ? 0 : (start + 1), end, totalItems, adminCurrentPage, totalPages));
        }

        if (btnFirstPage != null) btnFirstPage.setEnabled(adminCurrentPage > 1);
        if (btnPrevPage != null) btnPrevPage.setEnabled(adminCurrentPage > 1);
        if (btnNextPage != null) btnNextPage.setEnabled(adminCurrentPage < totalPages);
        if (btnLastPage != null) btnLastPage.setEnabled(adminCurrentPage < totalPages);
    }

    private void renderAdminTable(List<MenuItem> items) {
        adminTableModel.setRowCount(0);
        for (MenuItem item : items) {
            adminTableModel.addRow(new Object[]{
                item.getId(),
                item.getDayOfWeek(),
                item.getMealType().getDisplayName(),
                item.getItemName(),
                item.getCategory(),
                String.format("%.2f", item.getExtraCost()),
                item.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void applyAdminFilters() {
        String selectedDay = (String) filterDayCombo.getSelectedItem();
        String selectedMeal = (String) filterMealCombo.getSelectedItem();
        String keyword = txtAdminSearch.getText().trim();

        try {
            List<MenuItem> list;
            if (!keyword.isEmpty()) {
                list = menuDAO.searchMenuItems(keyword);
            } else if (!"All Days".equals(selectedDay) && !"All Meals".equals(selectedMeal)) {
                MealType mt = MealType.valueOf(selectedMeal);
                list = menuDAO.getMenuItems(selectedDay, mt);
            } else if (!"All Days".equals(selectedDay)) {
                list = menuDAO.getMenuItemsByDay(selectedDay);
            } else {
                list = menuDAO.getAllMenuItems();
            }

            if ("All Days".equals(selectedDay) && !"All Meals".equals(selectedMeal)) {
                MealType mt = MealType.valueOf(selectedMeal);
                list = list.stream().filter(m -> m.getMealType() == mt).toList();
            }

            adminFullItemList = list;
            adminCurrentPage = 1;
            updateAdminTablePage();
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateAdminForm(int rowIndex) {
        try {
            int id = (int) adminTableModel.getValueAt(rowIndex, 0);
            MenuItem item = menuDAO.getMenuItemById(id);
            if (item != null) {
                selectedMenuItemId = item.getId();
                lblFormMode.setText("Editing Item #" + selectedMenuItemId + ": " + item.getItemName());
                lblFormMode.setForeground(new Color(230, 81, 0));
                comboFormDay.setSelectedItem(item.getDayOfWeek());
                comboFormMealType.setSelectedItem(item.getMealType());
                txtFormItemName.setText(item.getItemName());
                comboFormCategory.setSelectedItem(item.getCategory());
                txtFormExtraCost.setText(String.valueOf(item.getExtraCost()));
                txtFormDescription.setText(item.getDescription());
                chkFormAvailable.setSelected(item.isAvailable());

                if (lblAdminStatus != null) {
                    lblAdminStatus.setText("Editing Mode: Item #" + item.getId() + " ('" + item.getItemName() + "') selected. Modify fields and click 'Update Item'.");
                    lblAdminStatus.setForeground(new Color(230, 81, 0));
                }

                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAdminForm() {
        selectedMenuItemId = -1;
        lblFormMode.setText("[+] Add New Menu Item");
        lblFormMode.setForeground(AppConstants.COLOR_PRIMARY);
        comboFormDay.setSelectedIndex(0);
        comboFormMealType.setSelectedIndex(0);
        txtFormItemName.setText("");
        comboFormCategory.setSelectedIndex(0);
        txtFormExtraCost.setText("0.0");
        txtFormDescription.setText("");
        chkFormAvailable.setSelected(true);
        adminTable.clearSelection();

        if (lblAdminStatus != null) {
            lblAdminStatus.setText("Status: Ready. Select an item to Edit or Delete, or fill the form to add a new dish.");
            lblAdminStatus.setForeground(AppConstants.COLOR_PRIMARY);
        }

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private void showStatusNotification(String message, Color color) {
        if (lblAdminStatus == null) return;
        lblAdminStatus.setText(message);
        lblAdminStatus.setForeground(color);

        if (statusResetTimer != null && statusResetTimer.isRunning()) {
            statusResetTimer.stop();
        }

        // Auto-dismiss status message after 4 seconds back to default ready state
        statusResetTimer = new javax.swing.Timer(4000, e -> {
            if (lblAdminStatus != null) {
                lblAdminStatus.setText("Status: Ready. Select an item to Edit or Delete, or fill the form to add a new dish.");
                lblAdminStatus.setForeground(AppConstants.COLOR_PRIMARY);
            }
        });
        statusResetTimer.setRepeats(false);
        statusResetTimer.start();
    }

    private void performAddMenuItem() {
        String name = txtFormItemName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Dish Name!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double extraCost = 0.0;
        try {
            extraCost = Double.parseDouble(txtFormExtraCost.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Extra Cost must be a valid number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MenuItem newItem = new MenuItem(
            (String) comboFormDay.getSelectedItem(),
            (MealType) comboFormMealType.getSelectedItem(),
            name,
            (String) comboFormCategory.getSelectedItem(),
            txtFormDescription.getText().trim(),
            extraCost,
            chkFormAvailable.isSelected()
        );

        try {
            boolean success = menuDAO.addMenuItem(newItem);
            if (success) {
                showStatusNotification("SUCCESS: Record added successfully!", new Color(25, 135, 84));
                JOptionPane.showMessageDialog(this, "Record added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearAdminForm();
                loadAdminMenuData();
                loadClientMenu(selectedClientDay);
            }
        } catch (MessDatabaseException e) {
            showStatusNotification("ERROR: Failed to add item: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performUpdateMenuItem() {
        if (selectedMenuItemId <= 0) return;

        String name = txtFormItemName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dish Name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double extraCost = 0.0;
        try {
            extraCost = Double.parseDouble(txtFormExtraCost.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Extra Cost format!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        MenuItem item = new MenuItem(
            selectedMenuItemId,
            (String) comboFormDay.getSelectedItem(),
            (MealType) comboFormMealType.getSelectedItem(),
            name,
            (String) comboFormCategory.getSelectedItem(),
            txtFormDescription.getText().trim(),
            extraCost,
            chkFormAvailable.isSelected(),
            null
        );

        try {
            boolean success = menuDAO.updateMenuItem(item);
            if (success) {
                showStatusNotification("SUCCESS: Record updated successfully!", new Color(25, 135, 84));
                JOptionPane.showMessageDialog(this, "Record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearAdminForm();
                loadAdminMenuData();
                loadClientMenu(selectedClientDay);
            }
        } catch (MessDatabaseException e) {
            showStatusNotification("ERROR: Failed to update item: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, "Update error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDeleteMenuItem() {
        if (selectedMenuItemId <= 0) return;

        int delId = selectedMenuItemId;
        String delName = txtFormItemName.getText();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete item #" + delId + " (" + delName + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = menuDAO.deleteMenuItem(delId);
                if (success) {
                    showStatusNotification("SUCCESS: Record deleted successfully!", new Color(220, 53, 69));
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearAdminForm();
                    loadAdminMenuData();
                    loadClientMenu(selectedClientDay);
                }
            } catch (MessDatabaseException e) {
                showStatusNotification("ERROR: Failed to delete item: " + e.getMessage(), Color.RED);
                JOptionPane.showMessageDialog(this, "Delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
