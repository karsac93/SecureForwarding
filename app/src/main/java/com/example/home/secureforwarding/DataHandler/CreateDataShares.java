package com.example.home.secureforwarding.DataHandler;

import android.util.Log;

import com.backblaze.erasure.ReedSolomon;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.KeyHandler.AEScrypto;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.example.home.secureforwarding.DataHandler.DataConstant.BYTES_IN_INT;

public class CreateDataShares {
    private String deviceID;
    private String nodeType;
    private AppDatabase database;
    private byte[] fileByte;
    private byte[] aesKey;
    byte[] signature;
    String destId;
    int DATA_SHARDS, PARITY_SHARDS, TOTAL_SHARDS;
    String placeholderImage;

    private static final String TAG = CreateDataShares.class.getSimpleName();

    /**
     * @param deviceID  - unique id of the device
     * @param nodeType  - Owner, inter or destination
     * @param database  - instance of the database
     * @param fileByte  - total file as byte[]
     * @param aesKey    - aes key generated for this msg
     * @param destId    - dest id to which data shares need to be sent
     * @param dataNum   - number of original data shares
     * @param parityNum - number of parity data shares
     */
    public CreateDataShares(String deviceID, String nodeType, AppDatabase database, byte[] fileByte, byte[] aesKey, String destId, int dataNum, int parityNum) {
        this.deviceID = deviceID;
        this.nodeType = nodeType;
        this.database = database;
        this.fileByte = fileByte;
        this.aesKey = aesKey;
        this.destId = destId;
        DATA_SHARDS = dataNum;
        PARITY_SHARDS = parityNum;
        TOTAL_SHARDS = DATA_SHARDS + PARITY_SHARDS;
    }

    /**
     * Method which is responsible to generate data shares
     *
     * @return
     */
    public byte[][] generateDataShares() {

        // original data is encrypted with aes key
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

        // Both data and parity are sent as argument to get the end result
        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, shardSize);

        //data shares are updated in the database
        DataShares shares;
        for (int i = 0; i < shards.length; i++) {
            signature = MainActivity.ecpreObj.SignMessage(shards[i]);
            Log.d(TAG, "shardsize:" + shardSize);
            byte[] withSignatureShard = new byte[shardSize + DataConstant.SIGNATURE_LENGTH];
            System.arraycopy(shards[i], 0, withSignatureShard, 0, shards[i].length);
                System.arraycopy(signature, 0, withSignatureShard, shardSize, signature.length);
            shares = new DataShares(deviceID, destId, i, KeyConstant.OWNER_TYPE, DataConstant.DATA_TYPE, KeyConstant.NOT_SENT_STATUS,
                    "NA", withSignatureShard);
            database.dao().insertDataShares(shares);
        }

        byte[][] secrets = new byte[2][];
        Log.d(TAG, "Checking the number of creations:" + secrets.length);
        Log.d(TAG, "Public key lenght:" + MainActivity.ecpreObj.pubKey);
        secrets[0] = MainActivity.ecpreObj.pubKey;
        byte[] secret = new byte[KeyConstant.keyByteLenght];
        Arrays.fill(secret, (byte) 1);
        byte[] dataInfo;
        String Stringsecret = "dy=" + DATA_SHARDS + ";py=" + PARITY_SHARDS + ";";
        dataInfo = Stringsecret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(dataInfo, 0, secret, 0, dataInfo.length);
        Log.d(TAG, "Important observation:" + secrets.length + " secret msg:" + new String(secret));
        secrets[1] = secret;
        return secrets;
    }

    public void setPlaceholderImage(String placeholderImage) {
        this.placeholderImage = placeholderImage;
    }
}
