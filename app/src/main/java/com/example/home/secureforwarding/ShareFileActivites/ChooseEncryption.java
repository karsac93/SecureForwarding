package com.example.home.secureforwarding.ShareFileActivites;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.home.secureforwarding.Entities.Shares;
import com.example.home.secureforwarding.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ChooseEncryption extends AppCompatActivity {

    public static final String TAG = ChooseEncryption.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_encryption);
        Bundle bundle = getIntent().getExtras();
        byte[] shareObject = bundle.getByteArray(ShareFilesActivity.SEND_SHARE_KEY);
        Shares share = (Shares) deserialize(shareObject);
        Log.d(TAG, "Share obtained in new Activity:" + share.getType());

    }

    public static Object deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(in);
            return is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
