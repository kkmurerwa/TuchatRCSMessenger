package com.example.tuchatrcsmessenger.Classes;

import java.util.ArrayList;
import java.util.List;

public class ChatroomClass {
    private List<String> chatMembers;

    public ChatroomClass(List<String> chatMembers) {
        this.chatMembers = chatMembers;
    }

    public List<String> getChatMembers() {
        return chatMembers;
    }

    public void setChatMembers(List<String> chatMembers) {
        this.chatMembers = chatMembers;
    }
}
