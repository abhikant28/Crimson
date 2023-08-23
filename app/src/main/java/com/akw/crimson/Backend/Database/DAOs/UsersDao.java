package com.akw.crimson.Backend.Database.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;

import java.util.List;

@Dao
public interface UsersDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);


//    @Query("SELECT * FROM user_table ORDER BY time ASC")
//    Cursor getAllUsersCursor();

    @Query("SELECT * FROM user_table WHERE known is 1 ORDER BY updateTime desc")
    LiveData<List<User>> getChatList();

    @Query("SELECT * FROM user_table WHERE connected is 1 AND type is "+ Constants.User.USER_TYPE_USER+" ORDER BY updateTime desc")
    List<User> getConnectedUsers();

    @Query("SELECT * FROM user_table WHERE type="+ Constants.User.USER_TYPE_USER+" ORDER BY displayName")
    LiveData<List<User>> getAllUsersListLive();

    @Query("SELECT * FROM user_table WHERE user_id=:userID")
    LiveData<User> getUserLive(String userID);

    @Query("SELECT * FROM user_table WHERE type="+ Constants.User.USER_TYPE_USER+" ORDER BY displayName")
    List<User> getAllUsersList();


    @Query("SELECT * FROM user_table WHERE user_id=:user_ID LIMIT 1")
    User getUser(String user_ID);

    @Query("SELECT * FROM user_table WHERE phoneNumber=:num LIMIT 1")
    User getUserByNum(String num);

    @Query("SELECT * FROM user_table WHERE userName LIKE '%' || :query || '%' OR displayName LIKE'%' || :query || '%' OR phoneNumber LIKE'%' || :query || '%'")
    List<User> searchUserByText(String query);

    @Query("SELECT COUNT(*) > 0 FROM user_table WHERE phoneNumber = :value")
    boolean checkForNumber(String value);

    @Query("SELECT * FROM messages_Table WHERE user_id=:user_ID AND media is 1 AND ( mediaType is 1 OR mediaType is 2 OR mediaType is 9 OR mediaType is 10) ORDER BY sentTime desc")
    List<Message> getUserMedia(String user_ID);

    @Query("SELECT * FROM messages_Table WHERE user_id=:user_ID AND media is 1 AND mediaType IN(:type) ORDER BY sentTime desc")
    List<Message> getUserMediaByType(String user_ID, int[] type);

}
