package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(primaryKeys = {"msg_id", "file_id"})
public class DataShares implements Serializable {
    @ColumnInfo(name = "msg_id")
    @NonNull
    private String msg_id;

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

    @NonNull
    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(@NonNull String msg_id) {
        this.msg_id = msg_id;
    }

    @NonNull
    public String getDestId() {
        return destId;
    }

    public void setDestId(@NonNull String destId) {
        this.destId = destId;
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

    @NonNull
    public String getShareType() {
        return shareType;
    }

    public void setShareType(@NonNull String shareType) {
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

    public DataShares() {

    }

    @Ignore
    public DataShares(@NonNull String msg_id, @NonNull String destId, int fileId, String type, @NonNull String shareType, int status, String senderInfo, byte[] data) {
        this.msg_id = msg_id;
        this.destId = destId;
        this.fileId = fileId;
        this.type = type;
        this.shareType = shareType;
        this.status = status;
        this.senderInfo = senderInfo;
        this.data = data;
    }
}
