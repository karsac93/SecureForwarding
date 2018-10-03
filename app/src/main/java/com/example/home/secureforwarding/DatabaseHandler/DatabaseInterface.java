package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.Shares;

import java.util.List;

@Dao
public interface DatabaseInterface {
    @Insert
    void insertCompleteFile(CompleteFiles completeFiles);

    @Query("select id from completefiles")
    List<String> fetchCompleteIds();

    @Insert
    void insertKeyShares(Shares shares);

    @Query("select count(*) from shares")
    int numShares();

    @Insert
    void insertDataShares(Shares shares);
}
