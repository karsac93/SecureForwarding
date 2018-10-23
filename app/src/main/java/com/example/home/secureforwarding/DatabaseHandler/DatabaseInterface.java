package com.example.home.secureforwarding.DatabaseHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKeyShares(KeyShares shares);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKeyStore(KeyStore keyStore);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateKeyShare(KeyShares shares);

    @Query("select * from completefiles where type=:owner_type and dest_id=:id")
    List<CompleteFiles> getCompleteFilesForDevice(String owner_type, String id);

    @Query("delete from keyshares where msg_id=:msg_id")
    void deleteKeySharesForMsg(String msg_id);

    @Query("delete from datashares where msg_id=:msg_id")
    void deleteDataSharesForMsg(String msg_id);

    @Delete
    void deleteCompleteFileId(CompleteFiles completeFiles);

    @Query("select * from keyshares where file_id in (select min(file_id) from keyshares " +
            "where status=:status and " +
            "file_id not in (select file_id from keyshares where encrypted_node_num=:node_num and" +
            " node_type<>:destType) group by msg_id)")
    List<KeyShares> getKeySharesForThisDevice(int status, String node_num, String destType);

    @Query("select * from keyshares where file_id in (select min(file_id) from keyshares " +
            "where status=:status and encrypted_node_num=:node_num group by msg_id)")
    List<KeyShares> getKeySharesEncryptedWithDevice(int status, String node_num);

    @Query("select * from keyshares where file_id <= 7")
    List<KeyShares> testKeyShares();


}
