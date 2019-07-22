package com.example.home.secureforwarding.KeyHandler;

import android.util.Log;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.Entities.SecretStore;
import com.example.home.secureforwarding.GoogleNearbySupports.IncomingMsgHandler;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecipherKeyShare {
    public static final String TAG = DecipherKeyShare.class.getSimpleName();
    List<KeyShares> obtainedShares;
    List<SecretShare> secretShares = new ArrayList<>();
    AppDatabase appDatabase;
    CorruptInfo corruptInfoListener;

    public DecipherKeyShare(List<KeyShares> keyShares, AppDatabase appDatabase, IncomingMsgHandler incomingMsgHandler) {
        this.obtainedShares = keyShares;
        this.appDatabase = appDatabase;
        this.corruptInfoListener = incomingMsgHandler;
    }

    public SecretStore decipher() {
        List<String> frequentHash = new ArrayList<>();
        for (KeyShares data : obtainedShares) {
            byte[] keyShare = new byte[KeyConstant.keyByteLenght];
            System.arraycopy(data.getData(), 0, keyShare, 0, KeyConstant.keyByteLenght);
            byte[] hash = new byte[AEScrypto.hashLenght];
            System.arraycopy(new AEScrypto().Hash(keyShare), 0, hash, 0, AEScrypto.hashLenght);
            for (int j = 0; j < KeyConstant.treeHeight; j++) {
                byte[] siblingHash = new byte[AEScrypto.hashLenght];
                System.arraycopy(data.getData(), KeyConstant.keyByteLenght + j * AEScrypto.hashLenght, siblingHash, 0, AEScrypto.hashLenght);

                byte[] concatenatedHash = new byte[2 * AEScrypto.hashLenght];
                if ((j == 0 && (data.getFileId() - 4) % 2 == 0) || (j > 0 && ((data.getFileId() - 4) / (j * 2)) % 2 == 0)) {
                    System.arraycopy(hash, 0, concatenatedHash, 0, AEScrypto.hashLenght);
                    System.arraycopy(siblingHash, 0, concatenatedHash, AEScrypto.hashLenght, AEScrypto.hashLenght);
                } else {
                    System.arraycopy(siblingHash, 0, concatenatedHash, 0, AEScrypto.hashLenght);
                    System.arraycopy(hash, 0, concatenatedHash, AEScrypto.hashLenght, AEScrypto.hashLenght);
                }
                System.arraycopy(new AEScrypto().Hash(concatenatedHash), 0, hash, 0, AEScrypto.hashLenght);
            }
            SecretShare thisShare = new SecretShare(data.getFileId(), new BigInteger(1, keyShare));
            try {
                thisShare.setHash(new String(hash, "UTF-8"));
                Log.d(TAG, "Key Hash:" + thisShare.getHash());
                frequentHash.add(thisShare.getHash());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d(TAG, "Some problem with this keyshare");
            }
            secretShares.add(thisShare);
        }


        boolean flag = false;
        int size = obtainedShares.size();
        StringBuilder s = new StringBuilder();
        String freqHash = getFreqHash(frequentHash);
        List<Integer> corruptNums = new ArrayList<>();
        for(int i=0; i< secretShares.size(); i++){
            SecretShare share = secretShares.get(i);
            Log.d(TAG, share.getHash() + " " + freqHash);
            if(!share.getHash().contains(freqHash)){
                flag = true;
                corruptNums.add(i);
                Log.d(TAG, "deleting corrupt ones:" + share.getNumber() + " " + i);
            }
        }
        Log.d(TAG, "Secretshare size:" + secretShares.size() +
                " CorruptNums size:" + corruptNums.size());
        for(Integer i : corruptNums){
            Log.d(TAG, "key shares being deleted:" + i);
            secretShares.remove(i.intValue());
            KeyShares keyShare = obtainedShares.get(i.intValue());
            obtainedShares.remove(i.intValue());
            appDatabase.dao().deleteKeyShare(keyShare);
        }

        Log.d(TAG, " flag:" + flag + " secretshare size:" + secretShares.size());
        if(flag && size < KeyConstant.keyShareK){
            String nums = s.substring(0, s.length()-1);
            corruptInfoListener.displayCorruptInfo(nums, obtainedShares.get(0).getMsg_id());
            return null;
        }

        SecretShare[] secretShares1 = new SecretShare[secretShares.size()];
        int i = 0;
        for (SecretShare secretShare : secretShares) {
            secretShares1[i] = secretShare;
            i++;
        }

        RSecretShare rss = new RSecretShare(KeyConstant.keyShareK, KeyConstant.keyShareN);
        BigInteger[] retrievedInfo = rss.ReconstructShare(secretShares1);
        byte[] aesKey = retrievedInfo[0].toByteArray();
        Log.d(TAG, "aes Key:" + new String(aesKey));
        String dataShareInfo = new String(retrievedInfo[2].toByteArray(), StandardCharsets.UTF_8);
        dataShareInfo = dataShareInfo.substring(0, dataShareInfo.lastIndexOf(";"));
        Log.d(TAG, "aes Key:" + new String(aesKey) + "data share info:" + dataShareInfo);
        String[] k_n = dataShareInfo.split(";");
        int k = Integer.parseInt(k_n[0].substring(k_n[0].indexOf("=")+1));
        int n = Integer.parseInt(k_n[1].substring(k_n[1].indexOf("=")+1));
        SecretStore secretStore = new SecretStore(obtainedShares.get(0).getMsg_id(),
                k, n, aesKey, false);
        secretStore.setSignature(retrievedInfo[1].toByteArray());
        Log.d(TAG, "k=" + k + " N=" + n);
        appDatabase.dao().insertSecretStore(secretStore);
        Log.d(TAG, "inserted secret store successfully!");
        return secretStore;
    }

    private String getFreqHash(List<String> frequentHash) {
       HashMap<String, Integer> freqStorer = new HashMap<>();
       for(String s : frequentHash){
           if(freqStorer.containsKey(s))
               freqStorer.put(s, freqStorer.get(s) + 1);
           else
               freqStorer.put(s, 1);
       }
       String s = null;
       int largest = 0;
       for(Map.Entry<String, Integer> map : freqStorer.entrySet()){
           if(map.getValue() > largest){
               largest = map.getValue();
               s = map.getKey();
           }
       }
       return s;
    }

    public interface CorruptInfo{
        void displayCorruptInfo(String nums, String msg_id);
    }

}
