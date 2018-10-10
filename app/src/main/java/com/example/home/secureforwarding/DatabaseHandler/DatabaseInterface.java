package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.Entities.Shares;

import java.util.List;

@Dao
public interface DatabaseInterface {
    @Insert
    void insertCompleteFile(CompleteFiles completeFiles);

    @Query("select * from completefiles where type=:type")
    List<CompleteFiles> fetchCompleteFiles(String type);

    @Insert
    void insertKeyShares(Shares shares);

    @Query("select count(*) from Shares")
    int numShares();

    @Insert
    void insertDataShares(Shares shares);

    @Query("select * from keystore")
    List<KeyStore> getKeyStores();

    @Query("select * from shares where id=:fileId")
    List<Shares> getShares(String fileId);

    @Query("select * from shares where share_type=:interType")
    List<Shares> getInterShares(String interType);

    @Query("select public_key from keystore where id=:deviceId")
    byte[] getPublicKey(String deviceId);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateKeyShare(Shares shares);

}
