package com.akw.crimson.Backend.Database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Database;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.Profile;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Database.DAOs.ProfileDao;
import com.akw.crimson.Backend.Database.DAOs.UsersDao;
import com.akw.crimson.Backend.Database.DAOs.MessagesDao;

@Database(entities = {Profile.class, User.class, Message.class}, version=1)
abstract class TheDatabase extends RoomDatabase{

    private static TheDatabase instance;

    public abstract ProfileDao profileDao();
    public abstract MessagesDao messagesDao();
    public abstract UsersDao usersDao();

//    SimpleSQLiteQuery query= new SimpleSQLiteQuery("CREATE TRIGGER update_user_msg AFTER UPDATE ON messages_Table"+
//            "  BEGIN" +
//            "    UPDATE user_table SET last_msg = new.msg AND unread_count=unread_count + 1 WHERE user_id = new.user_id;" +
//            "  END;");
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
            instance.getQueryExecutor();
//            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_user_msg AFTER UPDATE ON messages_Table"+
//                    "  BEGIN" +
//                    "    UPDATE user_table SET last_msg = new.msg AND unread_count=unread_count + 1 WHERE user_id = new.user_id;" +
//                    "  END;");
                    }
    };

    private  static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProfileDao profileDao;
        private MessagesDao messagesDao;
        private UsersDao chatListDao;
        private TheDatabase db;

        private PopulateDbAsyncTask(TheDatabase db){
            profileDao =db.profileDao();
            messagesDao =db.messagesDao();
            chatListDao = db.usersDao();
            this.db=db;
        }
        @Override
        protected Void doInBackground(Void... voids) {

//            chatListDao.insert(new User("1","","First Contact","https://www.shareicon.net/data/64x64/2016/05/24/770133_man_512x512.png","974631852",true));
//            chatListDao.insert(new User("2","","Second Contact","https://www.shareicon.net/data/64x64/2016/05/24/770131_man_512x512.png","0129384756",true));
//            chatListDao.insert(new User("3","","Third Contact",null,"6785940321",true));
//            chatListDao.insert(new User("4","","Forth Contact","https://icon-library.com/images/profile-pic-icon/profile-pic-icon-28.jpg","0987654321",true));
//            chatListDao.insert(new User("5","","Fifth Contact","https://icon-library.com/images/profile-pic-icon/profile-pic-icon-10.jpg","1234567890",false));
//
////            db.query(new SimpleSQLiteQuery("CREATE TRIGGER update_user_msg AFTER UPDATE ON messages_Table"+
//                    "  BEGIN" +
//                    "    UPDATE user_table SET last_msg = new.msg AND unread_count=unread_count + 1 WHERE user_id = new.user_id;" +
//                    "  END;"));

//
//            messagesDao.insert(new Message("231","1","","Hi!",false,false,"_",0));
//            messagesDao.insert(new Message("232","1","","Hey",true,false,"_",1));
//            messagesDao.insert(new Message("233","1","","Bye..",false,false,"_",0));
//            messagesDao.insert(new Message("234","1","","Byeeee!",true,false,"_",2));
//            messagesDao.insert(new Message("134","2","","Hello",false,false,"_",1,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("135","2","","What's up?",true,false,"_",0,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("136","2","","All fine..",false,false,"_",1,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("137","2","","What about you??",false,false,"_",1,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("138","2","","Ditto .",true,true,"_",0,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("334","3","","Sent by user 3 Message Text",false,false,"_",1,"12:00","22/10/2022"));
//            messagesDao.insert(new Message("434","4","","Sent by user 4 Message Text",false,false,"_",1,"12:00","22/10/2022"));
            return null;
        }
    }

}
