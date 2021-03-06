package com.example.home.secureforwarding.KeyHandler;

import java.math.BigInteger;

public class SecretShare {

    private int number;
    private BigInteger share;
    private String hash;
    private byte[] signature;
    public boolean isVerified;

    public SecretShare(int number, BigInteger share) {
        this.number = number;
        this.share = share;
    }

    public int getNumber() {
        return number;
    }

    public BigInteger getShare() {
        return share;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
