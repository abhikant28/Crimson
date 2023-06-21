package com.akw.crimson.Backend.Database;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.DAOs.MessagesDao;
import com.akw.crimson.Backend.Database.DAOs.UsersDao;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Chat.ChatActivity;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TheRepository {

    public String userID;
    private final UsersDao usersDao;
    private final MessagesDao messagesDao;
    private final LiveData<List<User>> getChatList;
    private LiveData<List<Message>> getLiveMessages;
    private final LiveData<List<Message>> pendingMessagesList;
    private final LiveData<List<Message>> infoMessagesList;
    private final LiveData<List<Message>> receivedMessagesList;
    private final LiveData<List<User>> getAllUsers;

    public TheRepository(Application application) {
        TheDatabase database = TheDatabase.getInstance(application);
        usersDao = database.usersDao();
        messagesDao = database.messagesDao();
//        chatList= usersDao.getChatList();
        getChatList = usersDao.getChatList();
        getAllUsers = usersDao.getAllUsersList();
        pendingMessagesList = messagesDao.pendingMessages();
        infoMessagesList = messagesDao.receivedInfoMessages();
        receivedMessagesList = messagesDao.receivedMessages();
//        getLiveMessages=messagesDao.getLiveMessages(userID);
        getLiveMessages = null;
    }

    public LiveData<List<Message>> getLiveMessages(String userId) {
        getLiveMessages = messagesDao.getLiveMessages(userId);
        return getLiveMessages;
    }

    public void insertMessage(Message msg) {
        new InsertMessageAsyncTask(messagesDao, usersDao).execute(msg);
    }

    public void deleteMessage(Message msg) {
        new DeleteMessageAsyncTask(usersDao, messagesDao).execute(msg);
    }

    public void updateMessage(Message msg) {
        new UpdateMessageAsyncTask(usersDao, messagesDao).execute(msg);
    }

    public void updateAllMessage(List<Message> msg) {
        if (!msg.isEmpty()) {
            new UpdateMessageAllAsyncTask(messagesDao).execute(msg);
        }
    }

    public void insertAllMessage(List<Message> msg) {
        if (!msg.isEmpty()) {
            new InsertAllMessageAsyncTask(messagesDao).execute(msg);
        }
    }
    public void deleteAllMessage(List<Message> msg) {
        if (!msg.isEmpty()) {
            new DeleteAllMessageAsyncTask(messagesDao).execute(msg);
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

    public List<Message> getStarredUserMessage(String userID) {
        try {
            return new GetStarredUserMessagesAsyncTask(messagesDao).execute(userID).get();
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
            return new SearchInUserMessageAsyncTask(messagesDao).execute(new String[]{query, id}).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<List<Message>> getInfoMessagesList() {
        return infoMessagesList;
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
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                return new GetUserAsyncTask(usersDao).execute(user_ID).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.i("InterruptedException", e.toString());
                retryCount++;
            }
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

    public List<User> getConnectedUsers() {
        try {
            return new GetConnectedUsersAsyncTask(usersDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Message> getUserMedia(String id) {
        try {
            return new GetUserMediaAsyncTask(usersDao).execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Message> getUserMediaByType(String id, int[] types) {
        try {
            return new GetUserMediaByTypeAsyncTask(usersDao, types).execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
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

    public List<Message> getStarredMessage() {
        try {
            return new GetStarredMessagesAsyncTask(messagesDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class InsertMessageAsyncTask extends AsyncTask<Message, Void, Void> {
        private final MessagesDao messagesDao;
        private final UsersDao usersDao;

        private InsertMessageAsyncTask(MessagesDao msgDao, UsersDao usersDao) {
            this.messagesDao = msgDao;
            this.usersDao = usersDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.insert(messages[0]);
            User user = usersDao.getUser(messages[0].getUser_id());
           // Log.i("REPO USER ID ::::: ", messages[0].getUser_id());
           // Log.i("REPO NULL CHECK ::::: ", (user==null)+"_"+(messages[0]==null));
            if (user!=null && messages[0].getMsg() != null)
                user.setLast_msg(messages[0].getMsg().substring(0, Math.min(15, messages[0].getMsg().length())), messages[0].isMedia() ? messages[0].getMediaType() : Constants.Media.KEY_MESSAGE_MEDIA_TYPE_NONE);
            user.setTime(UsefulFunctions.getTime());
            user.setDate(UsefulFunctions.getDate());
            if (messages[0].isSelf()) {
                user.setUnread_count(0);
                user.setUnread(false);
                if (!user.isConnected()) {
                    user.setConnected(true);
                    //Send Profile Pic
                }
            } else {
                user.setUnread_count(user.getUnread_count() + 1);
                user.setUnread(true);
            }
            ChatActivity.updated = true;
            user.incMsgCount();
            user.setKnown(true);
            usersDao.update(user);
            return null;
        }
    }

    private static class InsertAllMessageAsyncTask extends AsyncTask<List<Message>, Void, Void> {
        private final MessagesDao messagesDao;

        private InsertAllMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Void doInBackground(List<Message>... messages) {
            messagesDao.insertAll(messages[0]);
            return null;
        }
    }

    private static class UpdateMessageAsyncTask extends AsyncTask<Message, Void, Void> {
        private final MessagesDao messagesDao;
        private final UsersDao usersDao;

        private UpdateMessageAsyncTask(UsersDao usersDao, MessagesDao msgDao) {
            this.messagesDao = msgDao;
            this.usersDao = usersDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.update(messages[0]);
            User user = usersDao.getUser(messages[0].getUser_id());
            if (messages[0].isMedia()) {
                if (messages[0].getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE || messages[0].getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO || messages[0].getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO)
                    user.addMedia(messages[0].getMediaID());
                else if (messages[0].getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT)
                    user.addDoc(messages[0].getMediaID());

            }

            ChatActivity.updated = true;
//            ChatActivity.updateID = messages[0].getUser_id();
            return null;
        }
    }

    private static class UpdateMessageAllAsyncTask extends AsyncTask<List<Message>, Void, Void> {
        private final MessagesDao messagesDao;

        private UpdateMessageAllAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Void doInBackground(List<Message>... messages) {
            messagesDao.updateAll(messages[0]);
            return null;
        }
    }

    private static class DeleteMessageAsyncTask extends AsyncTask<Message, Void, Void> {
        private final MessagesDao messagesDao;
        private final UsersDao usersDao;

        private DeleteMessageAsyncTask(UsersDao usersDao, MessagesDao msgDao) {
            this.messagesDao = msgDao;
            this.usersDao = usersDao;
        }

        @Override
        protected Void doInBackground(Message... messages) {
            messagesDao.delete(messages[0]);

            return null;
        }
    }

    private static class DeleteAllMessageAsyncTask extends AsyncTask<List<Message>, Void, Void> {
        private final MessagesDao messagesDao;

        private DeleteAllMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Void doInBackground(List<Message>... messages) {
            messagesDao.deleteAll(messages[0]);
            return null;
        }
    }


    private static class GetMessageAsyncTask extends AsyncTask<String, Void, Message> {
        private final MessagesDao messagesDao;

        private GetMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected Message doInBackground(String... L_msg_ID) {
            return messagesDao.getMessage(L_msg_ID[0]);
        }
    }

    private static class SearchInMessageAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final MessagesDao messagesDao;

        private SearchInMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected List<Message> doInBackground(String... L_msg_ID) {
            return messagesDao.searchInMessage(L_msg_ID[0]);
        }
    }

    private static class SearchInUserMessageAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final MessagesDao messagesDao;

        private SearchInUserMessageAsyncTask(MessagesDao msgDao) {
            this.messagesDao = msgDao;
        }

        @Override
        protected List<Message> doInBackground(String... query) {
            return messagesDao.searchInUserMessages(query[0], query[1]);
        }
    }

    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private final UsersDao dao;

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
        private final UsersDao dao;

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
        private final UsersDao dao;

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
        private final UsersDao dao;

        private GetUserAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return dao.getUser(strings[0]);
        }
    }

    private static class GetUserByNumAsyncTask extends AsyncTask<String, Void, User> {
        private final UsersDao dao;

        private GetUserByNumAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return dao.getUserByNum(strings[0]);
        }
    }

    private static class GetStarredMessagesAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final MessagesDao dao;

        private GetStarredMessagesAsyncTask(MessagesDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<Message> doInBackground(String... strings) {
            return dao.getStarredMessages();
        }
    }

    private static class GetStarredUserMessagesAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final MessagesDao dao;

        private GetStarredUserMessagesAsyncTask(MessagesDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<Message> doInBackground(String... strings) {
            return dao.getStarredUserMessages(strings[0]);
        }
    }

    private static class GetConnectedUsersAsyncTask extends AsyncTask<Void, Void, List<User>> {
        private final UsersDao dao;

        private GetConnectedUsersAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<User> doInBackground(Void... strings) {
            return dao.getConnectedUsers();
        }
    }

    private static class GetSearchUsersAsyncTask extends AsyncTask<String, Void, List<User>> {
        private final UsersDao dao;

        private GetSearchUsersAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<User> doInBackground(String... strings) {
            return dao.searchUserByText(strings[0]);
        }
    }

    private static class GetUserMediaAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final UsersDao dao;

        private GetUserMediaAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<Message> doInBackground(String... strings) {
            return dao.getUserMedia(strings[0]);
        }
    }

    private static class GetUserMediaByTypeAsyncTask extends AsyncTask<String, Void, List<Message>> {
        private final UsersDao dao;
        private final int[] types;

        public GetUserMediaByTypeAsyncTask(UsersDao dao, int[] types) {
            this.dao = dao;
            this.types = types;
        }

        @Override
        protected List<Message> doInBackground(String... strings) {
            return dao.getUserMediaByType(strings[0], types);
        }

    }

    private static class CheckForUserNumAsyncTask extends AsyncTask<String, Void, Boolean> {
        private final UsersDao dao;

        private CheckForUserNumAsyncTask(UsersDao dao) {
            this.dao = dao;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return dao.checkForNumber(strings[0]);
        }
    }


}
