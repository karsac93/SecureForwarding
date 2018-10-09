package com.example.home.secureforwarding.DataHandler;

import android.util.Log;

import com.backblaze.erasure.ReedSolomon;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.Shares;
import com.example.home.secureforwarding.KeyHandler.AEScrypto;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.example.home.secureforwarding.DataHandler.DataConstant.BYTES_IN_INT;
import static com.example.home.secureforwarding.DataHandler.DataConstant.DATA_SHARDS;
import static com.example.home.secureforwarding.DataHandler.DataConstant.PARITY_SHARDS;
import static com.example.home.secureforwarding.DataHandler.DataConstant.TOTAL_SHARDS;

public class CreateDataShares {

    private String deviceID;
    private String nodeType;
    private AppDatabase database;
    private byte[] fileByte;
    private byte[] aesKey;
    byte[] signature;
    String destId;

    private static final String TAG = CreateDataShares.class.getSimpleName();
    SingletoneECPRE ecpr = SingletoneECPRE.getInstance();

    public CreateDataShares(String deviceID, String nodeType, AppDatabase database, byte[]fileByte, byte[]aesKey, String destId) {
        this.deviceID = deviceID;
        this.nodeType = nodeType;
        this.database = database;
        this.fileByte = fileByte;
        this.aesKey = aesKey;
        this.destId = destId;
    }

    public byte[] generateDataShares(){
        byte[][] ecpr_keys = ecpr.GetKey(0);
        byte[] pvt_key = ecpr_keys[0];
        byte[] encodedVal = new AEScrypto().Encrypt(aesKey, fileByte);
        Log.d(TAG, "Encrypted block length:" + encodedVal.length);
        int fileSize = encodedVal.length;
        int storedSize = fileSize + BYTES_IN_INT;
        final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;

        final int bufferSize = shardSize * DATA_SHARDS;
        final byte[] allBytes = new byte[bufferSize];
        ByteBuffer.wrap(allBytes).putInt(fileSize);
        InputStream is = new ByteArrayInputStream(encodedVal);
        try {
            int bytesRead = is.read(allBytes, BYTES_IN_INT, fileSize);
            Log.d(TAG, "Bytes read:" + bytesRead);
            if (bytesRead != fileSize) {
                throw new IOException("not enough bytes read");
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[][] shards = new byte[TOTAL_SHARDS][shardSize];

        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
        }

        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, shardSize);

        Shares shares = null;
        for(int i=0; i<shards.length; i++) {
            signature = ecpr.SignMessage(shards[i], pvt_key);
            shares = new Shares(deviceID, i, KeyConstant.OWNER_TYPE, DataConstant.DATA_TYPE, KeyConstant.NOT_SENT_STATUS,
                    null, shards[i], destId);
            database.dao().insertDataShares(shares);
        }
        return signature;
    }


}
