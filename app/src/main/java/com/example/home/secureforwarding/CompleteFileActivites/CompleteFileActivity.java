package com.example.home.secureforwarding.CompleteFileActivites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.R;
import com.example.home.secureforwarding.ShareFileActivites.ShareFilesActivity;

public class CompleteFileActivity extends AppCompatActivity implements CompleteFileFragment.OnListFragmentInteractionListener {

    public static final String MSG_ID = "msg_id";
    public static final String DISPLAY_INFO = "displayinfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_file);

        Intent intent = getIntent();
        String displayInfo = intent.getStringExtra(DISPLAY_INFO);
        String sharetype = "";

        if(displayInfo.contains(KeyConstant.OWNER_TYPE))
            sharetype = KeyConstant.OWNER_TYPE;
        else
            sharetype = KeyConstant.INTER_TYPE;

        CompleteFileFragment completeFileFragment = new CompleteFileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DISPLAY_INFO, sharetype);
        completeFileFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.completeFragmentContainer, completeFileFragment);
        transaction.commit();
    }

    @Override
    public void onListFragmentInteraction(CompleteFiles item) {
        Intent intent = new Intent(this, ShareFilesActivity.class);
        intent.putExtra(ShareFilesActivity.INTENT_ACTION, ShareFilesActivity.COMPLETE_ACTION);
        intent.putExtra(MSG_ID, item.getId());
        startActivity(intent);
    }
}
