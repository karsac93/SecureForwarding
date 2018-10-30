package com.example.home.secureforwarding.GoogleNearbySupports;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.MainActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IncomingMsgHandler {
    private static final String TAG = IncomingMsgHandler.class.getSimpleName();
    Context context;
    SharesPOJO sharesPOJO;
    AppDatabase appDatabase;

    public IncomingMsgHandler(Context context, SharesPOJO sharesPOJO) {
        this.context = context;
        this.sharesPOJO = sharesPOJO;
        appDatabase = AppDatabase.getAppDatabase(context);
        handleMessages(sharesPOJO);
    }

    private void handleMessages(SharesPOJO sharesPOJO) {
        handleCompleteFiles(sharesPOJO.completeFilesToSend);
        handleKeyShares(sharesPOJO.keySharesToSend);
        handleDataShares(sharesPOJO.dataSharesToSend);
    }

    private void handleDataShares(List<DataShares> dataSharesToSend) {
        String deviceId = SharedPreferenceHandler.getStringValues(context, MainActivity.DEVICE_ID);
        for (DataShares dataShare : dataSharesToSend) {
            if (dataShare.getDestId().equals(deviceId)) {
                if (appDatabase.dao().checkCompleteFilealreadyPresent(dataShare.getMsg_id(), KeyConstant.DEST_TYPE, true) == 0) {
                    if(appDatabase.dao().checkCompleteFileRowExistsForMsg(dataShare.getMsg_id(), KeyConstant.DEST_TYPE) == 0){
                        CompleteFiles completeFiles = new CompleteFiles(dataShare.getMsg_id(),
                                KeyConstant.DEST_TYPE, deviceId, SharedPreferenceHandler.getStringValues(context, MainActivity.PLACEHOLDER_IMAGE));
                        appDatabase.dao().insertCompleteFile(completeFiles);
                    }
                    dataShare.setSenderInfo("NA");
                    dataShare.setType(KeyConstant.DEST_TYPE);
                    dataShare.setStatus(KeyConstant.NOT_SENT_STATUS);
                    appDatabase.dao().insertDataShares(dataShare);
                }
            } else {
                dataShare.setSenderInfo("NA");
                dataShare.setType(KeyConstant.DEST_TYPE);
                dataShare.setStatus(KeyConstant.NOT_SENT_STATUS);
                appDatabase.dao().insertDataShares(dataShare);
            }
        }
    }

    private void handleKeyShares(List<KeyShares> keySharesToSend) {
        String deviceId = SharedPreferenceHandler.getStringValues(context, MainActivity.DEVICE_ID);
        for (KeyShares keyshare : keySharesToSend) {
            if (keyshare.getDestId().equals(deviceId)) {
                if (appDatabase.dao().checkCompleteFilealreadyPresent(keyshare.getMsg_id(), KeyConstant.DEST_TYPE, true) == 0) {
                    if(appDatabase.dao().checkCompleteFileRowExistsForMsg(keyshare.getMsg_id(), KeyConstant.DEST_TYPE) == 0){
                        CompleteFiles completeFiles = new CompleteFiles(keyshare.getMsg_id(),
                                KeyConstant.DEST_TYPE, deviceId, SharedPreferenceHandler.getStringValues(context, MainActivity.PLACEHOLDER_IMAGE));
                        appDatabase.dao().insertCompleteFile(completeFiles);
                    }
                    keyshare.setCipher_data(null);
                    keyshare.setEncryptedNodeNum("NA");
                    keyshare.setSenderInfo("NA");
                    keyshare.setType(KeyConstant.DEST_TYPE);
                    keyshare.setStatus(KeyConstant.NOT_SENT_STATUS);
                    appDatabase.dao().insertKeyShares(keyshare);
                }
            } else {
                keyshare.setCipher_data(null);
                keyshare.setEncryptedNodeNum("NA");
                keyshare.setSenderInfo("NA");
                keyshare.setType(KeyConstant.INTER_TYPE);
                keyshare.setStatus(KeyConstant.NOT_SENT_STATUS);
                appDatabase.dao().insertKeyShares(keyshare);
            }
        }
    }

    private void handleCompleteFiles(HashMap<String, String> completeFilesList) {
        String deviceId = null;
        if (completeFilesList.size() > 0)
            deviceId = SharedPreferenceHandler.getStringValues(context, MainActivity.DEVICE_ID);
        Iterator it = completeFilesList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (appDatabase.dao().checkCompleteFilealreadyPresent(pair.getKey().toString(), KeyConstant.DEST_TYPE, true) == 0) {
                appDatabase.dao().deleteKeySharesForMsg(pair.getKey().toString());
                appDatabase.dao().deleteDataSharesForMsg(pair.getKey().toString());
                byte[] imageArray = Base64.decode(pair.getValue().toString(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
                File file = createFile(pair.getKey().toString());
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    byte[] bitmapBytes = bos.toByteArray();
                    FileOutputStream fos = new FileOutputStream(createFile(pair.getKey().toString()));
                    fos.write(bitmapBytes);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                CompleteFiles completeFile = new CompleteFiles(pair.getKey().toString(),
                        KeyConstant.DEST_TYPE, deviceId, file.getAbsolutePath());
                completeFile.setStatus(true);
                appDatabase.dao().insertCompleteFile(completeFile);
            }
        }
    }

    private File createFile(String msg_id) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SecureForwarding/DestMessage/");
        myDir.mkdirs();
        String fname = msg_id + ".jpg";
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
