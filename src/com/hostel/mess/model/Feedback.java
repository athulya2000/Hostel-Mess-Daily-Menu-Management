package com.hostel.mess.model;

import java.sql.Timestamp;

/**
 * Model representing Student / Resident Mess Feedback and Ratings.
 * Extends BaseEntity.
 */
public class Feedback extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String studentName;
    private String roomNumber;
    private String mealType;
    private int rating; // 1 to 5 stars
    private String comments;

    public Feedback() {
        super();
        this.rating = 5;
    }

    public Feedback(String studentName, String roomNumber, String mealType, int rating, String comments) {
        super();
        this.studentName = studentName;
        this.roomNumber = roomNumber;
        this.mealType = mealType;
        this.rating = rating;
        this.comments = comments;
    }

    public Feedback(int id, String studentName, String roomNumber, String mealType, int rating, String comments, Timestamp createdAt) {
        super(id, createdAt);
        this.studentName = studentName;
        this.roomNumber = roomNumber;
        this.mealType = mealType;
        this.rating = rating;
        this.comments = comments;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = Math.max(1, Math.min(5, rating));
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String getEntitySummary() {
        return String.format("%s (Room %s) - %s: %d/5 stars", studentName, roomNumber, mealType, rating);
    }
}
