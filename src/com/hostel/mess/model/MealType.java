package com.hostel.mess.model;

/**
 * Enumeration representing Hostel Mess Meal Types.
 */
public enum MealType {
    BREAKFAST("Breakfast", "07:30 AM - 09:30 AM"),
    LUNCH("Lunch", "12:30 PM - 02:30 PM"),
    SNACKS("Evening Tea & Snacks", "04:30 PM - 05:45 PM"),
    DINNER("Dinner", "07:45 PM - 09:30 PM");

    private final String displayName;
    private final String defaultTiming;

    MealType(String displayName, String defaultTiming) {
        this.displayName = displayName;
        this.defaultTiming = defaultTiming;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultTiming() {
        return defaultTiming;
    }

    public static MealType fromString(String text) {
        if (text == null) return BREAKFAST;
        for (MealType mt : MealType.values()) {
            if (mt.name().equalsIgnoreCase(text.trim()) || mt.displayName.equalsIgnoreCase(text.trim())) {
                return mt;
            }
        }
        return BREAKFAST;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
