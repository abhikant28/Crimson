package com.akw.crimson.Database.DAOs;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.akw.crimson.AppObjects.Profile;

@Dao
public interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Profile profile);

    @Delete
    void delete(Profile profile);

    @Update
    void update(Profile profile);

    @Query("SELECT * FROM profile_table")
    Cursor getAllProfiles();

    @Query("Select * FROM profile_table where user_ID=:user_id")
    Profile getProfile(String user_id);

}
