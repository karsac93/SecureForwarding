package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.DataHandler.DataConstant;
import com.example.home.secureforwarding.Entities.Shares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ShareFilesActivity extends AppCompatActivity implements SharesFragment.OnListKeyFragmentInteractionListener {
    public static final String TAG = ShareFilesActivity.class.getSimpleName();
    public static final String INTENT_ACTION = "intent_action";
    public static final String COMPLETE_ACTION = "completeMsg";
    public static final String INTER_ACTION = "interMsg";
    public static final String SEND_SHARE_KEY = "sendShare";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        Intent intent = getIntent();
        String action = intent.getStringExtra(INTENT_ACTION);
        Log.d(TAG, "Action:" + action);
        String id = "";
        if (action.contains(COMPLETE_ACTION))
            id = intent.getStringExtra(CompleteFileActivity.MSG_ID);
        else
            id = KeyConstant.INTER_TYPE;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SharesFragment sharesFragment = new SharesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CompleteFileActivity.MSG_ID, id);
        sharesFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragContainer, sharesFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onListFragmentInteraction(Shares share) {
        if (share.getEncryptedNodeNum() == null) {
            if (!share.getShareType().contains(DataConstant.DATA_TYPE)) {
                Intent intent = new Intent(this, ChooseEncryption.class);
                Bundle bundle = new Bundle();
                byte[] shareByte = serialize(share);
                bundle.putByteArray(SEND_SHARE_KEY, shareByte);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
