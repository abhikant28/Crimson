package com.example.crimson.Database.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.crimson.AppObjects.Profile;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert
    void insert(Profile profile);

    @Delete
    void delete(Profile profile);

    @Update
    void update(Profile profile);

    @Query("SELECT * FROM profile_table")
    List<Profile> getAllProfiles();

    @Query("Select * FROM profile_table where ID=:user_id")
    Profile getProfile(String user_id);

}
