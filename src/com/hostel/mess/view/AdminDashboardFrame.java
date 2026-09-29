package com.hostel.mess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
 * Admin Panel for Hostel Mess Daily Menu Register.
 * Focused purely on Menu Item CRUD (Create, Read/List, Update, Delete).
 * 
 * Demonstrates:
 * - Swing Controls: JFrame, JTable, JScrollPane, JSplitPane, JTextField,
 *   JTextArea, JComboBox, JCheckBox, JButton, JLabel
 * - Layout Managers: BorderLayout, GridBagLayout, FlowLayout, GridLayout
 * - Event Handling: Delegation Event Model (ActionListener, MouseListener)
 * - JDBC CRUD operations via DAO pattern
 */
public class AdminDashboardFrame extends JFrame {

    private MenuDAO menuDAO;

    // Menu Table components (READ / LIST)
    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    private JComboBox<String> filterDayCombo;
    private JComboBox<String> filterMealCombo;
    private JTextField txtSearch;

    // Menu Form components (CREATE / UPDATE / DELETE)
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

    public AdminDashboardFrame() {
        initDAO();
        initUI();
        loadMenuData();
    }

    private void initDAO() {
        try {
            this.menuDAO = new MenuDAOImpl();
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this,
                    "Database initialization error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setTitle(AppConstants.APP_TITLE + " - Admin Menu Register");
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Banner with Quick Actions
        JPanel header = createAdminHeader();
        add(header, BorderLayout.NORTH);

        // Main Center: Menu CRUD Management (Table + Form)
        JPanel menuPanel = createMenuManagementPanel();
        add(menuPanel, BorderLayout.CENTER);
    }

    private JPanel createAdminHeader() {
        JPanel header = UIHelper.createHeaderBanner(
                "Mess Admin Panel",
                "Add, Edit, List, and Delete daily meal schedule items"
        );

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton btnOpenStudent = UIHelper.createStyledButton("Client View (Menu List)", new Color(40, 167, 69), Color.WHITE);
        btnOpenStudent.addActionListener(e -> {
            StudentPortalFrame studentView = new StudentPortalFrame();
            studentView.setVisible(true);
        });

        JButton btnLogout = UIHelper.createDangerButton("Logout");
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the Admin Panel?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new MainLauncherFrame().setVisible(true);
            }
        });

        rightControls.add(btnOpenStudent);
        rightControls.add(btnLogout);

        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createMenuManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(AppConstants.COLOR_BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Filter and Search Bar (North)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        filterBar.add(new JLabel("Filter Day:"));
        filterDayCombo = new JComboBox<>();
        filterDayCombo.addItem("All Days");
        for (String day : AppConstants.DAYS_OF_WEEK) {
            filterDayCombo.addItem(day);
        }
        filterBar.add(filterDayCombo);

        filterBar.add(new JLabel("Meal:"));
        filterMealCombo = new JComboBox<>();
        filterMealCombo.addItem("All Meals");
        for (MealType mt : MealType.values()) {
            filterMealCombo.addItem(mt.name());
        }
        filterBar.add(filterMealCombo);

        filterBar.add(new JLabel("Search Dish:"));
        txtSearch = UIHelper.createStyledTextField(14);
        filterBar.add(txtSearch);

        JButton btnFilter = UIHelper.createPrimaryButton("Filter / Search");
        JButton btnReset = UIHelper.createSecondaryButton("Reset");

        btnFilter.addActionListener(e -> applyMenuFilters());
        btnReset.addActionListener(e -> {
            filterDayCombo.setSelectedIndex(0);
            filterMealCombo.setSelectedIndex(0);
            txtSearch.setText("");
            loadMenuData();
        });

        filterBar.add(btnFilter);
        filterBar.add(btnReset);
        panel.add(filterBar, BorderLayout.NORTH);

        // 2. Table for READ / LIST
        String[] columns = {"ID", "Day", "Meal", "Dish Name", "Category", "Extra Cost (Rs)", "Available"};
        menuTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        menuTable = new JTable(menuTableModel);
        menuTable.setFont(AppConstants.FONT_BODY);
        menuTable.setRowHeight(28);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.getTableHeader().setFont(AppConstants.FONT_BODY_BOLD);
        menuTable.getTableHeader().setBackground(new Color(235, 240, 248));

        menuTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        menuTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        menuTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        menuTable.getColumnModel().getColumn(6).setPreferredWidth(75);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        menuTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        menuTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        menuTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        menuTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        menuTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        menuTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = menuTable.getSelectedRow();
                if (row != -1) {
                    populateFormFromTableRow(row);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(menuTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 1));

        // 3. Form Panel for CREATE, UPDATE, DELETE (Right side)
        JPanel formContainer = createMenuFormPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, formContainer);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(6);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMenuFormPanel() {
        JPanel formCard = UIHelper.createCardPanel();
        formCard.setLayout(new BorderLayout(5, 5));
        formCard.setPreferredSize(new Dimension(380, 500));

        // Form Title
        lblFormMode = new JLabel("[+] Add New Menu Item");
        lblFormMode.setFont(AppConstants.FONT_HEADER_MEDIUM);
        lblFormMode.setForeground(AppConstants.COLOR_PRIMARY);
        lblFormMode.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        formCard.add(lblFormMode, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Day of Week
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(new JLabel("Day of Week:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        comboFormDay = new JComboBox<>(AppConstants.DAYS_OF_WEEK);
        fieldsPanel.add(comboFormDay, gbc);
        row++;

        // Meal Type
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(new JLabel("Meal Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        comboFormMealType = new JComboBox<>(MealType.values());
        fieldsPanel.add(comboFormMealType, gbc);
        row++;

        // Dish / Item Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(new JLabel("Dish Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtFormItemName = UIHelper.createStyledTextField(15);
        fieldsPanel.add(txtFormItemName, gbc);
        row++;

        // Category
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        comboFormCategory = new JComboBox<>(AppConstants.CATEGORIES);
        fieldsPanel.add(comboFormCategory, gbc);
        row++;

        // Extra Cost
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        fieldsPanel.add(new JLabel("Extra Cost (Rs):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtFormExtraCost = UIHelper.createStyledTextField(8);
        txtFormExtraCost.setText("0.0");
        fieldsPanel.add(txtFormExtraCost, gbc);
        row++;

        // Description / Sides
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        fieldsPanel.add(new JLabel("Side Dishes:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtFormDescription = new JTextArea(3, 15);
        txtFormDescription.setFont(AppConstants.FONT_BODY);
        txtFormDescription.setLineWrap(true);
        txtFormDescription.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtFormDescription);
        fieldsPanel.add(descScroll, gbc);
        row++;

        // Is Available Checkbox
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        chkFormAvailable = new JCheckBox("Dish is Available Today", true);
        chkFormAvailable.setBackground(Color.WHITE);
        chkFormAvailable.setFont(AppConstants.FONT_BODY_BOLD);
        fieldsPanel.add(chkFormAvailable, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // CRUD Action Buttons Panel (South)
        JPanel actionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnAdd = UIHelper.createPrimaryButton("+ Add Menu Item");
        btnUpdate = UIHelper.createSuccessButton("Update Item");
        btnDelete = UIHelper.createDangerButton("Delete Item");
        btnClear = UIHelper.createSecondaryButton("Clear Form");

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> performCreateMenuItem());
        btnUpdate.addActionListener(e -> performUpdateMenuItem());
        btnDelete.addActionListener(e -> performDeleteMenuItem());
        btnClear.addActionListener(e -> clearMenuForm());

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        formCard.add(actionPanel, BorderLayout.SOUTH);
        return formCard;
    }

    private void loadMenuData() {
        try {
            List<MenuItem> items = menuDAO.getAllMenuItems();
            renderMenuItemsToTable(items);
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load menu: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderMenuItemsToTable(List<MenuItem> items) {
        menuTableModel.setRowCount(0);
        for (MenuItem item : items) {
            menuTableModel.addRow(new Object[]{
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

    private void applyMenuFilters() {
        String selectedDay = (String) filterDayCombo.getSelectedItem();
        String selectedMeal = (String) filterMealCombo.getSelectedItem();
        String keyword = txtSearch.getText().trim();

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

            renderMenuItemsToTable(list);
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Search/filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromTableRow(int rowIndex) {
        try {
            int id = (int) menuTableModel.getValueAt(rowIndex, 0);
            MenuItem item = menuDAO.getMenuItemById(id);
            if (item != null) {
                selectedMenuItemId = item.getId();
                lblFormMode.setText("Edit Item #" + selectedMenuItemId);
                comboFormDay.setSelectedItem(item.getDayOfWeek());
                comboFormMealType.setSelectedItem(item.getMealType());
                txtFormItemName.setText(item.getItemName());
                comboFormCategory.setSelectedItem(item.getCategory());
                txtFormExtraCost.setText(String.valueOf(item.getExtraCost()));
                txtFormDescription.setText(item.getDescription());
                chkFormAvailable.setSelected(item.isAvailable());

                btnAdd.setEnabled(false);
                btnUpdate.setEnabled(true);
                btnDelete.setEnabled(true);
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error fetching item details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearMenuForm() {
        selectedMenuItemId = -1;
        lblFormMode.setText("[+] Add New Menu Item");
        comboFormDay.setSelectedIndex(0);
        comboFormMealType.setSelectedIndex(0);
        txtFormItemName.setText("");
        comboFormCategory.setSelectedIndex(0);
        txtFormExtraCost.setText("0.0");
        txtFormDescription.setText("");
        chkFormAvailable.setSelected(true);
        menuTable.clearSelection();

        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    // CREATE
    private void performCreateMenuItem() {
        String name = txtFormItemName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Dish/Item Name!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFormItemName.requestFocus();
            return;
        }

        double extraCost = 0.0;
        try {
            extraCost = Double.parseDouble(txtFormExtraCost.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Extra Cost must be a valid number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtFormExtraCost.requestFocus();
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
                JOptionPane.showMessageDialog(this, "Menu Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearMenuForm();
                loadMenuData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert menu item into database.", "Insert Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // UPDATE
    private void performUpdateMenuItem() {
        if (selectedMenuItemId <= 0) {
            JOptionPane.showMessageDialog(this, "No item selected for update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtFormItemName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dish Name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double extraCost = 0.0;
        try {
            extraCost = Double.parseDouble(txtFormExtraCost.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for Extra Cost!", "Validation Error", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Menu Item updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                clearMenuForm();
                loadMenuData();
            } else {
                JOptionPane.showMessageDialog(this, "Item could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Database update error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DELETE
    private void performDeleteMenuItem() {
        if (selectedMenuItemId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete item #" + selectedMenuItemId + " (" + txtFormItemName.getText() + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = menuDAO.deleteMenuItem(selectedMenuItemId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Menu item deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearMenuForm();
                    loadMenuData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (MessDatabaseException e) {
                JOptionPane.showMessageDialog(this, "Database delete error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
