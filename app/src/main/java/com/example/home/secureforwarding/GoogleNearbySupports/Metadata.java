package com.example.home.secureforwarding.GoogleNearbySupports;

import java.io.Serializable;

public class Metadata implements Serializable {
    String deviceId;
    byte[] devicePubKey;

    public Metadata(String deviceId, byte[] devicePubKey) {
        this.deviceId = deviceId;
        this.devicePubKey = devicePubKey;
    }
}
