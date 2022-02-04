package com.example.crimson.Database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.crimson.AppObjects.Message;
import com.example.crimson.AppObjects.Profile;
import com.example.crimson.AppObjects.User;
import com.example.crimson.Database.DAOs.UsersDao;
import com.example.crimson.Database.DAOs.MessagesDao;
import com.example.crimson.Database.DAOs.ProfileDao;

@Database(entities = {Profile.class, User.class, Message.class}, version=1)
abstract class TheDatabase extends RoomDatabase{

    private static TheDatabase instance;

    public abstract ProfileDao profileDao();
    public abstract MessagesDao messagesDao();
    public abstract UsersDao usersDao();

    public static synchronized TheDatabase getInstance(Context context){
        if(instance==null){
            instance = Room.databaseBuilder(context.getApplicationContext(),TheDatabase.class
                    ,"AppDatabase").fallbackToDestructiveMigration()
                    .addCallback(roomCallBack)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallBack = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private  static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProfileDao profileDao;
        private MessagesDao messagesDao;
        private UsersDao chatListDao;

        private PopulateDbAsyncTask(TheDatabase db){
            profileDao =db.profileDao();
            messagesDao =db.messagesDao();
            chatListDao = db.usersDao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
//            messagesDao.insert(new Message());
//
//
//            chatListDao.insert(new User());
//            chatListDao.insert(new User());
//            chatListDao.insert(new User());
//            profileDao.insert(new Profile());

            return null;
        }
    }

}
