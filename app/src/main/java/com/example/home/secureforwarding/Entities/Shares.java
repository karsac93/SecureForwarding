package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(primaryKeys = {"id", "file_id", "share_type"})
public class Shares implements Serializable {
    @ColumnInfo(name = "id")
    @NonNull
    private String id;

    @ColumnInfo(name = "encrypted_node_num")
    private String encryptedNodeNum;

    @NonNull
    @ColumnInfo(name = "dest_id")
    private String destId;

    @ColumnInfo(name = "file_id")
    private int fileId;

    @ColumnInfo(name = "node_type")
    private String type;

    @ColumnInfo(name = "share_type")
    @NonNull
    private String shareType;

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "sender_info")
    private String senderInfo;

    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
    private byte[] data;

    @ColumnInfo(name = "cipher_data", typeAffinity = ColumnInfo.BLOB)
    private byte[] cipher_data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShareType() {
        return shareType;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(String senderInfo) {
        this.senderInfo = senderInfo;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

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

    @NonNull
    public String getDestId() {
        return destId;
    }

    public void setDestId(@NonNull String destId) {
        this.destId = destId;
    }

    public Shares(String id, int fileId, String type, String shareType, int status, String senderInfo, byte[] data, String destId) {
        this.id = id;
        this.fileId = fileId;
        this.type = type;
        this.shareType = shareType;
        this.status = status;
        this.senderInfo = senderInfo;
        this.data = data;
        this.destId = destId;
    }
}
