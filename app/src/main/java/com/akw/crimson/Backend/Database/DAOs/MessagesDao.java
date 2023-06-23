package com.akw.crimson.Backend.Database.DAOs;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Constants;

import java.util.List;

@Dao
public interface MessagesDao {

//    @Query("CREATE TRIGGER IF NOT EXISTS update_last_msg AFTER INSERT ON message_table\n" +
//            "FOR EACH ROW\n" +
//            "WHEN NEW.messageType = " +Constants.Message.MESSAGE_TYPE_TEXT+
//            "BEGIN\n" +
//            "    UPDATE user_table\n" +
//            "    SET last_msg = SUBSTR(NEW.msg, 1, 10)\n" +
//            "    WHERE ROWID = 1;\n" +
//            "END;")

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Message> messages);

    @Update
    void update(Message message);

    @Update
    void updateAll(List<Message> messages);

    @Delete
    void delete(Message message);

    @Delete
    void deleteAll(List<Message> messages);

    //    @Query("SELECT * from messages_Table where user_id= :user_id LIMIT 100")
//    LiveData<List<Message>> getMessages(String user_id);
    @Query("SELECT * FROM MESSAGES_TABLE WHERE msgType=" + Constants.Message.MESSAGE_TYPE_INFO + " AND status=" + Constants.Message.MESSAGE_STATUS_RECEIVED)
    LiveData<List<Message>> receivedInfoMessages();

    @Query("SELECT * FROM MESSAGES_TABLE WHERE status=" + Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD)
    LiveData<List<Message>> pendingMessages();

    @Query("SELECT * FROM MESSAGES_TABLE WHERE msgType=" + Constants.Message.MESSAGE_TYPE_TEXT + " AND status=" + Constants.Message.MESSAGE_STATUS_RECEIVED)
    LiveData<List<Message>> receivedMessages();

    @Query("SELECT * from messages_Table where msg_ID=:msgID LIMIT 1")
    Message getMessage(String msgID);

    @Query("SELECT * FROM messages_Table WHERE user_id = :user_ID AND msgType=" + Constants.Message.MESSAGE_TYPE_TEXT + " ORDER BY CASE WHEN self = 1 THEN sentTime ELSE receivedTime END ASC")
    Cursor getMessages(String user_ID);

    @Query("SELECT * from messages_Table where starred is 1")
    List<Message> getStarredMessages();

    @Query("SELECT * from messages_Table where starred is 1 AND user_id= :userID")
    List<Message> getStarredUserMessages(String userID);

    @Query("SELECT * from messages_Table where user_id= :user_ID AND msgType=" + Constants.Message.MESSAGE_TYPE_TEXT)
    LiveData<List<Message>> getLiveMessages(String user_ID);

    @Query("SELECT * from messages_Table where msg LIKE'%' || :query || '%'")
    List<Message> searchInMessage(String query);

    @Query("SELECT * from messages_Table where user_id=:userID AND msg LIKE'%' || :query || '%'")
    List<Message> searchInUserMessages(String userID, String query);

}
