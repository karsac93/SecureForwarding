package com.example.home.secureforwarding.KeyHandler;

import android.util.Log;

import com.example.home.secureforwarding.Entities.KeyShares;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class DecipherKeyShare {
    public static final String TAG = DecipherKeyShare.class.getSimpleName();
    List<KeyShares> obtainedShares;
    List<SecretShare> secretShares = new ArrayList<>();

    public DecipherKeyShare(List<KeyShares> keyShares) {
        this.obtainedShares = keyShares;
        decipher();
    }

    private void decipher() {
        for (KeyShares data : obtainedShares) {
            byte[] keyShare = new byte[KeyConstant.keyByteLenght];
            System.arraycopy(data.getData(), 0, keyShare, 0, KeyConstant.keyByteLenght);
            byte[] hash = new byte[AEScrypto.hashLenght];
            System.arraycopy(new AEScrypto().Hash(keyShare), 0, hash, 0, AEScrypto.hashLenght);
            for (int j = 0; j < KeyConstant.treeHeight; j++) {
                byte[] siblingHash = new byte[AEScrypto.hashLenght];
                System.arraycopy(data.getData(), KeyConstant.keyByteLenght + j * AEScrypto.hashLenght, siblingHash, 0, AEScrypto.hashLenght);

                byte[] concatedHash = new byte[2 * AEScrypto.hashLenght];
                if ((j == 0 && (data.getFileId() - 4) % 2 == 0) || (j > 0 && ((data.getFileId() - 4) / (j * 2)) % 2 == 0)) {
                    System.arraycopy(hash, 0, concatedHash, 0, AEScrypto.hashLenght);
                    System.arraycopy(siblingHash, 0, concatedHash, AEScrypto.hashLenght, AEScrypto.hashLenght);
                } else {
                    System.arraycopy(siblingHash, 0, concatedHash, 0, AEScrypto.hashLenght);
                    System.arraycopy(hash, 0, concatedHash, AEScrypto.hashLenght, AEScrypto.hashLenght);
                }
                System.arraycopy(new AEScrypto().Hash(concatedHash), 0, hash, 0, AEScrypto.hashLenght);
            }
            SecretShare thisShare = new SecretShare(data.getFileId(), new BigInteger(1, keyShare));
            try {
                thisShare.setHash(new String(hash, "UTF-8"));
                Log.d(TAG, "Key Hash:" + thisShare.getHash());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            secretShares.add(thisShare);
        }
        SecretShare[] secretShares1 = new SecretShare[secretShares.size()];
        int i=0;
        for(SecretShare secretShare : secretShares){
            secretShares1[i] = secretShare;
            i++;
        }
        RSecretShare rss = new RSecretShare(KeyConstant.keyShareK, KeyConstant.keyShareN);
        BigInteger[] retrievedInfo = rss.ReconstructShare(secretShares1);
        for(BigInteger info : retrievedInfo){
            byte[] byteInfo = info.toByteArray();
            Log.d(TAG, "---------Hidden message---------:" + new String(byteInfo));
        }
    }
}
