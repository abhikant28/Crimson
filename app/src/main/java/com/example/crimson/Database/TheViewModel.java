package com.example.crimson.Database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.crimson.AppObjects.Message;
import com.example.crimson.AppObjects.Profile;
import com.example.crimson.AppObjects.User;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TheViewModel extends AndroidViewModel {

    private TheRepository repository;
    private LiveData<List<Message>> chatMessages;
    private LiveData<List<User>> chatList;

    public TheViewModel(@NonNull Application application) {
        super(application);
        repository= new TheRepository(application);
        chatList=repository.getChatList();
    }

    public void insertMessage(Message msg){
        repository.insertMessage(msg);
    }
    public void updateMessage(Message msg){
        repository.updateMessage(msg);
    }
    public LiveData<List<Message>> getChatMessages(String user_ID){
        return repository.getChatMessages(user_ID);
    }
    public Message getMessage(String L_msg_ID){
        return repository.getMessage(L_msg_ID);
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
    public LiveData<List<User>> getChatList(){
        return chatList;
    }
    public List<User> getAllUsers(){
        return repository.getAllUsers();
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
    public List<Profile> getAllProfiles(){
        return repository.getAllProfiles();
    }
    public Profile getProfile(String user_id){
        return repository.getProfile(user_id);
    }


    }
}