package com.task.expendituretracker.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "entries")
public class Entry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String toOrFrom;
    private String name;
    private float amount;
    private int to;

    public Entry(String toOrFrom, String name, float amount, int to) {
        this.toOrFrom = toOrFrom;
        this.name = name;
        this.amount = amount;
        this.to = to;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToOrFrom() {
        return toOrFrom;
    }

    public void setToOrFrom(String toOrFrom) {
        this.toOrFrom = toOrFrom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
