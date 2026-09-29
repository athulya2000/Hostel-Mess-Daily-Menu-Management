package com.hostel.mess;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.hostel.mess.database.DatabaseManager;
import com.hostel.mess.view.MainAppFrame;

/**
 * Main Application Entry Point.
 * 
 * Demonstrates:
 * - Application bootstrap
 * - Event Dispatch Thread (EDT) execution using SwingUtilities.invokeLater
 * - System Look and Feel integration
 */
public class AppMain {

    public static void main(String[] args) {
        // Apply modern system look and feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set look and feel: " + e.getMessage());
            }
        }

        // Initialize Database in background/startup
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            System.out.println("Hostel Mess Database (" + db.getActiveDatabaseType() + ") initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }

        // Start HTML/CSS Web Client Server in background
        try {
            com.hostel.mess.web.MessWebServer.startServer();
        } catch (Exception e) {
            System.err.println("Web server note: " + e.getMessage());
        }

        // Check if running on cloud server or headless environment (e.g. Render / Linux Docker)
        boolean serverOnly = java.awt.GraphicsEnvironment.isHeadless();
        for (String arg : args) {
            if ("--server-only".equalsIgnoreCase(arg) || "--headless".equalsIgnoreCase(arg)) {
                serverOnly = true;
                break;
            }
        }

        if (serverOnly) {
            System.out.println("Running in Server-Only / Headless Cloud Mode. GUI skipped.");
            try {
                // Keep the server alive indefinitely
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        // Launch unified 2-tab GUI safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainAppFrame app = new MainAppFrame();
            app.setVisible(true);
        });
    }
}
