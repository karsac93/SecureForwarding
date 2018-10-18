package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class KeyStore {

    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
    private String id;

    @ColumnInfo(name = "public_key", typeAffinity = ColumnInfo.BLOB)
    private byte[] publicKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public KeyStore(String id, byte[] publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return id;
    }
}
