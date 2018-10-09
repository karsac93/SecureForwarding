package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.Entities.Shares;

@Database(version = 1, entities = {CompleteFiles.class, KeyStore.class, Shares.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    abstract public DatabaseInterface dao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "secure_database")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
