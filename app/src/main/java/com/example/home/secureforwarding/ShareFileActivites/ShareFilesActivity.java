package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.R;

public class ShareFilesActivity extends AppCompatActivity {
    public static final String TAG = ShareFilesActivity.class.getSimpleName();
    public static final String INTENT_ACTION = "intent_action";
    public static final String COMPLETE_ACTION = "completeMsg";
    public static final String INTER_ACTION = "interMsg";
    public static final String SEND_SHARE_KEY = "sendShare";
    public static final String POS = "position";
    public static final int REQ = 101;
    String id;
    SharesFragment sharesFragment;
    static MyKeySharesRecyclerViewAdapter context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        Intent intent = getIntent();
        String action = intent.getStringExtra(INTENT_ACTION);
        Log.d(TAG, "Action:" + action);
        if (action.contains(COMPLETE_ACTION))
            id = intent.getStringExtra(CompleteFileActivity.MSG_ID);
        else
            id = KeyConstant.INTER_TYPE;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        sharesFragment = new SharesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CompleteFileActivity.MSG_ID, id);
        sharesFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragContainer, sharesFragment);
        fragmentTransaction.commit();

    }





    @Override
    protected void onResume() {
        super.onResume();
    }


}
