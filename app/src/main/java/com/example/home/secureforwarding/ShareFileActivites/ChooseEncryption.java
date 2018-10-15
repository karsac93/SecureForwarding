package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.Entities.Shares;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.R;

import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseEncryption extends AppCompatActivity {

    public static final String TAG = ChooseEncryption.class.getSimpleName();

    @BindView(R.id.nodespinner)
    Spinner nodeSpinner;

    @BindView(R.id.generateproxy)
    Button generateProxyButton;

    AppDatabase database;
    Shares share;

    SingletoneECPRE ecpre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_encryption);
        ButterKnife.bind(this);

        ecpre = SingletoneECPRE.getInstance();

        Bundle bundle = getIntent().getExtras();
        byte[] shareObject = bundle.getByteArray(ShareFilesActivity.SEND_SHARE_KEY);
        share = SerializationUtils.deserialize(shareObject);
        Log.d(TAG, "Share obtained in new Activity:" + share.getType());

        database = AppDatabase.getAppDatabase(this);
        List<KeyStore> keystore = database.dao().getKeyStores();
        ArrayAdapter<KeyStore> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keystore);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(adapter);
    }

    /**
     * A proxy cipher for the selected share is created and updated in the database
     */
    @OnClick(R.id.generateproxy)
    public void generateProxy() {
        String nodeId = (String) nodeSpinner.getSelectedItem();
        if (nodeId == null || nodeId.length() == 0) {
            Toast.makeText(this, "List is empty, try again later!", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] publicKey = database.dao().getPublicKey(nodeId);
        if (publicKey == null || publicKey.length == 0) {
            Toast.makeText(this, "Something wrong! Not able to fetch public key of this device!", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] pvtKey = ecpre.pvtKey;
        byte[] pubKey = ecpre.pubKey;

        byte[] proxyKey = ecpre.GenerateProxyKey(pvtKey, publicKey);
        byte[] proxyReEncyption = ecpre.ReEncryption(pubKey, proxyKey);

        share.setCipher_data(proxyReEncyption);
        share.setEncryptedNodeNum(nodeId);
        database.dao().updateKeyShare(share);

    }
}
