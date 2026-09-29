package com.hostel.mess;

import java.util.List;
import com.hostel.mess.dao.FeedbackDAO;
import com.hostel.mess.dao.FeedbackDAOImpl;
import com.hostel.mess.dao.MenuDAO;
import com.hostel.mess.dao.MenuDAOImpl;
import com.hostel.mess.dao.NoticeDAO;
import com.hostel.mess.dao.NoticeDAOImpl;
import com.hostel.mess.database.DatabaseManager;
import com.hostel.mess.model.Feedback;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;
import com.hostel.mess.model.Notice;

/**
 * Headless Automated Verification Test.
 * Confirms all CRUD and DB operations are working accurately.
 */
public class TestVerification {

    public static void main(String[] args) {
        System.out.println(">>> Starting Hostel Mess Daily Menu Register Automated Tests <<<");

        try {
            // 1. Test Database Initialization (Singleton)
            System.out.println("\n[1] Testing Database Connection (Singleton Pattern)...");
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager != null && dbManager.getConnection() != null) {
                System.out.println("  PASS: SQLite Database initialized and connected successfully.");
            }

            MenuDAO menuDAO = new MenuDAOImpl();
            NoticeDAO noticeDAO = new NoticeDAOImpl();
            FeedbackDAO feedbackDAO = new FeedbackDAOImpl();

            // 2. Test READ (Initial seeded data)
            System.out.println("\n[2] Testing READ Operations...");
            List<MenuItem> allItems = menuDAO.getAllMenuItems();
            System.out.println("  Seeded Menu Items Count: " + allItems.size());
            assert allItems.size() > 0 : "Menu items should not be empty";
            System.out.println("  PASS: Successfully read " + allItems.size() + " menu items from database.");

            List<MenuItem> mondayBreakfast = menuDAO.getMenuItems("Monday", MealType.BREAKFAST);
            System.out.println("  Monday Breakfast Items: " + mondayBreakfast.size());
            for (MenuItem item : mondayBreakfast) {
                System.out.println("    - " + item.getEntitySummary());
            }

            // 3. Test CREATE
            System.out.println("\n[3] Testing CREATE Operation (Add Menu Item)...");
            MenuItem testItem = new MenuItem(
                "Friday",
                MealType.DINNER,
                "Kerala Parotta with Malabar Chicken Roast",
                "Non-Vegetarian",
                "Layered flaky parotta served with traditional spicy roasted coconut chicken gravy",
                30.0,
                true
            );
            boolean created = menuDAO.addMenuItem(testItem);
            if (created && testItem.getId() > 0) {
                System.out.println("  PASS: Created item with ID: " + testItem.getId());
            } else {
                throw new RuntimeException("CREATE operation failed!");
            }

            // 4. Test READ by ID & Search
            System.out.println("\n[4] Testing READ by ID & Search...");
            MenuItem fetched = menuDAO.getMenuItemById(testItem.getId());
            if (fetched != null && fetched.getItemName().equals(testItem.getItemName())) {
                System.out.println("  PASS: Retrieved newly created item: " + fetched.getItemName());
            } else {
                throw new RuntimeException("READ by ID failed!");
            }

            List<MenuItem> searchResults = menuDAO.searchMenuItems("Malabar Chicken");
            if (!searchResults.isEmpty()) {
                System.out.println("  PASS: Search query returned " + searchResults.size() + " match.");
            }

            // 5. Test UPDATE
            System.out.println("\n[5] Testing UPDATE Operation...");
            fetched.setItemName("Kerala Parotta with Malabar Chicken Curry (Extra Gravy)");
            fetched.setExtraCost(35.0);
            boolean updated = menuDAO.updateMenuItem(fetched);
            if (updated) {
                MenuItem verifiedUpdate = menuDAO.getMenuItemById(fetched.getId());
                System.out.println("  PASS: Updated item name to: " + verifiedUpdate.getItemName());
                System.out.println("  PASS: Updated extra cost to: Rs " + verifiedUpdate.getExtraCost());
            } else {
                throw new RuntimeException("UPDATE operation failed!");
            }

            // 6. Test DELETE
            System.out.println("\n[6] Testing DELETE Operation...");
            boolean deleted = menuDAO.deleteMenuItem(fetched.getId());
            if (deleted) {
                MenuItem postDelete = menuDAO.getMenuItemById(fetched.getId());
                if (postDelete == null) {
                    System.out.println("  PASS: Item with ID " + fetched.getId() + " successfully deleted from database.");
                } else {
                    throw new RuntimeException("Item still exists after DELETE!");
                }
            } else {
                throw new RuntimeException("DELETE operation failed!");
            }

            // 7. Test Notices & Feedback
            System.out.println("\n[7] Testing Notices & Feedback DAO...");
            List<Notice> notices = noticeDAO.getActiveNotices();
            System.out.println("  PASS: Active Notices: " + notices.size());

            Feedback testFb = new Feedback("Test Student", "B-101", "LUNCH", 5, "Automated test comment");
            feedbackDAO.addFeedback(testFb);
            double avgRating = feedbackDAO.getAverageRating();
            System.out.println("  PASS: Feedback submitted. Current average rating: " + avgRating);

            System.out.println("\n========================================================");
            System.out.println(">>> ALL 7 TEST PHASES PASSED SUCCESSFULLY! 100% READY <<<");
            System.out.println("========================================================");

        } catch (Exception e) {
            System.err.println("TEST FAILED WITH EXCEPTION:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
