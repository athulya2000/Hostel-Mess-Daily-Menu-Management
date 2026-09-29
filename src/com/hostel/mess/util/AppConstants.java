package com.hostel.mess.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Global Constants for the application.
 * Demonstrates: Static members, final variables, Encapsulation with private constructor.
 */
public final class AppConstants {

    // Private constructor to prevent instantiation (Utility Class Pattern)
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String APP_TITLE = "Hostel Mess Daily Menu Register";
    public static final String APP_VERSION = "v1.0";

    // Default Admin Credentials
    public static final String DEFAULT_ADMIN_USER = "admin@gmail.com";
    public static final String DEFAULT_ADMIN_PASS = "admin123";

    // UI Theme Colors
    public static final Color COLOR_PRIMARY = new Color(28, 56, 121);      // Deep Royal Blue
    public static final Color COLOR_PRIMARY_LIGHT = new Color(53, 89, 179);
    public static final Color COLOR_ACCENT = new Color(245, 130, 32);       // Warm Amber/Orange
    public static final Color COLOR_BG_LIGHT = new Color(248, 249, 252);    // Clean Soft Grey
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_DARK = new Color(33, 37, 41);
    public static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    public static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    public static final Color COLOR_DANGER = new Color(220, 53, 69);
    public static final Color COLOR_WARNING = new Color(255, 193, 7);

    // Common Fonts
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // Days of the Week
    public static final String[] DAYS_OF_WEEK = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    // Meal Categories
    public static final String[] CATEGORIES = {
        "Vegetarian", "Non-Vegetarian", "Special Feast", "Beverage", "Snacks"
    };
}
