package com.example.tuchatrcsmessenger.Classes;


import java.util.Date;

public class ConversationsClass {
    public String senderName, messageBody, readStatus, chatRoomId;
    public Date sentTime;


    public ConversationsClass() {}

    public ConversationsClass(String senderName, String messageBody, Date sentTime, String readStatus, String chatRoomId) {
        this.senderName = senderName;
        this.messageBody = messageBody;
        this.sentTime = sentTime;
        this.readStatus = readStatus;
        this.chatRoomId = chatRoomId;
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
}
