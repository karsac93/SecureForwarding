package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.Entities.KeyStore;

import java.util.List;

@Dao
public interface DatabaseInterface {
    @Insert
    void insertCompleteFile(CompleteFiles completeFiles);

    @Query("select * from completefiles where type=:type")
    List<CompleteFiles> fetchCompleteFiles(String type);

    @Insert
    void insertKeyShares(KeyShares shares);

    @Insert
    void insertDataShares(DataShares shares);

    @Query("select * from keystore")
    List<KeyStore> getKeyStores();

    @Query("select * from keyshares where msg_id=:msg_id")
    List<KeyShares> getKeyShareForMsg(String msg_id);

    @Query("select * from datashares where msg_id=:msg_id")
    List<DataShares> getDataShareForMsg(String msg_id);

    @Query("select * from keyshares where node_type=:interType")
    List<KeyShares> getInterKeyShares(String interType);

    @Query("select * from datashares where node_type=:interType")
    List<DataShares> getInterDataShares(String interType);

    @Query("select public_key from keystore where id=:deviceId")
    byte[] getPublicKey(String deviceId);

    @Insert()
    void insertKeyStore(KeyStore keyStore);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateKeyShare(KeyShares shares);

}
