package com.example.tuchatrcsmessenger.Classes;


import java.util.Date;

public class MessagesClass {
    public String senderName, messageBody, readStatus, chatRoomId, userId;
    public Date sentTime;

    public MessagesClass() {}

    public MessagesClass(String senderName, String messageBody, Date sentTime, String readStatus, String chatRoomId, String userId) {
        this.senderName = senderName;
        this.messageBody = messageBody;
        this.sentTime = sentTime;
        this.readStatus = readStatus;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
