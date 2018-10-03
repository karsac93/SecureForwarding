package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(primaryKeys = {"id", "file_id", "share_type"})
public class Shares {
    @ColumnInfo(name = "id")
    @NonNull
    private String id;

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

    public Shares(String id, int fileId, String type, String shareType, int status, String senderInfo, byte[] data) {
        this.id = id;
        this.fileId = fileId;
        this.type = type;
        this.shareType = shareType;
        this.status = status;
        this.senderInfo = senderInfo;
        this.data = data;
    }
}
