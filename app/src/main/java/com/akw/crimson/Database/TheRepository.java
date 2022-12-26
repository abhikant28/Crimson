package com.akw.crimson.Database;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.Profile;
import com.akw.crimson.AppObjects.User;
import com.akw.crimson.Chat.Chat;
import com.akw.crimson.Database.DAOs.MessagesDao;
import com.akw.crimson.Database.DAOs.ProfileDao;
import com.akw.crimson.Database.DAOs.UsersDao;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TheRepository {

    private ProfileDao profileDao;
    private UsersDao usersDao;
    private MessagesDao messagesDao;

    private LiveData<List<User>> getChatList;
    private LiveData<List<Message>> pendingMessagesList;
    private LiveData<List<User>> getAllUsers;

    public TheRepository(Application application){
        TheDatabase database = TheDatabase.getInstance(application);
        profileDao = database.profileDao();
        usersDao = database.usersDao();
        messagesDao =database.messagesDao();
//        chatList= usersDao.getChatList();
        getChatList =usersDao.getChatList();
        getAllUsers = usersDao.getAllUsersList();
        pendingMessagesList=messagesDao.pendingMessages();
    }

    public void insertMessage(Message msg){
        new InsertMessageAsyncTask(messagesDao,usersDao).execute(msg);
    }
    public void updateMessage(Message msg){
        new UpdateMessageAsyncTask(messagesDao).execute(msg);
    }
    public Cursor getChatMessages(String user_ID){
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

    public LiveData<List<Message>> getPendingMessagesList() {
        return pendingMessagesList;
    }

    public LiveData<List<User>> getGetChatList(){ return getChatList; }
    public LiveData<List<User>> getGetAllUsersList(){ return getAllUsers; }

    public void insertUser(User user){
        new InsertUserAsyncTask(usersDao).execute(user);
    }
    public void updateUser(User user){
        new UpdateUserAsyncTask(usersDao).execute(user);
    }
    public void deleteUser(User user){
        new DeleteUserAsyncTask(usersDao).execute(user);
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
    public Cursor getAllProfiles(){
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
        private UsersDao usersDao;

        private InsertMessageAsyncTask(MessagesDao msgDao,UsersDao usersDao){
            this.messagesDao=msgDao;
            this.usersDao=usersDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.insert(messages[0]);
            User user=usersDao.getUser(messages[0].getUser_id());
//            Log.i("CONNECTING USER:::::", user.getDisplayName()+user.isConnected());
            try{
                user.setLast_msg(messages[0].getMsg().substring(0, 15)+"...");
            }catch (StringIndexOutOfBoundsException e){
                user.setLast_msg(messages[0].getMsg().substring(0, messages[0].getMsg().length()));
            }
            Calendar time =Calendar.getInstance();
            user.setTime(String.format("%02d", time.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d",time.get(Calendar.MINUTE))+":"+String.format("%02d",time.get(Calendar.SECOND)));
            user.setDate(new SimpleDateFormat("yyyy/MM/dd").format(time.getTime()));
            if(messages[0].isSelf()){
                user.setUnread_count(0);
                user.setUnread(false);
            }else{
                user.setUnread_count(user.getUnread_count() + 1);
                user.setUnread(true);
            }
            Chat.updated=true;
            Chat.updateID=user.getUser_id();

            user.setConnected(true);
            usersDao.update(user);
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
    private static class GetAllUserAsyncTask extends AsyncTask<Void,Void,Cursor>{
        private UsersDao dao;

        private GetAllUserAsyncTask(UsersDao dao){
            this.dao=dao;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return dao.getAllUsersCursor();
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
    private static class GetAllProfileAsyncTask extends AsyncTask<Void,Void,Cursor>{
        private ProfileDao dao;

        private GetAllProfileAsyncTask(ProfileDao dao){
            this.dao=dao;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return dao.getAllProfiles();
        }

    }

}
