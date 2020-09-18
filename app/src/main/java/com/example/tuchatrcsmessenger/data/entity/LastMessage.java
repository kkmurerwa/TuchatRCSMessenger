package com.example.tuchatrcsmessenger.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "last_message_table")
public class LastMessage {

    String message;
    String sentTime;
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String id;

    public LastMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LastMessage(String message, String sentTime, String id) {
        this.message = message;
        this.sentTime = sentTime;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
}
