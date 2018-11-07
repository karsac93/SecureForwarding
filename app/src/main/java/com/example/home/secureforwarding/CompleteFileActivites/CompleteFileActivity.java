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
import com.example.home.secureforwarding.ShowImageActivity;

public class CompleteFileActivity extends AppCompatActivity implements CompleteFileFragment.OnListFragmentInteractionListener, CompleteFileFragment.ShowImageListener {

    public static final String MSG_ID = "msg_id";
    public static final String DISPLAY_INFO = "displayinfo";
    public static final String FILEPATH = "filepath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_file);

        Intent intent = getIntent();
        String displayInfo = intent.getStringExtra(DISPLAY_INFO);
        String sharetype = "";

        if (displayInfo.contains(KeyConstant.OWNER_TYPE))
            sharetype = KeyConstant.OWNER_TYPE;
        else if(displayInfo.contains(KeyConstant.DEST_TYPE))
            sharetype = KeyConstant.DEST_TYPE;
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

    @Override
    public void showImageListener(String filePath) {
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra(FILEPATH, filePath);
        startActivity(intent);
    }
}
