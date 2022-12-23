package com.akw.crimson.Database;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.Profile;
import com.akw.crimson.AppObjects.User;

import java.util.ArrayList;
import java.util.List;

public class TheViewModel extends AndroidViewModel {

    private TheRepository repository;
    private Cursor chatMessages;
    private Cursor chatList;
    private LiveData<List<User>> allUsers;
    private LiveData<List<Message>> pendingMessagesList;

    public TheViewModel(@NonNull Application application) {
        super(application);
        repository= new TheRepository(application);
//        chatList=repository.getChatList();
        allUsers=repository.getAllUserList();
        pendingMessagesList= repository.getPendingMessagesList();
    }

    public void insertMessage(Message msg){
        repository.insertMessage(msg);
    }
    public void updateMessage(Message msg){
        repository.updateMessage(msg);
    }
    public Cursor getChatMessages(String user_ID){
        return repository.getChatMessages(user_ID);
    }
    public Message getMessage(String L_msg_ID){
        return repository.getMessage(L_msg_ID);
    }
    public LiveData<List<Message>> getPendingMessagesList() {
        return pendingMessagesList;
    }

    public void insertUser(User user){
        repository.insertUser(user);
    }
    public void updateUser(User user){
        repository.updateUser(user);
    }
    public void deleteUser(User user){
        repository.deleteUser(user);
    }
    public Cursor getChatList(){
        return repository.getChatList();
    }
    public LiveData<List<User>> getAllUsersList(){
        return repository.getAllUserList();
    }
    public User getUser(String user_ID){
        return repository.getUser(user_ID);
    }

    public void insertProfile(Profile profile){
        repository.insertProfile(profile);
    }
    public void deleteProfile(Profile profile){
        repository.deleteProfile(profile);
    }
    public void updateProfile(Profile profile){
        repository.updateProfile(profile);
    }
    public Cursor getAllProfiles(){
        return repository.getAllProfiles();
    }
    public Profile getProfile(String user_id){
        return repository.getProfile(user_id);
    }

}