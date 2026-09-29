package com.hostel.mess.exception;

/**
 * Custom Checked Exception for input validation errors.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
