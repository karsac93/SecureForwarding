package com.example.home.secureforwarding.KeyHandler;

import android.util.Log;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.KeyShares;

import java.math.BigInteger;
import java.util.ArrayList;

import static com.example.home.secureforwarding.KeyHandler.KeyConstant.KEY_TYPE;
import static com.example.home.secureforwarding.KeyHandler.KeyConstant.NOT_SENT_STATUS;
import static com.example.home.secureforwarding.KeyHandler.KeyConstant.SENT_STATUS;

public class CreateKeyShares {
    private String device_msg_id;
    private String nodeType;
    private AppDatabase database;
    private byte[] key;
    private byte[][] secrets;
    private String destId;
    String placeholderImage;


    private static final String TAG = CreateKeyShares.class.getSimpleName();

    /**
     * @param device_msg_id - device + msg id
     * @param nodeType      - Type refers to owner, Inter or destination
     * @param database      - instance of database
     * @param key           - AES key
     * @param secrets       - A sign byte[] of one of the data fragment
     * @param destId        - Destination id of this share
     */
    public CreateKeyShares(String device_msg_id, String nodeType, AppDatabase database, byte[] key, byte[][] secrets, String destId) {
        this.device_msg_id = device_msg_id;
        this.nodeType = nodeType;
        this.database = database;
        this.key = key;
        this.secrets = secrets;
        this.destId = destId;
    }

    /**
     * Responside for creating the shares
     */
    public void generateKeyShares() {
        AEScrypto aesCrypto = new AEScrypto();
        Log.d(TAG, "---------Key---------:" + new String(key));

        // Based on K, the size of hidden msg is calculated
        RSecretShare rss = new RSecretShare(KeyConstant.keyShareK, KeyConstant.keyShareN);
        BigInteger[] hiddenInfo = new BigInteger[KeyConstant.keyShareK - 2];
        hiddenInfo[0] = new BigInteger(1, secrets[0]);
        hiddenInfo[1] = new BigInteger(1, secrets[1]);

        SecretShare[] shares = rss.CreateShare(new BigInteger(1, key), hiddenInfo);
        MerkleHashTree hashTree = new MerkleHashTree();
        ArrayList<byte[]> keyShares = new ArrayList<>();
        for (int i = 0; i < shares.length; i++) {
            byte[] thisShare = shares[i].getShare().toByteArray();
            Log.d(TAG, "this share length:" + thisShare.length);
            byte[] keyShare = new byte[KeyConstant.keyByteLenght];
            if (thisShare.length > KeyConstant.keyByteLenght) {
                System.arraycopy(thisShare, thisShare.length - KeyConstant.keyByteLenght, keyShare, 0, KeyConstant.keyByteLenght);
            } else {
                System.arraycopy(thisShare, 0, keyShare, 0, thisShare.length);
            }
            System.arraycopy(aesCrypto.Hash(keyShare), 0, hashTree.leafNodes.get(i).hash, 0, AEScrypto.hashLenght);
            byte[] fullKeyShare = new byte[KeyConstant.keyByteLenght + AEScrypto.hashLenght * KeyConstant.treeHeight];
            System.arraycopy(keyShare, 0, fullKeyShare, 0, KeyConstant.keyByteLenght);
            keyShares.add(fullKeyShare);
        }

        //Merkle hash tree is calculated and relevant information is added with each key share
        hashTree.CalculateHash(hashTree.root);
        for (int i = 0; i < keyShares.size(); i++) {
            MerkleHashTree.TreeNode node = hashTree.leafNodes.get(i);
            for (int j = 0; j < KeyConstant.treeHeight; j++) {
                System.arraycopy(node.sibling.hash, 0, keyShares.get(i), 32 + j * AEScrypto.hashLenght, AEScrypto.hashLenght);
                node = node.parent;
            }
        }

        //each share is encrypted using EC and updated in the database
        KeyShares dbShare = null;
        for (int i = 0; i < shares.length; i++) {
            boolean flag = false;
            byte[] dest_public_key = database.dao().getPublicKey(destId);
            if (dest_public_key != null) {
                flag = true;
                byte[] proxy_key = SingletoneECPRE.getInstance().GenerateProxyKey(SingletoneECPRE.getInstance().invKey, dest_public_key);
                byte[] reEncypt = SingletoneECPRE.getInstance().ReEncryption(SingletoneECPRE.getInstance().pubKey, proxy_key);
                byte[] cipher_data = SingletoneECPRE.getInstance().Encryption(keyShares.get(i));
                dbShare = new KeyShares(device_msg_id, destId, shares[i].getNumber(), nodeType,
                        KEY_TYPE, NOT_SENT_STATUS, "NA", cipher_data, destId, reEncypt);
            }
            if (flag == false)
                dbShare = new KeyShares(device_msg_id, destId, shares[i].getNumber(), nodeType,
                        KEY_TYPE, NOT_SENT_STATUS, "NA", keyShares.get(i), "NA", null);

//            if (i == 0 || i == 1) {
//                KeyShares dbShare1 = new KeyShares("4_1", String.valueOf(15), shares[i].getNumber(), KeyConstant.INTER_TYPE,
//                        KEY_TYPE, NOT_SENT_STATUS, null, keyShares.get(i), null, null);
//                database.dao().insertKeyShares(dbShare1);
//            } else if (i == 2 || i == 3) {
//                KeyShares dbShare1 = new KeyShares("4_3", String.valueOf(2), shares[i].getNumber(), KeyConstant.INTER_TYPE,
//                        KEY_TYPE, NOT_SENT_STATUS, null, keyShares.get(i), null, null);
//                database.dao().insertKeyShares(dbShare1);
//            } else if (i == 4 || i == 5) {
//                KeyShares dbShare1 = new KeyShares("5_1", String.valueOf(10), shares[i].getNumber(), KeyConstant.INTER_TYPE,
//                        KEY_TYPE, NOT_SENT_STATUS, null, keyShares.get(i), null, null);
//                database.dao().insertKeyShares(dbShare1);
//            } else if (i == 6 || i == 7) {
//                if(database.dao().checkCompleteFileRowExistsForMsg("10_2", KeyConstant.DEST_TYPE) == 0){
//                    CompleteFiles completeFiles = new CompleteFiles("10_2",
//                            KeyConstant.DEST_TYPE, device_msg_id.substring(0, device_msg_id.indexOf("_")), placeholderImage);
//                    database.dao().insertCompleteFile(completeFiles);
//                }
//                KeyShares dbShare1 = new KeyShares("5_2", String.valueOf(1), shares[i].getNumber(), KeyConstant.DEST_TYPE,
//                        KEY_TYPE, NOT_SENT_STATUS, null, keyShares.get(i), null, null);
//                database.dao().insertKeyShares(dbShare1);
//            }

//            KeyShares dbshare1;
//            if (shares[i].getNumber() > 7)
//                dbshare1 = new KeyShares("5_1", String.valueOf(3), shares[i].getNumber(),
//                        KeyConstant.INTER_TYPE, KEY_TYPE, SENT_STATUS,
//                        "4", keyShares.get(i), null, null);
//            else
//                dbshare1 = new KeyShares("5_1", String.valueOf(3), shares[i].getNumber(),
//                        KeyConstant.INTER_TYPE, KEY_TYPE, NOT_SENT_STATUS,
//                        null, keyShares.get(i), null, null);
//            database.dao().insertKeyShares(dbshare1);
            database.dao().insertKeyShares(dbShare);
        }
    }

    public void setPlaceholderImage(String placeholderImage) {
        this.placeholderImage = placeholderImage;
    }
}
