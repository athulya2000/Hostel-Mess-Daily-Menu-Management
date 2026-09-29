package com.hostel.mess.dao;

import java.util.List;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.MealType;
import com.hostel.mess.model.MenuItem;

/**
 * MenuDAO Interface defining CRUD operations for Hostel Mess Menu.
 * 
 * Demonstrates:
 * - Interface Definition & Abstraction (Syllabus Module 3)
 * - Polymorphism via Method Overloading (getAllMenuItems vs getMenuItemsByDay vs getMenuItems)
 * - Passing and Returning Objects
 */
public interface MenuDAO {

    // CREATE operation
    boolean addMenuItem(MenuItem item) throws MessDatabaseException;

    // READ operations (Overloaded for Polymorphism)
    MenuItem getMenuItemById(int id) throws MessDatabaseException;

    List<MenuItem> getAllMenuItems() throws MessDatabaseException;

    List<MenuItem> getMenuItemsByDay(String dayOfWeek) throws MessDatabaseException;

    List<MenuItem> getMenuItems(String dayOfWeek, MealType mealType) throws MessDatabaseException;

    List<MenuItem> searchMenuItems(String keyword) throws MessDatabaseException;

    // UPDATE operation
    boolean updateMenuItem(MenuItem item) throws MessDatabaseException;

    // DELETE operation
    boolean deleteMenuItem(int id) throws MessDatabaseException;

    // Aggregate statistics
    int getTotalItemsCount() throws MessDatabaseException;
}
