package com.example.home.secureforwarding;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.GoogleNearbySupports.NearbyService;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.ShareFileActivites.ShareFilesActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String DEVICE_ID = "deviceID";
    private static final int REQ_PERMISSION = 1;
    public static final int CAMERA_REQUEST = 2;
    private static boolean reqBool = false;
    private static final String IMG_NUM_KEY = "image_num";
    public static final String INTENT_IMG = "imgFile";
    public static final String DATA_DECODE_SKIP = "data_decode";
    public static final String PLACEHOLDER_IMAGE = "placeholder";

    /**
     * File - create a file before taking picture and save it for performing data sharing
     */
    File file;
    Intent intent;

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION
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

    @BindView(R.id.operations)
    TextView operationsTxt;

    @BindView(R.id.enable)
    Button nearbyEnable;

    @BindView(R.id.disable)
    Button nearbyDisable;

    @BindView(R.id.netMsg)
    TextView internetMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        checkDeviceId();
        initializeEcpre();
        if (SharedPreferenceHandler.getStringValues(this, PLACEHOLDER_IMAGE).length() == 0) {
            checkWritePermission();
        }
    }

    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0024);
        } else {
            createAndSavePlaceHolder();
        }
    }

    private void createAndSavePlaceHolder() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder1);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/SecureForwarding/");
        myDir.mkdirs();
        String fname = "placeholder.png";
        File placeholderFile = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (placeholderFile.exists())
            placeholderFile.delete();
        try {
            placeholderFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(placeholderFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            SharedPreferenceHandler.setStringValues(this, PLACEHOLDER_IMAGE, placeholderFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initializeEcpre() {
        InitializeEcpr initializeEcpr = new InitializeEcpr(this);
        initializeEcpr.run();
    }

    private void checkDeviceId() {
        if (SharedPreferenceHandler.getStringValues(this, DEVICE_ID).length() == 0) {
            displayDeviceid.setText("Device ID: Please set device ID");
            deviceIdBtn.setVisibility(View.VISIBLE);
            internetMsg.setVisibility(View.VISIBLE);
            setVisibilityToElements(View.GONE);
        } else {
            displayDeviceid.append(SharedPreferenceHandler.getStringValues(this, DEVICE_ID));
            deviceIdBtn.setVisibility(View.GONE);
            internetMsg.setVisibility(View.GONE);
            setVisibilityToElements(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.sf_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppDatabase database = AppDatabase.getAppDatabase(this);
        switch (item.getItemId()) {
            case R.id.reset_all:
                SharedPreferences sharedPreferences = SharedPreferenceHandler.getSharedPreferences(this);
                sharedPreferences.edit().clear().commit();
                database.clearAllTables();
                checkDeviceId();
                initializeEcpre();
                break;
            case R.id.reset_msg:
                Log.d(TAG, "Good inside reset msg");
                database = AppDatabase.getAppDatabase(this);
                database.clearAllTables();
                break;
            case R.id.data_decoding:
                Log.d(TAG, "Good inside data decoding");
                final View view = getLayoutInflater().inflate(R.layout.custom_popup, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Action");
                builder.setView(view);
                final RadioGroup radioGroup = view.findViewById(R.id.radioGroup2);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean dataSkip = false;
                        int id = radioGroup.getCheckedRadioButtonId();
                        if (id == R.id.skipBtn)
                            dataSkip = true;
                        SharedPreferenceHandler.setBooleanValue(MainActivity.this,
                                DATA_DECODE_SKIP, dataSkip);
                        Log.d(TAG, "Value selected is :" + dataSkip);
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
                break;

        }
        return true;
    }

    /**
     * Once the device id is set, visibility of other elements are modified
     *
     * @param view_type
     */
    private void setVisibilityToElements(int view_type) {
        cameraBtn.setVisibility(view_type);
        ownMsg.setVisibility(view_type);
        interMsg.setVisibility(view_type);
        destMsg.setVisibility(view_type);
        operationsTxt.setVisibility(view_type);
        nearbyEnable.setVisibility(view_type);
        nearbyDisable.setVisibility(view_type);
    }

    /**
     * All retrieved msgs, for which the device is destination is displayed
     */
    @OnClick(R.id.destMsg)
    public void showDestImage() {
        intent = new Intent(this, CompleteFileActivity.class);
        intent.putExtra(CompleteFileActivity.DISPLAY_INFO, KeyConstant.DEST_TYPE);
        startActivity(intent);
    }

    /**
     * ALl own msgs, along with their key and data fragments are displayed
     */
    @OnClick(R.id.ownMsg)
    public void showOwnImages() {
        intent = new Intent(this, CompleteFileActivity.class);
        intent.putExtra(CompleteFileActivity.DISPLAY_INFO, KeyConstant.OWNER_TYPE);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Permission to access camera is checked and camera intent is called if premission is given
     * else, request permission and then take pic and save it
     */
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

    /**
     * Shows all the intermediate msg received from other devices
     */
    @OnClick(R.id.intermsg)
    public void showInterMsgs() {
        intent = new Intent(this, ShareFilesActivity.class);
        intent.putExtra(ShareFilesActivity.INTENT_ACTION, ShareFilesActivity.INTER_ACTION);
        startActivity(intent);
    }

    /**
     * Get the URI from already created file for this image and starts the camera intent
     */
    private void saveImage() {
        Uri imageUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode != RESULT_CANCELED) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(INTENT_IMG, file);
                startActivity(intent);
            } else {
                file.delete();
            }
        }
    }

    /**
     * Start the Google Nearby Connections service
     */

    @OnClick(R.id.enable)
    public void startGoogleNearbyService() {
        Toast.makeText(this, "Searching for nearby devices!", Toast.LENGTH_SHORT).show();
        intent = new Intent(this, NearbyService.class);
        startService(intent);
    }

    @OnClick(R.id.disable)
    public void stopGoogleNearbyService() {
        Toast.makeText(this, "Device no longer connects or visible to other device!",
                Toast.LENGTH_SHORT).show();
        intent = new Intent(this, NearbyService.class);
        stopService(intent);
    }

    /**
     * Creates the file for camera intent to save it, which will be used later for fragmentation too
     */
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

    /**
     * Checks permission to modify SD card and access camera
     *
     * @param context
     * @param permissions
     * @return
     */
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
            case 0024:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createAndSavePlaceHolder();
                } else {
                    Toast.makeText(this, "Provide the write permission to create placeholder image!"
                            , Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Device id is requested from the user
     */
    @OnClick(R.id.deviceId)
    public void requestDeviceID() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("counter");
        final boolean[] flag = new boolean[1];
        flag[0] = true;
        final int[] val = new int[1];
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (flag[0]) {
                    flag[0] = false;
                    val[0] = Integer.parseInt(dataSnapshot.getValue().toString());
                    reference.setValue(val[0] + 1);
                    database.goOffline();
                    SharedPreferenceHandler.setStringValues(MainActivity.this, DEVICE_ID, String.valueOf(val[0]));
                    setHomeScreen(val[0]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setHomeScreen(int val) {
        displayDeviceid.setText("Device ID: " + val);
        internetMsg.setVisibility(View.GONE);
        deviceIdBtn.setVisibility(View.GONE);
        setVisibilityToElements(View.VISIBLE);
    }

    class InitializeEcpr implements Runnable {
        Context context;

        public InitializeEcpr(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            SingletoneECPRE ecpre = SingletoneECPRE.getInstance();
            if (SharedPreferenceHandler.getStringValues(context, SingletoneECPRE.PREF_EC_PARAM).length() == 0) {
                ecpre = SingletoneECPRE.getInstance();
                ecpre.initialize(true, context);
            } else {
                ecpre.initialize(false, context);
            }
        }
    }
}
