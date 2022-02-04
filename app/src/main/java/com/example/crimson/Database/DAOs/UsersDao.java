package com.example.crimson.Database.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.crimson.AppObjects.User;

import java.util.List;

public interface UsersDao {

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM user_table WHERE connected is 1 ORDER BY time ASC")
    LiveData<List<User>> getChatList();

    @Query("SELECT * FROM user_table ORDER BY name ASC")
    List<User> getAllUsers();

    @Query("SELECT * FROM user_table WHERE user_id=:user_ID LIMIT 1")
    User getUser(String user_ID);
}
