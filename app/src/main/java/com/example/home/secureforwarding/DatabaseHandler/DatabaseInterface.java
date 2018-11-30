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
import com.example.home.secureforwarding.Entities.SecretStore;

import java.util.List;

@Dao
public interface DatabaseInterface {

    // Insert queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCompleteFile(CompleteFiles completeFiles);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKeyShares(KeyShares shares);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDataShares(DataShares shares);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertKeyStore(KeyStore keyStore);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSecretStore(SecretStore secretStore);


    // Update queries
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateDataShare(DataShares dataShare);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateKeyShare(KeyShares shares);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCompleteFile(CompleteFiles completeFiles);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSecretStore(SecretStore secretStore);

    // Select queries
    @Query("select * from completefiles where type=:type")
    List<CompleteFiles> fetchCompleteFiles(String type);

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

    @Query("select * from completefiles where type=:owner_type and dest_id=:id")
    List<CompleteFiles> getCompleteFilesForDevice(String owner_type, String id);

    @Query("select * from keyshares where file_id in (select min(file_id) from keyshares " +
            "where status=:status and " +
            "msg_id not in (select msg_id from keyshares where encrypted_node_num=:nodeId and" +
            " dest_id<>:nodeId and status=:status) and node_type<>:destType group by msg_id)")
    List<KeyShares> getKeySharesForThisDevice(int status, String nodeId, String destType);

    @Query("select * from keyshares where file_id in (select min(file_id) from keyshares " +
            "where status=:status and encrypted_node_num=:nodeId and dest_id<>:nodeId and " +
            "node_type<>:destType group by msg_id)")
    List<KeyShares> getKeySharesEncryptedWithDevice(int status, String nodeId, String destType);

    @Query("select * from keyshares where node_type<>:nodeType and dest_id=:nodeId and status=:status")
    List<KeyShares> getKeySharesForDestDevice(String nodeType, String nodeId, int status);

    @Query("select * from datashares where file_id in (select min(file_id) from datashares " +
            "where status=:status and node_type<>:destType and dest_id<>:destId group by msg_id)")
    List<DataShares> getDataSharesForThisDevice(int status, String destType, String destId);

    @Query("select * from datashares where file_id in (select file_id from datashares " +
            "where node_type<>:destType and dest_id=:destId and status=:status)")
    List<DataShares> getDataSharesForThisDestDevice(int status, String destType, String destId);

    @Query("select count(*) from completefiles where id=:msg_id and type=:destType and status=:status")
    int checkCompleteFilealreadyPresent(String msg_id, String destType, boolean status);

    @Query("select count(*) from completefiles where id=:msg_id and type=:destType")
    int checkCompleteFileRowExistsForMsg(String msg_id, String destType);

    @Query("select * from completefiles where id=:msgId")
    CompleteFiles getCompleteFileforMsg(String msgId);

    @Query("select * from secretstore where msg_id=:msgId")
    SecretStore getSecretStoreForMsg(String msgId);

    //Delete queries
    @Delete
    void deleteCompleteFileId(CompleteFiles completeFiles);

    @Query("delete from completefiles where id=:id")
    void deleteCompleteFileQuery(String id);

    @Query("delete from keyshares where msg_id=:msg_id")
    void deleteKeySharesForMsg(String msg_id);

    @Query("delete from datashares where msg_id=:msg_id")
    void deleteDataSharesForMsg(String msg_id);

    @Delete
    void deleteKeyShare(KeyShares keyShares);

    @Delete
    void deleteDataShare(DataShares dataShares);

    //Testing queries
    @Query("select * from keyshares limit 4")
    List<KeyShares> getTestShares();

    @Query("select * from datashares limit 4")
    List<DataShares> getTestDataShares();



}
