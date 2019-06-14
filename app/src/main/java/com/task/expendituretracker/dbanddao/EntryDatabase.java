package com.task.expendituretracker.dbanddao;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


import com.task.expendituretracker.models.Entry;

@Database(entities = {Entry.class},version = 1)
public abstract class EntryDatabase extends RoomDatabase {
    public abstract EntryDao getDao();

    private static EntryDatabase database;

    public static synchronized EntryDatabase getInstance(Application application){
        if(database==null){
            database=Room.databaseBuilder(application.getApplicationContext(),EntryDatabase.class,"entry-db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

}
