package com.example.tuchatrcsmessenger.data.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tuchatrcsmessenger.data.entity.ContactsClass;

import java.util.List;

@Dao
public interface ContactsDao {

    @Query("SELECT * FROM contacts_table")
    LiveData<List<ContactsClass>> getContacts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContact(ContactsClass contact);

    @Query("DELETE  FROM contacts_table")
    void deleteAll();
}
