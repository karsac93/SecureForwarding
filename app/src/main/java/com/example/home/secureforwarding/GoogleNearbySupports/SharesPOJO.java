package com.example.home.secureforwarding.GoogleNearbySupports;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharesPOJO implements Serializable {
    List<KeyShares> keySharesToSend;
    List<DataShares> dataSharesToSend;
    HashMap<String, String> completeFilesToSend;

    public SharesPOJO(List<KeyShares> keySharesToSend, List<DataShares> dataSharesToSend,
                      HashMap<String, String> completeFiles) {
        this.keySharesToSend = keySharesToSend;
        this.dataSharesToSend = dataSharesToSend;
        this.completeFilesToSend = completeFiles;
    }
}
