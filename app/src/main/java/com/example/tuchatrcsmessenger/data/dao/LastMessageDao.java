package com.example.tuchatrcsmessenger.data.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tuchatrcsmessenger.data.entity.ContactsClass;
import com.example.tuchatrcsmessenger.data.entity.LastMessage;

import java.util.List;

@Dao
public interface LastMessageDao {

    @Query("SELECT * FROM last_message_table WHERE id = :id")
    LiveData<LastMessage> getLastMessageLiveData(String id);

    @Query("SELECT * FROM last_message_table WHERE id = :id")
    LastMessage getLastMessage(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLastMessage(LastMessage lastMessage);

    @Query("DELETE  FROM last_message_table")
    void deleteAll();
}
