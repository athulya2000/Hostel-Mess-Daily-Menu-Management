package com.hostel.mess.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Abstract BaseEntity demonstrating Inheritance, Encapsulation, and Abstraction.
 * Part of B.Tech S3 OOP Concepts requirement.
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected Timestamp createdAt;

    // Default constructor
    public BaseEntity() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Overloaded constructor demonstrating Polymorphism
    public BaseEntity(int id) {
        this.id = id;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public BaseEntity(int id, Timestamp createdAt) {
        this.id = id;
        this.createdAt = (createdAt != null) ? createdAt : new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters (Encapsulation)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Abstract method to be overridden by subclasses (Abstraction & Polymorphism)
    public abstract String getEntitySummary();
}
