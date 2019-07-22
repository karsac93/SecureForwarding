package com.example.home.secureforwarding.DataHandler;

import android.os.Environment;
import android.util.Log;

import com.backblaze.erasure.ReedSolomon;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.SecretStore;
import com.example.home.secureforwarding.GoogleNearbySupports.IncomingMsgHandler;
import com.example.home.secureforwarding.KeyHandler.AEScrypto;
import com.example.home.secureforwarding.KeyHandler.DecipherKeyShare;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DecipherDataShares {

    private static final String TAG = DecipherKeyShare.class.getSimpleName();
    private static final int BYTES_IN_INT = 4;
    AppDatabase appDatabase;
    SecretStore secretStore;
    List<DataShares> dataShares;
    DecipherKeyShare.CorruptInfo corruptInfoListener;

    public DecipherDataShares(AppDatabase appDatabase, SecretStore secretStore, List<DataShares> dataShares, IncomingMsgHandler incomingMsgHandler) {
        this.appDatabase = appDatabase;
        this.secretStore = secretStore;
        this.dataShares = dataShares;
        corruptInfoListener = incomingMsgHandler;
        decipherDataShards();
    }

    private void decipherDataShards() {
        int k = secretStore.getKnum();
        int n = secretStore.getNnum();
        int total = k + n;
        byte[][] shards = new byte[total][];
        final boolean[] shardsPresent = new boolean[total];
        int shardSize = 0;

        int size = dataShares.size();
        boolean checkCorrupt = false;
        StringBuilder s = new StringBuilder();
        SingletoneECPRE ecpre = MainActivity.ecpreObj;
        for(int i=0; i < dataShares.size(); i++){
            byte[] data = dataShares.get(i).getData();
            shardSize = data.length - DataConstant.SIGNATURE_LENGTH;
            byte[] signature = new byte[DataConstant.SIGNATURE_LENGTH];
            System.arraycopy(data, data.length - DataConstant.SIGNATURE_LENGTH, signature, 0, DataConstant.SIGNATURE_LENGTH);
            //Log.d(TAG, "signature:" + new String(signature));
            byte[] tempData = new byte[shardSize];
            System.arraycopy(data, 0, tempData, 0, shardSize);
            Log.d(TAG, "size of shard:" + shardSize + " Size of signature:" + signature.length);
            boolean flag = ecpre.VerifySignature(tempData, signature, secretStore.getSignature());
            if(flag) Log.d(TAG, "----------------------");
            if(dataShares.get(i).getFileId()==1)
                flag = false;
            else
                flag = true;
            Log.d(TAG, "flag:" + flag + " Msg_id:" + dataShares.get(i).getFileId());
            if(flag) {
                shards[i] = new byte[shardSize];
                System.arraycopy(tempData, 0, shards[i], 0, shardSize);
                shardsPresent[i] = true;
            }
            else{
                checkCorrupt = true;
                size--;
                s.append(dataShares.get(i).getFileId()).append(",");
                appDatabase.dao().deleteDataShare(dataShares.get(i));
            }
        }

        //Log.d(TAG, "checkCorrupt:" + checkCorrupt + " Length:" + shardsPresent.length);
        if(checkCorrupt && size < k){
            Log.d(TAG, "Yes we are inside corrupt!!");
            String nums = s.substring(0, s.length()-1);
            corruptInfoListener.displayCorruptInfo(nums, dataShares.get(0).getMsg_id());
            return;
        }

        for(int i=0; i< total; i++){
            if(!shardsPresent[i]){
                shards[i] = new byte[shardSize];
            }
        }

        ReedSolomon reedSolomon = ReedSolomon.create(k, n);
        reedSolomon.decodeMissing(shards, shardsPresent, 0, shardSize);

        byte[] allBytes = new byte[shardSize * k];
        for(int i=0; i < k; i++){
            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
        }

        byte[] tempEncryptedByte = new byte[allBytes.length - BYTES_IN_INT];
        System.arraycopy(allBytes, BYTES_IN_INT, tempEncryptedByte,0, allBytes.length - BYTES_IN_INT);
        byte[] decryptedVal = new AEScrypto().Decrypt(secretStore.getAesKey(), tempEncryptedByte);

        File file = createFile(dataShares.get(0).getMsg_id());
        Log.d(TAG, "file obtained:" + file.getAbsolutePath());
        try {
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(decryptedVal);
            outputStream.flush();
            outputStream.close();

            CompleteFiles completeFiles = appDatabase.dao().getCompleteFileforMsg(dataShares.get(0).getMsg_id());
            completeFiles.setFilePath(file.getAbsolutePath());
            completeFiles.setStatus(true);
            secretStore.setStatus(true);
            appDatabase.dao().updateCompleteFile(completeFiles);
            appDatabase.dao().updateSecretStore(secretStore);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private File createFile(String msg_id) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SecureForwarding/DestMessage/");
        myDir.mkdirs();
        String fname = msg_id+ ".jpg";
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}

