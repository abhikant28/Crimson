package com.akw.crimson.Backend.Database;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Database.DAOs.DataConverter;
import com.akw.crimson.Backend.Database.DAOs.UsersDao;
import com.akw.crimson.Backend.Database.DAOs.MessagesDao;

import java.io.File;

@Database(
        version = 1,
        entities = {User.class, Message.class},
        exportSchema = true)
//        autoMigrations = {
//                @AutoMigration(from = 1, to = 2)
//
//        }


@TypeConverters({DataConverter.class})
abstract class TheDatabase extends RoomDatabase{

    private static TheDatabase instance;

    public abstract MessagesDao messagesDao();
    public abstract UsersDao usersDao();

    public static synchronized TheDatabase getInstance(Context context){
        if(instance==null){
            String mediaStorageDir = Environment.getExternalStorageDirectory()
                    + "/Android/media" +
                    "" +
                    "/"
                    + context.getApplicationContext().getPackageName()
                    + "/Databases";
            new File(mediaStorageDir).mkdirs();
            instance = Room.databaseBuilder(context.getApplicationContext()
                            ,TheDatabase.class,"AppDatabase")
                    .fallbackToDestructiveMigration()
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
            instance.getQueryExecutor();
                    }
    };

    private  static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private MessagesDao messagesDao;
        private UsersDao chatListDao;
        private TheDatabase db;

        private PopulateDbAsyncTask(TheDatabase db){
            messagesDao =db.messagesDao();
            chatListDao = db.usersDao();
            this.db=db;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            return null;
        }
    }

}
