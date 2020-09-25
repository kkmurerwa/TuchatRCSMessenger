package com.example.tuchatrcsmessenger.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "last_message_table")
public class LastMessage {

    String message;
    String sentTime;
    int unreadCount;
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String id;

    public LastMessage() {
    }

    public LastMessage(String message, String sentTime, String id, int unreadCount) {
        this.message = message;
        this.sentTime = sentTime;
        this.id = id;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
