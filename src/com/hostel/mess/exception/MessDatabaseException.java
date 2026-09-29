package com.hostel.mess.exception;

/**
 * Custom Checked Exception for Database-related errors.
 * Demonstrates: Custom Exception creation, Inheritance (extends Exception),
 * throw, throws, and constructor chaining with super.
 */
public class MessDatabaseException extends Exception {
    private static final long serialVersionUID = 1L;

    public MessDatabaseException(String message) {
        super(message);
    }

    public MessDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
