package com.example.home.secureforwarding.KeyHandler;

import android.util.Log;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.Shares;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import static com.example.home.secureforwarding.KeyHandler.KeyConstant.KEY_TYPE;
import static com.example.home.secureforwarding.KeyHandler.KeyConstant.NOT_SENT_STATUS;

public class CreateKeyShares {
    private String device_msg_id;
    private String nodeType;
    private AppDatabase database;
    private byte[] key;
    private byte[] sign;
    private String destId;


    private static final String TAG = CreateKeyShares.class.getSimpleName();

    public CreateKeyShares(String device_msg_id, String nodeType, AppDatabase database, byte[] key, byte[] sign, String destId) {
        this.device_msg_id = device_msg_id;
        this.nodeType = nodeType;
        this.database = database;
        this.key = key;
        this.sign = sign;
        this.destId = destId;
    }

    public void generateKeyShares() {
        AEScrypto aesCrypto = new AEScrypto();
        Log.d(TAG, "---------Key---------:" + new String(key));


        RSecretShare rss = new RSecretShare(KeyConstant.keyShareK, KeyConstant.keyShareN);
        BigInteger[] hiddenInfo = new BigInteger[KeyConstant.keyShareK - 2];

        for (int i = 0; i < hiddenInfo.length; i++) {
            byte[] b = new byte[KeyConstant.keyByteLenght];
            new Random().nextBytes(b);
            hiddenInfo[i] = new BigInteger(1, b);
        }

        SecretShare[] shares = rss.CreateShare(new BigInteger(1, key), hiddenInfo);
        MerkleHashTree hashTree = new MerkleHashTree();
        ArrayList<byte[]> keyShares = new ArrayList<>();
        for (int i = 0; i < shares.length; i++) {
            shares[i].setSignature(sign);
            byte[] thisShare = shares[i].getShare().toByteArray();
            byte[] keyShare = new byte[KeyConstant.keyByteLenght];
            if (thisShare.length > KeyConstant.keyByteLenght) {
                System.arraycopy(thisShare, thisShare.length - KeyConstant.keyByteLenght, keyShare, 0, KeyConstant.keyByteLenght);
            } else {
                System.arraycopy(thisShare, 0, keyShare, 0, KeyConstant.keyByteLenght);
            }
            System.arraycopy(aesCrypto.Hash(keyShare), 0, hashTree.leafNodes.get(i).hash, 0, AEScrypto.hashLenght);
            byte[] fullKeyShare = new byte[KeyConstant.keyByteLenght + AEScrypto.hashLenght * KeyConstant.treeHeight];
            System.arraycopy(keyShare, 0, fullKeyShare, 0, KeyConstant.keyByteLenght);
            keyShares.add(fullKeyShare);
        }

        hashTree.CalculateHash(hashTree.root);
        for (int i = 0; i < keyShares.size(); i++) {
            MerkleHashTree.TreeNode node = hashTree.leafNodes.get(i);
            for (int j = 0; j < KeyConstant.treeHeight; j++) {
                System.arraycopy(node.sibling.hash, 0, keyShares.get(i), 32 + j * AEScrypto.hashLenght, AEScrypto.hashLenght);
                node = node.parent;
            }
        }

        Shares dbShare;
        for (int i = 0; i < shares.length; i++) {
            byte[] cipher_data = SingletoneECPRE.getInstance().Encryption(keyShares.get(i));
            dbShare = new Shares(device_msg_id, shares[i].getNumber(), nodeType,
                    KEY_TYPE, NOT_SENT_STATUS, null, cipher_data, destId);
            database.dao().insertKeyShares(dbShare);
        }

        Log.d(TAG, "Total number of inserted shares:" + database.dao().numShares());
    }
}
