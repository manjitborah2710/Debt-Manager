package com.task.expendituretracker.dbanddao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.task.expendituretracker.models.Entry;

import java.util.List;

@Dao
public interface EntryDao {

    @Insert
    public void insertEntry(Entry entry);

    @Update
    public void updateEntry(Entry entry);

    @Delete
    public void deleteEntry(Entry entry);

    @Query("select * from entries")
    public LiveData<List<Entry>> getAllEntries();

    @Query("select * from entries where `to`=1")
    public LiveData<List<Entry>> getBorrowed();

    @Query("select * from entries where `to`=0")
    public LiveData<List<Entry>> getLent();

    @Query("select * from entries where toOrFrom=:payeeOrPayer")
    public LiveData<List<Entry>> getDealsWith(String payeeOrPayer);

    @Query("delete from entries")
    public void deleteAll();
}
