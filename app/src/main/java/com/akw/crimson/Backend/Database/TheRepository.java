package com.akw.crimson.Backend.Database;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Chat.ChatActivity;
import com.akw.crimson.Backend.Database.DAOs.MessagesDao;
import com.akw.crimson.Backend.Database.DAOs.UsersDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TheRepository {

    private UsersDao usersDao;
    private MessagesDao messagesDao;

    private LiveData<List<User>> getChatList;
    private LiveData<List<Message>> pendingMessagesList;
    private LiveData<List<Message>> receivedMessagesList;
    private LiveData<List<User>> getAllUsers;

    public TheRepository(Application application) {
        TheDatabase database = TheDatabase.getInstance(application);
        usersDao = database.usersDao();
        messagesDao = database.messagesDao();
//        chatList= usersDao.getChatList();
        getChatList = usersDao.getChatList();
        getAllUsers = usersDao.getAllUsersList();
        pendingMessagesList = messagesDao.pendingMessages();
        receivedMessagesList=messagesDao.receivedMessages();
    }

    public void insertMessage(Message msg) {
        new InsertMessageAsyncTask(messagesDao, usersDao).execute(msg);
    }

    public void updateMessage(Message msg) {
        new UpdateMessageAsyncTask(messagesDao).execute(msg);
    }
    public void updateAllMessage(List<Message> msg) {
        if(!msg.isEmpty()){
            new UpdateMessageAllAsyncTask(messagesDao).execute(msg);
        }
    }

    public Cursor getChatMessages(String user_ID) {
        return messagesDao.getMessages(user_ID);
    }

    public Message getMessage(String L_msg_ID) {
        try {
            return new GetMessageAsyncTask(messagesDao).execute(L_msg_ID).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Message> searchInMessage(String query) {
        try {
            return new SearchInMessageAsyncTask(messagesDao).execute(query).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Message> searchInUserMessage(String query, String id) {
        try {
            return new SearchInUserMessageAsyncTask(messagesDao).execute(new String[]{query,id}).get();
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

    public LiveData<List<Message>> getReceivedMessagesList() {
        return receivedMessagesList;
    }

    public LiveData<List<User>> getGetChatList() {
        return getChatList;
    }

    public LiveData<List<User>> getGetAllUsersList() {
        return getAllUsers;
    }

    public void insertUser(User user) {
        new InsertUserAsyncTask(usersDao).execute(user);
    }

    public void updateUser(User user) {
        new UpdateUserAsyncTask(usersDao).execute(user);
    }

    public void deleteUser(User user) {
        new DeleteUserAsyncTask(usersDao).execute(user);
    }

    public User getUser(String user_ID) {
        try {
            return new GetUserAsyncTask(usersDao).execute(user_ID).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByNum(String num) {
        try {
            return new GetUserByNumAsyncTask(usersDao).execute(num).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> searchUserByText(String num) {
        try {
            return new GetSearchUsersAsyncTask(usersDao).execute(num).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkForUserNum(String userNum) {
        try {
            return new CheckForUserNumAsyncTask(usersDao).execute(userNum).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static class InsertMessageAsyncTask extends AsyncTask<Message, Void, Void> {
        private MessagesDao messagesDao;
        private UsersDao usersDao;

        private InsertMessageAsyncTask(MessagesDao msgDao, UsersDao usersDao) {
            this.messagesDao = msgDao;
            this.usersDao = usersDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.insert(messages[0]);
            User user = usersDao.getUser(messages[0].getUser_id());
            try {
                user.setLast_msg(messages[0].getMsg().substring(0, 15) + "...");
            } catch (StringIndexOutOfBoundsException e) {
                user.setLast_msg(messages[0].getMsg().substring(0, messages[0].getMsg().length()));
            }
            Calendar time = Calendar.getInstance();
            user.setTime(String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)) + ":" + String.format("%02d", time.get(Calendar.SECOND)));
            user.setDate(new SimpleDateFormat("yyyy/MM/dd").format(time.getTime()));
            if (messages[0].isSelf()) {
                user.setUnread_count(0);
                user.setUnread(false);
            } else {
                user.setUnread_count(user.getUnread_count() + 1);
                user.setUnread(true);
            }
            ChatActivity.updated = true;
            ChatActivity.updateID = user.getUser_id();

            user.setConnected(true);
            usersDao.update(user);
            return null;
        }
    }

    private static class UpdateMessageAsyncTask extends AsyncTask<Message, Void, Void> {
        private MessagesDao messagesDao;

        private UpdateMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.update(messages[0]);
            return null;
        }
    }
    private static class UpdateMessageAllAsyncTask extends AsyncTask<List<Message>, Void, Void> {
        private MessagesDao messagesDao;

        private UpdateMessageAllAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Void doInBackground(List<Message>... messages) {
            messagesDao.updateAll(messages[0]);
            return null;
        }
    }
    private static class GetMessageAsyncTask extends AsyncTask<String, Void, Message> {
        private MessagesDao messagesDao;

        private GetMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Message doInBackground(String... L_msg_ID) {
            return messagesDao.getMessage(L_msg_ID[0]);
        }
    }
    private static class SearchInMessageAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private MessagesDao messagesDao;

        private SearchInMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected List<Message> doInBackground(String... L_msg_ID) {
            return messagesDao.searchInMessage(L_msg_ID[0]);
        }
    }
    private static class SearchInUserMessageAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private MessagesDao messagesDao;

        private SearchInUserMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected List<Message> doInBackground(String... query) {
            return messagesDao.searchInUserMessages(query[0],query[1]);
        }
    }
    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UsersDao dao;

        private InsertUserAsyncTask(UsersDao usersDao) {
            this.dao = usersDao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.insert(user[0]);
            return null;
        }
    }

    private static class UpdateUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UsersDao dao;

        private UpdateUserAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.insert(user[0]);
            return null;
        }

    }

    private static class DeleteUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UsersDao dao;

        private DeleteUserAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(User... user) {
            dao.delete(user[0]);
            return null;
        }

    }

    private static class GetUserAsyncTask extends AsyncTask<String, Void, User> {
        private UsersDao dao;

        private GetUserAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return dao.getUser(strings[0]);
        }
    }
    private static class GetUserByNumAsyncTask extends AsyncTask<String, Void, User> {
        private UsersDao dao;

        private GetUserByNumAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return dao.getUserByNum(strings[0]);
        }
    }
    private static class GetSearchUsersAsyncTask extends AsyncTask<String, Void, List<User>> {
        private UsersDao dao;

        private GetSearchUsersAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<User> doInBackground(String... strings) {
            return dao.searchUserByText(strings[0]);
        }
    }
    private static class CheckForUserNumAsyncTask extends AsyncTask<String, Void, Boolean> {
        private UsersDao dao;

        private CheckForUserNumAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return dao.checkForNumber(strings[0]);
        }
    }


}
