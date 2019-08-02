package com.example.home.secureforwarding.GoogleNearbySupports;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.DetailActivity;
import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class P2PHandler implements Serializable {
    String id;
    byte[] otherPubKey;
    Context context;
    AppDatabase appDatabase;
    SingletoneECPRE singletoneECPRE;
    private static boolean flag = true;

    public P2PHandler(String id, byte[] otherPubKey, Context context) {
        this.id = id;
        this.otherPubKey = otherPubKey;
        this.context = context;
        appDatabase = AppDatabase.getAppDatabase(context);
        singletoneECPRE = MainActivity.ecpreObj;
    }

    public SharesPOJO fetchFilesToSend() {
//        HashMap<String, String> completeFiles = getCompleteFiles();
//        List<KeyShares> keyShares = appDatabase.dao().getKeySharesForThisDevice(KeyConstant.NOT_SENT_STATUS, id, KeyConstant.DEST_TYPE);
//        keyShares.addAll(appDatabase.dao().getKeySharesEncryptedWithDevice(KeyConstant.NOT_SENT_STATUS, id, KeyConstant.DEST_TYPE));
//        keyShares.addAll(appDatabase.dao().getKeySharesForDestDevice(KeyConstant.DEST_TYPE, id, KeyConstant.NOT_SENT_STATUS));
//        for(KeyShares keyShare : keyShares){
//            if(keyShare.getEncryptedNodeNum() == null) {
//                byte[] proxyKey = singletoneECPRE.GenerateProxyKey(singletoneECPRE.invKey, otherPubKey);
//                byte[] renec = singletoneECPRE.ReEncryption(singletoneECPRE.pubKey, proxyKey);
//                keyShare.setCipher_data(renec);
//                keyShare.setEncryptedNodeNum(id);
//            }
//            keyShare.setSenderInfo(id);
//            keyShare.setStatus(KeyConstant.SENT_STATUS);
//            appDatabase.dao().updateKeyShare(keyShare);
//        }
//        List<DataShares> dataShares = getDataShares(id);
//        SharesPOJO shares = new SharesPOJO(keyShares, dataShares, completeFiles);
//        return shares;

        List<KeyShares> keyShares = null;
        List<DataShares> dataShares = null;
        List<KeyShares> test = appDatabase.dao().getFourKeyShares();
        if(test != null && test.size() > 0 &&
                test.get(0).getType().equals(KeyConstant.OWNER_TYPE)){
            if(test.get(0).getStatus() == KeyConstant.NOT_SENT_STATUS){
                keyShares = appDatabase.dao().getFourKeyShares();
                dataShares = appDatabase.dao().getFourDataShares();
                flag = false;
            }
            else{
                keyShares = appDatabase.dao().getOneKeyshare(id);
                dataShares = appDatabase.dao().getOneDataShare();
            }
            for(KeyShares keyShare : keyShares){
                if(keyShare.getEncryptedNodeNum().contains("NA") && appDatabase.dao().getKeyStores().size() <= 1) {
                    byte[] proxyKey = singletoneECPRE.GenerateProxyKey(singletoneECPRE.invKey, otherPubKey);
                    byte[] renec = singletoneECPRE.ReEncryption(singletoneECPRE.pubKey, proxyKey);
                    keyShare.setCipher_data(renec);
                    keyShare.setEncryptedNodeNum(id);
                }
                keyShare.setSenderInfo(id);
                keyShare.setStatus(KeyConstant.SENT_STATUS);
                //appDatabase.dao().updateKeyShare(keyShare);
            }


            for(DataShares dataShare : dataShares){
                dataShare.setStatus(KeyConstant.SENT_STATUS);
                dataShare.setSenderInfo(id);
                //appDatabase.dao().updateDataShare(dataShare);
            }
        }
        else{
            keyShares = appDatabase.dao().getFourKeyShares();
            dataShares = appDatabase.dao().getFourDataShares();
        }


        SharesPOJO shares = new SharesPOJO(keyShares, dataShares, new HashMap<String, String>());
        return shares;
    }

    private List<DataShares> getDataShares(String id) {
        List<DataShares> dataShares = new ArrayList<>();
        dataShares.addAll(appDatabase.dao().getDataSharesForThisDevice(
                KeyConstant.NOT_SENT_STATUS, KeyConstant.DEST_TYPE, id));
        dataShares.addAll(appDatabase.dao().getDataSharesForThisDestDevice(
                KeyConstant.NOT_SENT_STATUS, KeyConstant.DEST_TYPE, id));
        for(DataShares dataShare : dataShares){
            dataShare.setStatus(KeyConstant.SENT_STATUS);
            dataShare.setSenderInfo(id);
            appDatabase.dao().updateDataShare(dataShare);
        }
        return dataShares;
//        return null;

    }

    private HashMap<String, String> getCompleteFiles() {
        HashMap<String, String> mapCompleteFiles = new HashMap<>();
        List<CompleteFiles> completeFiles = appDatabase.dao().getCompleteFilesForDevice(KeyConstant.OWNER_TYPE, id);
        for(CompleteFiles completeFile : completeFiles){
            appDatabase.dao().deleteDataSharesForMsg(completeFile.getId());
            appDatabase.dao().deleteKeySharesForMsg(completeFile.getId());
            appDatabase.dao().deleteCompleteFileId(completeFile);
            //Bitmap bitmap = BitmapFactory.decodeFile(completeFile.getFilePath());
            Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/SecureForwarding/OwnMessage/ 1_6.jpg");
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOS);
            mapCompleteFiles.put(completeFile.getId(), Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT));
        }
        return mapCompleteFiles;
    }


}
