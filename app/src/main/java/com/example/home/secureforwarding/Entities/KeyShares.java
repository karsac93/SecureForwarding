package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class KeyShares extends DataShares implements Serializable {

    @ColumnInfo(name = "encrypted_node_num")
    private String encryptedNodeNum;

    @ColumnInfo(name = "cipher_data", typeAffinity = ColumnInfo.BLOB)
    private byte[] cipher_data;

    public String getEncryptedNodeNum() {
        return encryptedNodeNum;
    }

    public void setEncryptedNodeNum(String encryptedNodeNum) {
        this.encryptedNodeNum = encryptedNodeNum;
    }

    public byte[] getCipher_data() {
        return cipher_data;
    }

    public void setCipher_data(byte[] cipher_data) {
        this.cipher_data = cipher_data;
    }

    public KeyShares(@NonNull String msg_id, @NonNull String destId, int fileId, String type,
                     @NonNull String shareType, int status, String senderInfo, byte[] data,
                     String encryptedNodeNum, byte[] cipher_data) {
        super(msg_id, destId, fileId, type, shareType, status, senderInfo, data);
        this.encryptedNodeNum = encryptedNodeNum;
        this.cipher_data = cipher_data;
    }
}
