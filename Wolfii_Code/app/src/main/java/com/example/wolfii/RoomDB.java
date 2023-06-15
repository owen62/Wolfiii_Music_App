package com.example.wolfii;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// add database entities
@Database(entities = {
        DataPlaylist.class,
        Musique.class,
        DataPlaylistMusic.class,
        DataHiddenMusic.class,
        DataLikedMusic.class}, version = 2, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    // create database instance
    private static RoomDB database;
    //define database name
    private static String DATABASE_NAME = "test";

    public synchronized static RoomDB getInstance(Context context) {
        if(database==null) {
            // when db null
            // initialize database
            database = Room.databaseBuilder(context.getApplicationContext(),
                    RoomDB.class, DATABASE_NAME).allowMainThreadQueries()
                    .fallbackToDestructiveMigration().build();
        }
        return database;
    }

    // create Dao
    public abstract MainDao mainDao();

}
