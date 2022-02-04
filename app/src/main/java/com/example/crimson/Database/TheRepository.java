package com.example.crimson.Database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.crimson.AppObjects.Message;
import com.example.crimson.AppObjects.Profile;
import com.example.crimson.AppObjects.User;
import com.example.crimson.Database.DAOs.MessagesDao;
import com.example.crimson.Database.DAOs.ProfileDao;
import com.example.crimson.Database.DAOs.UsersDao;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TheRepository {

    private ProfileDao profileDao;
    private UsersDao usersDao;
    private MessagesDao messagesDao;

    private LiveData<List<User>> chatList;
    private LiveData<List<Message>> chatMessages;

    public TheRepository(Application application){
        TheDatabase database = TheDatabase.getInstance(application);
        profileDao = database.profileDao();
        usersDao = database.usersDao();
        messagesDao =database.messagesDao();
        chatList= usersDao.getChatList();
    }

    public void insertMessage(Message msg){
        new InsertMessageAsyncTask(messagesDao).execute(msg);
    }
    public void updateMessage(Message msg){
        new UpdateMessageAsyncTask(messagesDao).execute(msg);
    }
    public LiveData<List<Message>> getChatMessages(String user_ID){
        return messagesDao.getMessages(user_ID);
    }
    public Message getMessage(String L_msg_ID){
        try {
             return new GetMessageAsyncTask(messagesDao).execute(L_msg_ID).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertUser(User user){
        new InsertUserAsyncTask(usersDao).execute(user);
    }
    public void updateUser(User user){
        new UpdateUserAsyncTask(usersDao).execute(user);
    }
    public void deleteUser(User user){
        new DeleteUserAsyncTask(usersDao).execute(user);
    }
    public LiveData<List<User>> getChatList(){
        return chatList;
    }
    public List<User> getAllUsers(){
        try {
            return new GetAllUserAsyncTask(usersDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public User getUser(String user_ID){
        try {
            return new GetUserAsyncTask(usersDao).execute(user_ID).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertProfile(Profile profile){
        new InsertProfileAsyncTask(profileDao).execute(profile);
    }
    public void deleteProfile(Profile profile){
        new DeleteProfileAsyncTask(profileDao).execute(profile);
    }
    public void updateProfile(Profile profile){
        new UpdateProfileAsyncTask(profileDao).execute(profile);
    }
    public List<Profile> getAllProfiles(){
        try {
            return new GetAllProfileAsyncTask(profileDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Profile getProfile(String user_id){
        try {
            return new GetProfileAsyncTask(profileDao).execute(user_id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class InsertMessageAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessagesDao messagesDao;

        private InsertMessageAsyncTask(MessagesDao msgDao){
            this.messagesDao=msgDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.insert(messages[0]);
            return null;
        }
    }
    private static class UpdateMessageAsyncTask extends AsyncTask<Message,Void,Void>{
        private MessagesDao messagesDao;

        private UpdateMessageAsyncTask(MessagesDao msgDao){
            this.messagesDao=msgDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.insert(messages[0]);
            return null;
        }
    }
    private static class GetMessageAsyncTask extends AsyncTask<String,Void,Message>{
        private MessagesDao messagesDao;

        private GetMessageAsyncTask(MessagesDao msgDao){
            this.messagesDao=msgDao;
        }

        @Override
        protected Message doInBackground(String... L_msg_ID) {
            return messagesDao.getMessage(L_msg_ID[0]);
        }
    }

    private static class InsertUserAsyncTask extends AsyncTask<User,Void,Void>{
        private UsersDao dao;

        private InsertUserAsyncTask(UsersDao usersDao){
            this.dao=usersDao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.insert(user[0]);
            return null;
        }
    }
    private static class UpdateUserAsyncTask extends AsyncTask<User,Void,Void>{
        private UsersDao dao;

        private UpdateUserAsyncTask(UsersDao dao){
            this.dao=dao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.insert(user[0]);
            return null;
        }

    }
    private static class DeleteUserAsyncTask extends AsyncTask<User,Void,Void>{
        private UsersDao dao;

        private DeleteUserAsyncTask(UsersDao dao){
            this.dao=dao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.delete(user[0]);
            return null;
        }

    }
    private static class GetAllUserAsyncTask extends AsyncTask<Void,Void,List<User>>{
        private UsersDao dao;

        private GetAllUserAsyncTask(UsersDao dao){
            this.dao=dao;
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            return dao.getAllUsers();
        }

    }
    private static class GetUserAsyncTask extends AsyncTask<String,Void,User>{
        private UsersDao dao;

        private GetUserAsyncTask(UsersDao dao){
        this.dao=dao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return dao.getUser(strings[0]);
        }
    }

    private static class InsertProfileAsyncTask extends AsyncTask<Profile,Void,Void>{
        private ProfileDao dao;

        private InsertProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected Void doInBackground(Profile... profile) {
            dao.insert(profile[0]);
            return null;
        }
    }
    private static class UpdateProfileAsyncTask extends AsyncTask<Profile,Void,Void>{
        private ProfileDao dao;

        private UpdateProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected Void doInBackground(Profile... profiles) {
            dao.insert(profiles[0]);
            return null;
        }
    }
    private static class DeleteProfileAsyncTask extends AsyncTask<Profile,Void,Void>{
        private ProfileDao dao;

        private DeleteProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected Void doInBackground(Profile... profiles) {
            dao.delete(profiles[0]);
            return null;
        }
    }
    private static class GetProfileAsyncTask extends AsyncTask<String,Void,Profile>{
        private ProfileDao dao;

        private GetProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected Profile doInBackground(String... user_id) {
            return dao.getProfile(user_id[0]);
        }
    }
    private static class GetAllProfileAsyncTask extends AsyncTask<Void,Void,List<Profile>>{
        private ProfileDao dao;

        private GetAllProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected List<Profile> doInBackground(Void... voids) {
            return dao.getAllProfiles();
        }

    }

}
