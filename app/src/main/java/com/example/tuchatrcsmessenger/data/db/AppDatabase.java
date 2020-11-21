package com.example.tuchatrcsmessenger.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.tuchatrcsmessenger.Classes.DateConverter;
import com.example.tuchatrcsmessenger.data.dao.ContactsDao;
import com.example.tuchatrcsmessenger.data.dao.LastMessageDao;
import com.example.tuchatrcsmessenger.data.entity.ContactsClass;
import com.example.tuchatrcsmessenger.data.entity.LastMessage;

@Database(entities = {ContactsClass.class, LastMessage.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDB;

    public static AppDatabase getInstance(Context context) {
        if (null == appDB) {
            appDB = buildDatabaseInstance(context);
        }
        return appDB;
    }

    private static AppDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                AppDatabase.class,
                "app_db")
                .allowMainThreadQueries().fallbackToDestructiveMigration()
                .build();
    }

    public abstract ContactsDao getContactsDao();
    public abstract LastMessageDao getLastMessageDao();

    public void cleanUp() {
        appDB = null;
    }

}