package com.akw.crimson.Backend.Database.DAOs;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.Backend.AppObjects.Message;

import java.util.List;

@Dao
public interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message msg);

    @Update
    void update(Message msg);

    @Update
    void updateAll(List<Message> messages);

//    @Query("SELECT * from messages_Table where user_id= :user_id LIMIT 100")
//    LiveData<List<Message>> getMessages(String user_id);
    @Query("SELECT * FROM MESSAGES_TABLE WHERE status=0")
    LiveData<List<Message>> pendingMessages();

    @Query("SELECT * FROM MESSAGES_TABLE WHERE status=2")
    LiveData<List<Message>> receivedMessages();

    @Query("SELECT * from messages_Table where local_msg_ID=:L_msg_ID LIMIT 1")
    Message getMessage(String L_msg_ID);

    @Query("SELECT * from messages_Table where user_id= :user_ID")
    Cursor getMessages(String user_ID);

    @Query("SELECT * from messages_Table where msg LIKE'%' || :query || '%'")
    List<Message> searchInMessage(String query);

    @Query("SELECT * from messages_Table where user_id=:userID AND msg LIKE'%' || :query || '%'")
    List<Message> searchInUserMessages(String userID, String query);
}
