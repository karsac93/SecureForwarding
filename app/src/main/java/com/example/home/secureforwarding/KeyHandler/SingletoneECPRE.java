package com.example.home.secureforwarding.KeyHandler;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class SingletoneECPRE {

    public static final String TAG = SingletoneECPRE.class.getSimpleName();
    public static final String PREF_EC_PARAM = "ec_param";
    public static final String PREF_E_G = "e_g";
    public static final String PREF_E_K = "e_k";
    public static final String PREF_PUB_KEY = "pub_key";
    public static final String PREF_PVT_KEY = "pvt_key";
    public static final String PREF_INV_KEY = "inv_key";

    public byte[] pubKey, invKey, pvtKey;

    private static String curveParams = "type a\n"
            + "q 30578004525567207708454960858563842379267938237087909"
            + "4802196750417234088859005849817627164427132870287758928"
            + "389643819086514567628516919397486795364009499\n"
            + "h 41844639437299448003236508143000930413895794248116383"
            + "0046557793328281367025333536819962825554488661673500\n"
            + "r 730750818665451459101842416358717970580269694977\n"
            + "exp2 159\n" + "exp1 59\n" + "sign1 1\n" + "sign0 1\n";

    @SuppressWarnings("rawtypes")
    Field Zr, GT, C;
    //Zr -> [pvtKey, invPvtKey]
    //C -> [pubKey, proxyKey]
    //GT -> [reEnc]
    Element G, K, Zk;
    Pairing pairing;

    byte[] bytePairing;
    byte[] byteG;
    byte[] byteK;
    byte[] byteZk;

    public int rBits = 160;
    int qBits = 512;
    int plainByteLen = qBits / 8 - 4; //512/8=64;
    int cipherByteLen = qBits / 8 * 2;


    private static SingletoneECPRE thisObj;

    public SingletoneECPRE(Context context) {
        Pairing(context);
    }

    /**
     * Create a static method to get instance.
     */
    public static SingletoneECPRE getInstance(Context context) {
        if (thisObj == null) {
            thisObj = new SingletoneECPRE(context);
            //thisObj.GetPairing();
        }
        return thisObj;
    }

    @SuppressWarnings("rawtypes")
    public void Pairing(Context context) {
        // JPBC Type A pairing generato

        CurveParameters params = new DefaultCurveParameters()
                .load(new ByteArrayInputStream(curveParams.getBytes()));
        pairing = PairingFactory.getPairing(params);

        //Bilinear Pairing
        Zr = pairing.getZr();
        GT = pairing.getGT();
        C = pairing.getG1();
        Element tG = C.newElement();
        Element tK = Zr.newElement();
        Element tZk = pairing.pairing(tG, tG.powZn(tK));

        try {
            byteG = ByteStreams.toByteArray(context.getAssets().open("G"));
            byteK = ByteStreams.toByteArray(context.getAssets().open("K"));
            byteZk = ByteStreams.toByteArray(context.getAssets().open("Zk"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        tG.setFromBytes(byteG);
        G = tG.getImmutable();
        tK.setFromBytes(byteK);
        K = tK.getImmutable();
        tZk.setFromBytes(byteZk);
        Zk = tZk.getImmutable();


        bytePairing = params.toString().getBytes();
        byteG = G.toBytes();
        byteK = K.toBytes();
        byteZk = Zk.toBytes();

        Log.d(TAG, params.toString());
        Log.d(TAG, Base64.encodeToString(byteG, Base64.DEFAULT));
        Log.d(TAG, Base64.encodeToString(byteK, Base64.DEFAULT));
        Log.d(TAG, Base64.encodeToString(byteZk, Base64.DEFAULT));
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


    public byte[] Hash(byte[] value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = value;
            final byte[] resultByte = messageDigest.digest(bytesOfMessage);
            //System.out.println("Hash Length: " + resultByte.length);
            return resultByte;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Hash Exception:");
        }

        return null;
    }

    public byte[] SignMessage(byte[] cipher) {
        try {
            byte[] hash = Hash(cipher);
            Log.d(TAG, "hash:" + new String(hash));
            Element elemHash = C.newElement().setFromHash(hash, 0, hash.length);
            Element elemPvtKey = Zr.newElement();
            elemPvtKey.setFromBytes(pvtKey);
            Element signature = elemHash.powZn(elemPvtKey);
            return signature.toBytes();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public boolean VerifySignature(byte[] cipher, byte[] signature, byte[] pubKey) {
        try {
            byte[] hash = Hash(cipher);
            Log.d(TAG, new String(hash));
            Element elemHash = C.newElement().setFromHash(hash, 0, hash.length);

            Element elemSig = C.newElement();
            Element elemPubKey = C.newElement();
            elemSig.setFromBytes(signature);
            elemPubKey.setFromBytes(pubKey);

            Element e1 = pairing.pairing(elemSig, G);
            Element e2 = pairing.pairing(elemHash, elemPubKey);

            return e1.isEqual(e2) ? true : false;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
}