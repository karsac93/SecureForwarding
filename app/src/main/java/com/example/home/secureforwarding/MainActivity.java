package com.example.home.secureforwarding;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.ShareFileActivites.ShareFilesActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DEVICE_ID = "deviceID";
    private static final int REQ_PERMISSION = 1;
    public static final int CAMERA_REQUEST = 2;
    private static boolean reqBool = false;
    private static final String IMG_NUM_KEY = "image_num";
    private String deviceId;
    public static final String INTENT_IMG = "imgFile";
    File file;
    Intent intent;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    @BindView(R.id.deviceId)
    public Button deviceIdBtn;

    @BindView(R.id.textView)
    public TextView displayDeviceid;

    @BindView(R.id.cameraBtn)
    public Button cameraBtn;

    @BindView(R.id.ownMsg)
    Button ownMsg;

    @BindView(R.id.intermsg)
    Button interMsg;

    @BindView(R.id.destMsg)
    Button destMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (SharedPreferenceHandler.getStringValues(this, DEVICE_ID).length() == 0) {
            displayDeviceid.append("Please set device ID");
        } else {
            displayDeviceid.append(SharedPreferenceHandler.getStringValues(this, DEVICE_ID));
        }

        if (!displayDeviceid.getText().toString().contains("Please set device ID")) {
            cameraBtn.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.destMsg)
    public void showDestImage(){
        intent = new Intent(this, CompleteFileActivity.class);
        intent.putExtra(CompleteFileActivity.DISPLAY_INFO, KeyConstant.DEST_TYPE);
        startActivity(intent);
    }

    @OnClick(R.id.ownMsg)
    public void showOwnImages(){
        intent = new Intent(this, CompleteFileActivity.class);
        intent.putExtra(CompleteFileActivity.DISPLAY_INFO, KeyConstant.OWNER_TYPE);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.cameraBtn)
    public void checkPermission() {
        if (hasPermissions(this, PERMISSIONS) == false) {
            Log.d(TAG, "Requesting Permission");
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_PERMISSION);
        } else {
            createFile();
            saveImage();
        }

    }

    @OnClick(R.id.intermsg)
    public void showInterMsgs(){
        intent = new Intent(this, ShareFilesActivity.class);
        intent.putExtra(ShareFilesActivity.INTENT_ACTION, ShareFilesActivity.INTER_ACTION);
        startActivity(intent);
    }

    private void saveImage() {
        Uri imageUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == CAMERA_REQUEST){
            if(resultCode != RESULT_CANCELED){
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(INTENT_IMG, file);
                startActivity(intent);
            }
            else{
                file.delete();
            }
        }
    }


    private void createFile() {
        String root = Environment.getExternalStorageDirectory().toString();
        int fileNum = SharedPreferenceHandler.getIntValues(this, IMG_NUM_KEY);
        SharedPreferenceHandler.setIntValues(this, IMG_NUM_KEY, fileNum + 1);
        File myDir = new File(root + "/SecureForwarding/OwnMessage/");
        myDir.mkdirs();
        String id = displayDeviceid.getText().toString();
        String fname = id.substring(id.indexOf(":") + 1, id.length()) + "_" + fileNum + ".jpg";
        file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            reqBool = true;
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    reqBool = false;
                    break;
                }
            }
        }
        Log.d(TAG, "Permission:" + reqBool);
        return reqBool;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION:
                Log.d(TAG, "Checking the permission; Inside onRequestPermissionsResult");
                if (grantResults.length > 0) {
                    reqBool = true;
                    for (int results : grantResults) {
                        if (results == PackageManager.PERMISSION_DENIED) {
                            reqBool = false;
                            return;
                        }
                    }
                    createFile();
                    saveImage();
                }
                break;
        }
    }

    @OnClick(R.id.deviceId)
    public void requestDeviceID() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Device ID");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceId = input.getText().toString();
                Log.d(TAG, "Device id entered:" + deviceId);
                SharedPreferenceHandler.setStringValues(MainActivity.this, DEVICE_ID, deviceId);
                displayDeviceid.setText("Device ID:" + deviceId);
                if (!displayDeviceid.getText().toString().contains("Please set device ID")) {
                    cameraBtn.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
