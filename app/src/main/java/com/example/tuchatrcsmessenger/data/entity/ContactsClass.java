package com.example.tuchatrcsmessenger.data.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts_table")
public class ContactsClass {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String id;
    String displayName;
    String phoneNumber;

    public ContactsClass() {

    }

    public ContactsClass(@NonNull String id, String displayName, String phoneNumber) {
        this.id = id;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}