package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.Entities.OwnShares;

import java.util.List;

@Dao
public interface DatabaseInterface {
    @Insert
    void insertCompleteFile(CompleteFiles completeFiles);

    @Query("select id from completefiles")
    List<String> fetchCompleteIds();

    @Insert
    void insertKeyShares(OwnShares ownShares);

    @Query("select count(*) from OwnShares")
    int numShares();

    @Insert
    void insertDataShares(OwnShares ownShares);

    @Query("select * from keystore")
    List<KeyStore> getKeyStores();
}
