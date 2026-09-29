package com.hostel.mess.model;

import java.sql.Timestamp;

/**
 * Model representing a daily menu item in the hostel mess.
 * Demonstrates: Inheritance (extends BaseEntity), Encapsulation, Constructor Overloading,
 * and Method Overriding.
 */
public class MenuItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String dayOfWeek;       // Monday, Tuesday, ... Sunday
    private MealType mealType;      // BREAKFAST, LUNCH, SNACKS, DINNER
    private String itemName;        // e.g., Masala Dosa, Chicken Curry
    private String category;        // Vegetarian, Non-Vegetarian, Special Feast, Beverage
    private String description;     // Side dishes, ingredients or preparation notes
    private double extraCost;       // 0.00 for regular mess, or special extra amount
    private boolean isAvailable;    // Availability toggle

    // Default Constructor
    public MenuItem() {
        super();
        this.isAvailable = true;
        this.extraCost = 0.0;
        this.mealType = MealType.BREAKFAST;
        this.dayOfWeek = "Monday";
        this.category = "Vegetarian";
        this.description = "";
    }

    // Overloaded Constructor without ID (used for new insertions)
    public MenuItem(String dayOfWeek, MealType mealType, String itemName, String category,
                    String description, double extraCost, boolean isAvailable) {
        super();
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.extraCost = extraCost;
        this.isAvailable = isAvailable;
    }

    // Overloaded Constructor with ID (used when reading from database)
    public MenuItem(int id, String dayOfWeek, MealType mealType, String itemName, String category,
                    String description, double extraCost, boolean isAvailable, Timestamp createdAt) {
        super(id, createdAt);
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.extraCost = extraCost;
        this.isAvailable = isAvailable;
    }

    // Encapsulation: Getters and Setters with basic validation
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            this.dayOfWeek = dayOfWeek.trim();
        }
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = (mealType != null) ? mealType : MealType.BREAKFAST;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = (itemName != null) ? itemName.trim() : "";
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category != null) ? category.trim() : "Vegetarian";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    public double getExtraCost() {
        return extraCost;
    }

    public void setExtraCost(double extraCost) {
        this.extraCost = Math.max(0.0, extraCost);
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String getEntitySummary() {
        return String.format("[%s - %s] %s (%s)", dayOfWeek, mealType.getDisplayName(), itemName, category);
    }

    @Override
    public String toString() {
        return itemName + " [" + category + "]";
    }
}
