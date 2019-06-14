package com.task.expendituretracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.task.expendituretracker.models.Entry;
import com.task.expendituretracker.repositories.EntryRepository;

import java.util.List;

public class EntryViewModel extends AndroidViewModel {
    private EntryRepository entryRepository;
    public EntryViewModel(@NonNull Application application) {
        super(application);
        entryRepository=new EntryRepository(application);
    }
    public void insertEntry(Entry entry){
        entryRepository.insertEntry(entry);
    }
    public void updateEntry(Entry entry){
        entryRepository.updateEntry(entry);
    }
    public void deleteEntry(Entry entry){
        entryRepository.deleteEntry(entry);
    }
    public LiveData<List<Entry>> getAllEntries(){
        return entryRepository.getAllEntries();
    }
    public LiveData<List<Entry>> getBorrowed(){
        return entryRepository.getBorrowed();
    }
    public LiveData<List<Entry>> getLent(){
       return entryRepository.getLent();
    }
    public LiveData<List<Entry>> getDealsWith(String payeeOrPayer){
        return entryRepository.getDealsWith(payeeOrPayer);
    }
    public void deleteAll(){
        entryRepository.deleteAll();
    }
}
