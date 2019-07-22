package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import org.apache.commons.lang3.SerializationUtils;

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
    KeyShares share;

    SingletoneECPRE ecpre;
    MyKeySharesRecyclerViewAdapter.OnListKeyFragmentInteractionListener listener;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_encryption);
        ButterKnife.bind(this);

        ecpre = SingletoneECPRE.getInstance(null);

        Bundle bundle = getIntent().getExtras();
        byte[] shareObject = bundle.getByteArray(ShareFilesActivity.SEND_SHARE_KEY);
        share = SerializationUtils.deserialize(shareObject);
        position = bundle.getInt(ShareFilesActivity.POS);
        Log.d(TAG, "Shares obtained in new Activity:" + share.getType());

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


        byte[] proxyKey = ecpre.GenerateProxyKey(ecpre.invKey, publicKey);
        byte[] proxyReEncyption = ecpre.ReEncryption(ecpre.pubKey, proxyKey);

        share.setCipher_data(proxyReEncyption);
        Log.d(TAG, "Keystore obj:" + keystoreObj.getId());
        share.setEncryptedNodeNum(keystoreObj.getId());
        database.dao().updateKeyShare(share);
        Toast.makeText(this, "A proxy key is generated with chosen node for this share!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        byte[] shareByte = SerializationUtils.serialize(share);
        bundle.putByteArray(ShareFilesActivity.SEND_SHARE_KEY, shareByte);
        bundle.putInt(ShareFilesActivity.POS, position);
        intent.putExtras(bundle);
        setResult(ShareFilesActivity.REQ, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
