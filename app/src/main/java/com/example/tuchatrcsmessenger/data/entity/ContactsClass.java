package com.example.tuchatrcsmessenger.data.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts_table")
public class ContactsClass {

    String displayName;
    String phoneNumber;
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String userId;


    public ContactsClass() {


    }

    public ContactsClass(String userId, String displayName, @NonNull String phoneNumber) {
        this.userId = userId;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }


    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
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