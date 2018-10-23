package com.example.home.secureforwarding.GoogleNearbySupports;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SharesPOJO implements Serializable {
    List<KeyShares> keySharesToSend;
    List<DataShares> dataSharesToSend;
    List<String> completeFilesToSend;

    public SharesPOJO(List<KeyShares> keySharesToSend, ArrayList<DataShares> dataSharesToSend,
                      List<String> completeFiles) {
        this.keySharesToSend = keySharesToSend;
        this.dataSharesToSend = dataSharesToSend;
        this.completeFilesToSend = completeFiles;
    }
}
