package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.DataHandler.DataConstant;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import org.apache.commons.lang3.SerializationUtils;

import static com.example.home.secureforwarding.MainActivity.DEVICE_ID;

public class ShareFilesActivity extends AppCompatActivity implements SharesFragment.OnListKeyFragmentInteractionListener {
    public static final String TAG = ShareFilesActivity.class.getSimpleName();
    public static final String INTENT_ACTION = "intent_action";
    public static final String COMPLETE_ACTION = "completeMsg";
    public static final String INTER_ACTION = "interMsg";
    public static final String SEND_SHARE_KEY = "sendShare";
    String deviceId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        Intent intent = getIntent();
        String action = intent.getStringExtra(INTENT_ACTION);
        Log.d(TAG, "Action:" + action);
        String id = "";
        if (action.contains(COMPLETE_ACTION)) {
            id = intent.getStringExtra(CompleteFileActivity.MSG_ID);
            getSupportActionBar().setTitle("Fragments");
        } else {
            id = KeyConstant.INTER_TYPE;
            getSupportActionBar().setTitle("Fragments");
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SharesFragment sharesFragment = new SharesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CompleteFileActivity.MSG_ID, id);
        sharesFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragContainer, sharesFragment);
        fragmentTransaction.commit();
        deviceId = SharedPreferenceHandler.getStringValues(this, DEVICE_ID);

    }

    @Override
    public void onListFragmentInteraction(DataShares dataShare) {
        KeyShares share = (KeyShares) dataShare;
        if (share.getEncryptedNodeNum() == null || share.getEncryptedNodeNum().equals(deviceId)) {
            Intent intent = new Intent(this, ChooseEncryption.class);
            Bundle bundle = new Bundle();
            byte[] shareByte = SerializationUtils.serialize(share);
            bundle.putByteArray(SEND_SHARE_KEY, shareByte);
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }
}
