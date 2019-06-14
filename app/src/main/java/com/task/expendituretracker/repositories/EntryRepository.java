package com.task.expendituretracker.repositories;

import android.app.Application;
import android.os.AsyncTask;


import androidx.lifecycle.LiveData;

import com.task.expendituretracker.dbanddao.EntryDao;
import com.task.expendituretracker.dbanddao.EntryDatabase;
import com.task.expendituretracker.models.Entry;

import java.util.List;

public class EntryRepository {

    private EntryDao entryDao;

    public EntryRepository(Application application) {
        EntryDatabase database=EntryDatabase.getInstance(application);
        entryDao=database.getDao();
    }
    public void insertEntry(Entry entry){
        new InsertTask(entryDao).execute(entry);
    }

    public void updateEntry(Entry entry){
        new UpdateTask(entryDao).execute(entry);
    }

    public void deleteEntry(Entry entry){
        new DeleteTask(entryDao).execute(entry);
    }

    public LiveData<List<Entry>> getAllEntries(){
        LiveData<List<Entry>> allEntries=entryDao.getAllEntries();
        return  allEntries;
    }


    public LiveData<List<Entry>> getBorrowed(){
        LiveData<List<Entry>> allEntries=entryDao.getBorrowed();
        return  allEntries;
    }


    public LiveData<List<Entry>> getLent(){
        LiveData<List<Entry>> allEntries=entryDao.getLent();
        return  allEntries;
    }


    public LiveData<List<Entry>> getDealsWith(String payeeOrPayer){
        LiveData<List<Entry>> allEntries=entryDao.getDealsWith(payeeOrPayer);
        return  allEntries;
    }

    public void deleteAll(){
        new DeleteAlltask(entryDao).execute();
    }

    class InsertTask extends AsyncTask<Entry,Void,Void>{

        private EntryDao entryDao;
        private InsertTask(EntryDao entryDao){
            this.entryDao=entryDao;
        }

        @Override
        protected Void doInBackground(Entry... entries) {
            entryDao.insertEntry(entries[0]);
            return null;
        }

    }
    class UpdateTask extends AsyncTask<Entry,Void,Void>{

        private EntryDao entryDao;
        private UpdateTask(EntryDao entryDao){
            this.entryDao=entryDao;
        }

        @Override
        protected Void doInBackground(Entry... entries) {
            entryDao.updateEntry(entries[0]);
            return null;
        }
    }
    class DeleteTask extends AsyncTask<Entry,Void,Void>{

        private EntryDao entryDao;
        private DeleteTask(EntryDao entryDao){
            this.entryDao=entryDao;
        }

        @Override
        protected Void doInBackground(Entry... entries) {
            entryDao.deleteEntry(entries[0]);
            return null;
        }
    }
    class DeleteAlltask extends AsyncTask<Void,Void,Void>{

        private EntryDao entryDao;
        private DeleteAlltask(EntryDao entryDao){
            this.entryDao=entryDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            entryDao.deleteAll();
            return null;
        }
    }

}
