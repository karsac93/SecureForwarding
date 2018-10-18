package com.example.home.secureforwarding.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class CompleteFiles implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String id;

    @NonNull
    @ColumnInfo(name="type")
    private String type;

    @NonNull
    @ColumnInfo(name="dest_id")
    private String destId;

    @ColumnInfo(name = "path")
    private String filePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getDestId() {
        return destId;
    }

    public void setDestId(@NonNull String destId) {
        this.destId = destId;
    }

    public CompleteFiles(@NonNull String id, @NonNull String type, @NonNull String destId, String filePath) {
        this.id = id;
        this.type = type;
        this.destId = destId;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "CompleteFiles{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", destId='" + destId + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}

