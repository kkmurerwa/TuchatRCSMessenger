package com.example.tuchatrcsmessenger.data.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts_table")
public class ContactsClass {

    String displayName;
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String phoneNumber;

    public ContactsClass() {


    }

    public ContactsClass(String displayName, @NonNull String phoneNumber) {

        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
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

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}