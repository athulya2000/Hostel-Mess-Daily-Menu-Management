package com.hostel.mess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.hostel.mess.dao.MenuDAO;
import com.hostel.mess.dao.MenuDAOImpl;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;
import com.hostel.mess.util.AppConstants;
import com.hostel.mess.util.UIHelper;

/**
 * Client-Side Daily Menu Board (Student Portal).
 * Displays the daily meal list (Breakfast, Lunch, Snacks, Dinner)
 * with Day switching and quick search.
 */
public class StudentPortalFrame extends JFrame {

    private MenuDAO menuDAO;
    private String selectedDay;
    private JPanel mealCardsContainer;
    private JLabel lblCurrentDayBanner;
    private JTextField txtSearch;

    public StudentPortalFrame() {
        initDAO();
        LocalDate today = LocalDate.now();
        this.selectedDay = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        initUI();
        loadDailyMenu(selectedDay);
    }

    private void initDAO() {
        try {
            this.menuDAO = new MenuDAOImpl();
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        setTitle(AppConstants.APP_TITLE + " - Daily Menu Board (Client View)");
        setSize(980, 680);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Banner
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Center Content Area: Day Navigation + 4 Meal Cards
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(AppConstants.COLOR_BG_LIGHT);
        mainContent.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Day Navigation Bar
        JPanel navBar = createDayNavigationBar();
        mainContent.add(navBar, BorderLayout.NORTH);

        // 4 Meal Cards in 2x2 Grid
        mealCardsContainer = new JPanel(new GridLayout(2, 2, 14, 14));
        mealCardsContainer.setBackground(AppConstants.COLOR_BG_LIGHT);

        JScrollPane mealScroll = new JScrollPane(mealCardsContainer);
        mealScroll.setBorder(null);
        mealScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainContent.add(mealScroll, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = UIHelper.createHeaderBanner(
                "Hostel Mess - Daily Menu Board",
                "Daily breakfast, lunch, snacks, and dinner schedules"
        );

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton btnAdmin = UIHelper.createSecondaryButton("Admin Login");
        btnAdmin.addActionListener(e -> {
            AdminLoginDialog login = new AdminLoginDialog(this);
            login.setVisible(true);
            if (login.isAuthenticated()) {
                dispose();
                new AdminDashboardFrame().setVisible(true);
            }
        });

        rightControls.add(btnAdmin);
        header.add(rightControls, BorderLayout.EAST);
        return header;
    }

    private JPanel createDayNavigationBar() {
        JPanel navPanel = new JPanel(new BorderLayout(8, 8));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Days Buttons Panel
        JPanel daysBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        daysBtnRow.setOpaque(false);

        lblCurrentDayBanner = new JLabel("Day: " + selectedDay);
        lblCurrentDayBanner.setFont(AppConstants.FONT_BODY_BOLD);
        lblCurrentDayBanner.setForeground(AppConstants.COLOR_PRIMARY);
        lblCurrentDayBanner.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        daysBtnRow.add(lblCurrentDayBanner);

        for (String day : AppConstants.DAYS_OF_WEEK) {
            JButton btnDay = new JButton(day);
            btnDay.setFont(AppConstants.FONT_BODY);
            btnDay.setFocusPainted(false);
            btnDay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            if (day.equalsIgnoreCase(selectedDay)) {
                btnDay.setBackground(AppConstants.COLOR_PRIMARY);
                btnDay.setForeground(Color.WHITE);
            } else {
                btnDay.setBackground(new Color(240, 243, 248));
                btnDay.setForeground(AppConstants.COLOR_TEXT_DARK);
            }

            btnDay.addActionListener(e -> {
                selectedDay = day;
                lblCurrentDayBanner.setText("Day: " + selectedDay);
                for (java.awt.Component c : daysBtnRow.getComponents()) {
                    if (c instanceof JButton) {
                        JButton b = (JButton) c;
                        if (b.getText().equalsIgnoreCase(selectedDay)) {
                            b.setBackground(AppConstants.COLOR_PRIMARY);
                            b.setForeground(Color.WHITE);
                        } else {
                            b.setBackground(new Color(240, 243, 248));
                            b.setForeground(AppConstants.COLOR_TEXT_DARK);
                        }
                    }
                }
                loadDailyMenu(selectedDay);
            });

            daysBtnRow.add(btnDay);
        }

        navPanel.add(daysBtnRow, BorderLayout.WEST);

        // Quick Search on the right
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        txtSearch = UIHelper.createStyledTextField(12);
        JButton btnSearch = UIHelper.createPrimaryButton("Search Dish");
        btnSearch.setPreferredSize(new Dimension(110, 32));

        btnSearch.addActionListener(e -> {
            String kw = txtSearch.getText().trim();
            if (!kw.isEmpty()) {
                searchDishes(kw);
            } else {
                loadDailyMenu(selectedDay);
            }
        });

        searchPanel.add(new JLabel("Quick Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        navPanel.add(searchPanel, BorderLayout.EAST);
        return navPanel;
    }

    private void loadDailyMenu(String day) {
        mealCardsContainer.removeAll();
        try {
            List<MenuItem> allDayItems = menuDAO.getMenuItemsByDay(day);

            for (MealType mt : MealType.values()) {
                JPanel card = createMealCard(mt, allDayItems);
                mealCardsContainer.add(card);
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load menu: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        mealCardsContainer.revalidate();
        mealCardsContainer.repaint();
    }

    private void searchDishes(String keyword) {
        mealCardsContainer.removeAll();
        try {
            List<MenuItem> results = menuDAO.searchMenuItems(keyword);
            if (results.isEmpty()) {
                JPanel emptyPanel = new JPanel(new BorderLayout());
                emptyPanel.setBackground(Color.WHITE);
                JLabel lblEmpty = new JLabel("No dishes found matching: '" + keyword + "'", JLabel.CENTER);
                lblEmpty.setFont(AppConstants.FONT_HEADER_MEDIUM);
                lblEmpty.setForeground(AppConstants.COLOR_TEXT_MUTED);
                emptyPanel.add(lblEmpty, BorderLayout.CENTER);
                mealCardsContainer.setLayout(new BorderLayout());
                mealCardsContainer.add(emptyPanel, BorderLayout.CENTER);
            } else {
                mealCardsContainer.setLayout(new GridLayout(2, 2, 14, 14));
                for (MealType mt : MealType.values()) {
                    JPanel card = createMealCard(mt, results);
                    mealCardsContainer.add(card);
                }
            }
        } catch (MessDatabaseException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        mealCardsContainer.revalidate();
        mealCardsContainer.repaint();
    }

    private JPanel createMealCard(MealType mealType, List<MenuItem> items) {
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
                dishesList.add(createDishRow(item));
                dishesList.add(Box.createVerticalStrut(6));
            }
        }

        if (count == 0) {
            JLabel lblNoMenu = new JLabel("No menu items scheduled for this meal.");
            lblNoMenu.setFont(AppConstants.FONT_BODY);
            lblNoMenu.setForeground(AppConstants.COLOR_TEXT_MUTED);
            dishesList.add(lblNoMenu);
        }

        JScrollPane scroll = new JScrollPane(dishesList);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDishRow(MenuItem item) {
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

        if (!item.isAvailable()) {
            JLabel lblUnavail = new JLabel("[Unavailable]");
            lblUnavail.setFont(AppConstants.FONT_SMALL);
            lblUnavail.setForeground(AppConstants.COLOR_DANGER);
            badgePanel.add(lblUnavail);
        }

        row.add(badgePanel, BorderLayout.EAST);
        return row;
    }
}
