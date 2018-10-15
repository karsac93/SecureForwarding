package com.example.home.secureforwarding.KeyHandler;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import org.apache.commons.lang3.SerializationUtils;

import java.security.MessageDigest;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

public class SingletoneECPRE {

    public static final String TAG = SingletoneECPRE.class.getSimpleName();
    public static final String PREF_EC_PARAM = "ec_param";
    public static final String PREF_E_G = "e_g";
    public static final String PREF_E_K = "e_k";
    public static final String PREF_PUB_KEY = "pub_key";
    public static final String PREF_PVT_KEY = "pvt_key";
    public static final String PREF_INV_KEY = "inv_key";


    Field Zr, GT, C;
    Element G, K, Zk;
    Pairing pairing;
    PairingParameters params;

    public int rBits = 160;
    int qBits = 512;
    int plainByteLen = qBits / 8 - 4; //512/8=64;
    int cipherByteLen = qBits / 8 * 2;

    public byte[] pvtKey, pubKey, invKey;

    private static SingletoneECPRE thisObj;

    private SingletoneECPRE() {
    }

    public static SingletoneECPRE getInstance() {
        if (thisObj == null) {
            thisObj = new SingletoneECPRE();
        }
        return thisObj;
    }

    /**
     * Responsible for either creating new configuration or update the variables from
     * shared preferences. This is done to prevent changing of public and private key everytime the
     * app is opened
     *
     * @param flag
     * @param context
     */
    public void initialize(boolean flag, Context context) {
        if (flag) {
            Pairing();
            byte[] byteParams = SerializationUtils.serialize(params);
            setToSharedPreferences(byteParams, PREF_EC_PARAM, context);
            byte[] elementG = G.toBytes();
            setToSharedPreferences(elementG, PREF_E_G, context);
            byte[] elementK = K.toBytes();
            setToSharedPreferences(elementK, PREF_E_K, context);
            byte[][] keys = GenerateKey();
            pvtKey = keys[0];
            pubKey = keys[1];
            invKey = keys[2];
            setToSharedPreferences(pvtKey, PREF_PVT_KEY, context);
            setToSharedPreferences(pubKey, PREF_PUB_KEY, context);
            setToSharedPreferences(invKey, PREF_INV_KEY, context);
            Log.d(TAG, "params:" + params.hashCode());
            Log.d(TAG, "ZR:" + Zr.getOrder());
            Log.d(TAG, "GT:" + GT.getOrder());
            Log.d(TAG, "C:" + C.getOrder());
            Log.d(TAG, "G:" + G.hashCode());
            Log.d(TAG, "K:" + K.hashCode());
            Log.d(TAG, "Zk:" + Zk.hashCode());
        } else {
            byte[] paramsByte = getFromSharedPreferences(PREF_EC_PARAM, context);
            params = SerializationUtils.deserialize(paramsByte);
            pairing = PairingFactory.getPairing(params);
            Zr = pairing.getZr();
            GT = pairing.getGT();
            C = pairing.getG1();
            G = C.newElement();
            G.setFromBytes(getFromSharedPreferences(PREF_E_G, context));
            G = G.getImmutable();
            K = Zr.newElement();
            K.setFromBytes(getFromSharedPreferences(PREF_E_K, context));
            K = K.getImmutable();
            Zk = pairing.pairing(G, G.powZn(K)).getImmutable();
            Zk = Zk.getImmutable();
            pubKey = getFromSharedPreferences(PREF_PUB_KEY, context);
            pvtKey = getFromSharedPreferences(PREF_PVT_KEY, context);
            invKey = getFromSharedPreferences(PREF_INV_KEY, context);
            Log.d(TAG, "params:" + params.hashCode());
            Log.d(TAG, "ZR:" + Zr.getOrder());
            Log.d(TAG, "GT:" + GT.getOrder());
            Log.d(TAG, "C:" + C.getOrder());
            Log.d(TAG, "G:" + G.hashCode());
            Log.d(TAG, "K:" + K.hashCode());
            Log.d(TAG, "Zk:" + Zk.hashCode());
        }
    }

    private void setToSharedPreferences(byte[] value, String key, Context context) {
        String paramsString = Base64.encodeToString(value, Base64.DEFAULT);
        SharedPreferenceHandler.setStringValues(context, key, paramsString);
    }

    private byte[] getFromSharedPreferences(String key, Context context) {
        String value = SharedPreferenceHandler.getStringValues(context, key);
        byte[] byteValue = Base64.decode(value, Base64.DEFAULT);
        return byteValue;
    }

    public void Pairing() {
        PairingParametersGenerator paramGenerator = new TypeACurveGenerator(rBits, qBits);
        params = paramGenerator.generate();
        pairing = PairingFactory.getPairing(params);
        //Bilinear Pairing
        Zr = pairing.getZr();
        GT = pairing.getGT();
        C = pairing.getG1();
        G = C.newRandomElement().getImmutable();
        K = Zr.newRandomElement().getImmutable();
        Zk = pairing.pairing(G, G.powZn(K)).getImmutable();
    }

    public byte[][] GenerateKey() {
        Element pvtKey = Zr.newRandomElement().getImmutable();
        Element pubKey = G.powZn(pvtKey).getImmutable();
        Element invPvt = pvtKey.invert();
        byte[][] keys = {pvtKey.toBytes(), pubKey.toBytes(), invPvt.toBytes()};
        return keys;
    }

    public byte[] GenerateProxyKey(byte[] invPvtA, byte[] pubKeyB) {
        Element elemInvPvtA = Zr.newElement();
        Element elemPubKeyB = C.newElement();
        elemInvPvtA.setFromBytes(invPvtA);
        elemPubKeyB.setFromBytes(pubKeyB);

        Element proxyKeyAB = elemPubKeyB.powZn(elemInvPvtA);
        return proxyKeyAB.toBytes();
    }

    public byte[] Encryption(byte[] plainText) {
        int blockNum = (int) Math.ceil(plainText.length / (double) plainByteLen);
        byte[] byteCipher = new byte[blockNum * cipherByteLen];

        for (int i = 0; i < blockNum; i++) {
            Element E = GT.newElement();
            byte[] plainBlock = new byte[plainText.length - plainByteLen * i < plainByteLen ? plainText.length - plainByteLen * i : plainByteLen];
            System.arraycopy(plainText, plainByteLen * i, plainBlock, 0, plainBlock.length);
            E.setFromBytes(plainBlock);
            Element cipher = Zk.mul(E);
            System.arraycopy(cipher.toBytes(), 0, byteCipher, cipherByteLen * i, cipher.toBytes().length);
        }
        return byteCipher;
    }

    public byte[] ReEncryption(byte[] pubKeyA, byte[] proxyKeyAB) {
        Element elemPubKeyA = C.newElement();
        Element elemProxyKeyAB = C.newElement();
        elemPubKeyA.setFromBytes(pubKeyA);
        elemProxyKeyAB.setFromBytes(proxyKeyAB);

        Element reCipher = elemPubKeyA.powZn(K);
        Element reEncAB = pairing.pairing(reCipher, elemProxyKeyAB);
        return reEncAB.toBytes();
    }

    public byte[] Decryption(byte[] reEncAB, byte[] cipher, byte[] invPvtB) {
        Element elemReEncAB = GT.newElement();
        Element elemInvPvtB = Zr.newElement();
        elemReEncAB.setFromBytes(reEncAB);
        elemInvPvtB.setFromBytes(invPvtB);
        Element reCipher = elemReEncAB.powZn(elemInvPvtB);

        int blockNum = (int) Math.ceil(cipher.length / (double) cipherByteLen);
        byte[] bytePlain = new byte[blockNum * plainByteLen];
        for (int i = 0; i < blockNum; i++) {
            byte[] cipherBlock = new byte[cipherByteLen];
            System.arraycopy(cipher, cipherByteLen * i, cipherBlock, 0, cipherByteLen);
            Element elemCipher = GT.newElement();
            elemCipher.setFromBytes(cipherBlock);
            Element plainText = elemCipher.div(reCipher);

            byte[] bytePlainText = plainText.toBytes();
            int initIndex = 0;
            while (bytePlainText[initIndex] == 0) initIndex++;

            System.arraycopy(plainText.toBytes(), initIndex, bytePlain, plainByteLen * i, plainByteLen);
        }
        return bytePlain;
    }

    public byte[] Hash(byte[] value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = value;
            final byte[] resultByte = messageDigest.digest(bytesOfMessage);
            return resultByte;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Hash Exception:");
        }

        return null;
    }

    public byte[] SignMessage(byte[] cipher, byte[] pvtKey) {
        try {
            byte[] hash = Hash(cipher);
            Element elemHash = pairing.getG1().newElement().setFromHash(hash, 0, hash.length);

            Element elemPvtKey = Zr.newElement();
            elemPvtKey.setFromBytes(pvtKey);
            Element signature = elemHash.powZn(elemPvtKey);
            return signature.toBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public boolean VerifySignature(byte[] cipher, byte[] signature, byte[] pubKey) {
        try {
            byte[] hash = Hash(cipher);
            Element elemHash = pairing.getG1().newElement().setFromHash(hash, 0, hash.length);

            Element elemSig = C.newElement();
            Element elemPubKey = C.newElement();
            elemSig.setFromBytes(signature);
            elemPubKey.setFromBytes(pubKey);

            Element e1 = pairing.pairing(elemSig, G);
            Element e2 = pairing.pairing(elemHash, elemPubKey);

            return e1.isEqual(e2) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
