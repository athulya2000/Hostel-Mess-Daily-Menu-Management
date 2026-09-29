package com.hostel.mess.dao;

import java.util.List;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.Feedback;

public interface FeedbackDAO {
    boolean addFeedback(Feedback feedback) throws MessDatabaseException;
    List<Feedback> getAllFeedbacks() throws MessDatabaseException;
    double getAverageRating() throws MessDatabaseException;
    boolean deleteFeedback(int id) throws MessDatabaseException;
}
