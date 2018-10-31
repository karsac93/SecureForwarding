package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class SecretStore {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "msg_id")
    String msg_id;

    @ColumnInfo(name = "knum")
    int Knum;

    @ColumnInfo(name = "Nnum")
    int Nnum;

    @ColumnInfo(name = "aesKey", typeAffinity = ColumnInfo.BLOB)
    byte[] aesKey;

    @NonNull
    @ColumnInfo(name = "status")
    boolean status;

    @Ignore
    public SecretStore(@NonNull String msg_id, int knum, int nnum, byte[] aesKey, boolean status) {
        this.msg_id = msg_id;
        this.Knum = knum;
        this.Nnum = nnum;
        this.aesKey = aesKey;
        this.status = status;
    }

    public SecretStore(){

    }

    @NonNull
    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(@NonNull String msg_id) {
        this.msg_id = msg_id;
    }

    public int getKnum() {
        return Knum;
    }

    public void setKnum(int knum) {
        Knum = knum;
    }

    public int getNnum() {
        return Nnum;
    }

    public void setNnum(int nnum) {
        Nnum = nnum;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public void setAesKey(byte[] aesKey) {
        this.aesKey = aesKey;
    }

    @NonNull
    public boolean isStatus() {
        return status;
    }

    public void setStatus(@NonNull boolean status) {
        this.status = status;
    }
}
