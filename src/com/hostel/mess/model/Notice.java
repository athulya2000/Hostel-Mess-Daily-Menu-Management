package com.hostel.mess.model;

import java.sql.Timestamp;

/**
 * Model representing Mess Announcements / Notices posted by Admin.
 * Extends BaseEntity.
 */
public class Notice extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;
    private String priority; // Normal, High, Urgent
    private boolean active;

    public Notice() {
        super();
        this.priority = "Normal";
        this.active = true;
    }

    public Notice(String title, String content, String priority, boolean active) {
        super();
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.active = active;
    }

    public Notice(int id, String title, String content, String priority, boolean active, Timestamp createdAt) {
        super(id, createdAt);
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getEntitySummary() {
        return String.format("[%s] %s: %s", priority, title, content);
    }
}
