package com.akw.crimson.Database.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.AppObjects.Message;

import java.util.List;

@Dao
public interface MessagesDao {

    @Insert
    void insert(Message msg);

    @Update
    void update(Message msg);

    @Query("SELECT * from messages_Table where user_id= :user_ID LIMIT 100")
    LiveData<List<Message>> getMessages(String user_ID);

    @Query("SELECT * from messages_Table where local_msg_ID=:L_msg_ID LIMIT 1")
    Message getMessage(String L_msg_ID);
}
