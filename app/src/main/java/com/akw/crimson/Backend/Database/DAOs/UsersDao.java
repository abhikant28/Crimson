package com.akw.crimson.Backend.Database.DAOs;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.AppObjects.User;

import java.util.List;

@Dao
public interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);


    @Query("SELECT * FROM user_table ORDER BY time ASC")
    Cursor getAllUsersCursor();

    @Query("SELECT * FROM user_table WHERE connected is 1 ORDER BY date desc, lower(time) desc")
    LiveData<List<User>> getChatList();

    @Query("SELECT * FROM user_table ORDER BY displayName")
    LiveData<List<User>> getAllUsersList();

    @Query("SELECT * FROM user_table WHERE user_id=:user_ID LIMIT 1")
    User getUser(String user_ID);

    @Query("SELECT * FROM user_table WHERE phoneNumber=:num LIMIT 1")
    User getUserByNum(String num);

    @Query("SELECT * FROM user_table WHERE userName LIKE '%' || :query || '%' OR displayName LIKE'%' || :query || '%' OR phoneNumber LIKE'%' || :query || '%'")
    List<User> searchUserByText(String query);

    @Query("SELECT COUNT(*) > 0 FROM user_table WHERE phoneNumber = :value")
    boolean checkForNumber(String value);

}
