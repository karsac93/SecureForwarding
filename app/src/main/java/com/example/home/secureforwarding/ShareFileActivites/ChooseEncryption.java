package com.example.home.secureforwarding.ShareFileActivites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import org.apache.commons.lang3.SerializationUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.home.secureforwarding.MainActivity.DEVICE_ID;

public class ChooseEncryption extends AppCompatActivity {

    public static final String TAG = ChooseEncryption.class.getSimpleName();

    @BindView(R.id.nodespinner)
    Spinner nodeSpinner;

    @BindView(R.id.generateproxy)
    Button generateProxyButton;

    AppDatabase database;
    KeyShares share;
    String deviceID;

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
        Log.d(TAG, "Shares obtained in new Activity:" + share.getType());

        database = AppDatabase.getAppDatabase(this);
        List<KeyStore> keystore = database.dao().getKeyStores();
        ArrayAdapter<KeyStore> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keystore);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(adapter);

        deviceID = SharedPreferenceHandler.getStringValues(this, DEVICE_ID);
    }

    /**
     * A proxy cipher for the selected share is created and updated in the database
     */
    @OnClick(R.id.generateproxy)
    public void generateProxy() {
        KeyStore keystoreObj = (KeyStore) nodeSpinner.getSelectedItem();
        if (keystoreObj == null) {
            Toast.makeText(this, "List is empty, try again later!", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] publicKey = keystoreObj.getPublicKey();
        if (publicKey == null || publicKey.length == 0) {
            Toast.makeText(this, "Something wrong! Not able to fetch public key of this device!", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] invKey = ecpre.invKey;
        byte[] pubKey = ecpre.pubKey;
        if (share.getEncryptedNodeNum() == null || share.getEncryptedNodeNum().length() == 0) {
            byte[] encrypted_data = ecpre.Encryption(share.getData());
            share.setData(encrypted_data);
        }

        byte[] proxyKey = ecpre.GenerateProxyKey(invKey, publicKey);
        byte[] proxyReEncyption = ecpre.ReEncryption(pubKey, proxyKey);
        share.setCipher_data(proxyReEncyption);

        share.setEncryptedNodeNum(keystoreObj.getId());
        database.dao().updateKeyShare(share);
        Toast.makeText(this, "A proxy key is generated with chosen node for this share!", Toast.LENGTH_SHORT).show();
    }
}
