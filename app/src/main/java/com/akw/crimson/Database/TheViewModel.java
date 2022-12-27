package com.akw.crimson.Database;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.Profile;
import com.akw.crimson.AppObjects.User;

import java.util.List;

public class TheViewModel extends AndroidViewModel {

    private TheRepository repository;
    private Cursor chatMessages;
    private Cursor chatList;
    private LiveData<List<User>> getChatList;
    private LiveData<List<Message>> pendingMessagesList;
    private  LiveData<List<User>> getAllUsers;

    public TheViewModel(@NonNull Application application) {
        super(application);
        repository= new TheRepository(application);
//        chatList=repository.getChatList();
        getChatList =repository.getGetChatList();
       getAllUsers = repository.getGetAllUsersList();
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
    public LiveData<List<User>> getChatListUsers(){
        return repository.getGetChatList();
    }
    public LiveData<List<User>> getAllUsersList(){
        return repository.getGetAllUsersList();
    }
    public User getUser(String user_ID){
        return repository.getUser(user_ID);
    }
    public boolean checkForUserNum(String userNum){
        return repository.checkForUserNum(userNum);
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