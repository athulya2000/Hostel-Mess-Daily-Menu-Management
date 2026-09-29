package com.hostel.mess.dao;

import java.util.List;
import com.hostel.mess.exception.MessDatabaseException;
import com.hostel.mess.model.Notice;

public interface NoticeDAO {
    boolean addNotice(Notice notice) throws MessDatabaseException;
    List<Notice> getAllNotices() throws MessDatabaseException;
    List<Notice> getActiveNotices() throws MessDatabaseException;
    boolean deleteNotice(int id) throws MessDatabaseException;
}
