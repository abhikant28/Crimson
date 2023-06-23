package com.akw.crimson.Backend.Database;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;

import java.util.List;

public class TheViewModel extends AndroidViewModel {

    private final TheRepository repository;
    private Cursor chatMessages;
    private Cursor chatList;
    private final LiveData<List<User>> getChatList;
    private LiveData<List<Message>> getLiveMessagesList;
    private final LiveData<List<Message>> pendingMessagesList;
    private final LiveData<List<Message>> infoMessagesList;
    private final LiveData<List<Message>> receivedMessagesList;
    private final LiveData<List<User>> getAllUsers;

    public TheViewModel(@NonNull Application application) {
        super(application);
        repository = new TheRepository(application);
//        chatList=repository.getChatList();
        getChatList = repository.getGetChatList();
        getAllUsers = repository.getGetAllUsersList();
        pendingMessagesList = repository.getPendingMessagesList();
        infoMessagesList = repository.getInfoMessagesList();
        receivedMessagesList = repository.getReceivedMessagesList();
    }

    public void insertMessage(Message msg) {
        repository.insertMessage(msg);
    }
    public void deleteMessage(Message msg) {
        Log.d("THE VIEW MODEL::::::", "DELETING MESSAGE::");
        repository.deleteMessage(msg);
    }

    public void updateMessage(Message msg) {
        repository.updateMessage(msg);
    }

    public void updateAllMessage(List<Message> msg) {
        repository.updateAllMessage(msg);
    }

    public void insertAllMessage(List<Message> msg) {
        repository.insertAllMessage(msg);
    }
    public void deleteAllMessage(List<Message> msg) {
        repository.deleteAllMessage(msg);
    }

    public Cursor getChatMessages(String user_ID) {
        return repository.getChatMessages(user_ID);
    }

    public Message getMessage(String L_msg_ID) {
        return repository.getMessage(L_msg_ID);
    }

    public List<Message> searchInMessages(String query) {
        return repository.searchInMessage(query);
    }

    public List<Message> searchInUserMessages(String query, String id) {
        return repository.searchInUserMessage(query, id);
    }

    public LiveData<List<Message>> getLiveMessagesList(String userId) {
        getLiveMessagesList = repository.getLiveMessages(userId);
        return getLiveMessagesList;
    }
    public LiveData<List<Message>> getReceivedInfoMessagesList() {
        return infoMessagesList;
    }

    public LiveData<List<Message>> getUploadPendingMessagesList() {
        return pendingMessagesList;
    }

    public LiveData<List<Message>> getReceivedMessagesList() {
        return receivedMessagesList;
    }


    public void insertUser(User user) {
        repository.insertUser(user);
    }

    public void updateUser(User user) {
        repository.updateUser(user);
    }

    public void deleteUser(User user) {
        repository.deleteUser(user);
    }

    public LiveData<List<User>> getChatListUsers() {
        return repository.getGetChatList();
    }
    public List<User> getConnectedUsers() {
        return repository.getConnectedUsers();
    }

    public LiveData<List<User>> getAllUsersList() {
        return repository.getGetAllUsersList();
    }

    public User getUser(String user_ID) {
        return repository.getUser(user_ID);
    }

    public User getUserByNum(String num) {
        return repository.getUserByNum(num);
    }

    public List<User> searchUserByText(String query) {
        return repository.searchUserByText(query);
    }

    public List<Message> getStarredMessages() {
        return repository.getStarredMessage();
    }

    public List<Message> getUserMedia(String id) {
        return repository.getUserMedia(id);
    }

    public List<Message> getUserMediaByType(String id, int[] type) {
        return repository.getUserMediaByType(id, type);
    }

    public boolean checkForUserNum(String userNum) {
        return repository.checkForUserNum(userNum);
    }

    public List<Message> getStarredUserMessages(String userID){
            return repository.getStarredUserMessage(userID);
        }

    }
