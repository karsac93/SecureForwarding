package com.example.home.secureforwarding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.home.secureforwarding.DataHandler.CreateDataShares;
import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.CompleteFiles;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.KeyHandler.AEScrypto;
import com.example.home.secureforwarding.KeyHandler.CreateKeyShares;
import com.example.home.secureforwarding.KeyHandler.KeyConstant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    static final String STATIC = "static";
    static final String DYNAMIC = "dynamic";
    static final String HIGH = "high";

    @BindView(R.id.imageView)
    public ImageView imageView;

    @BindView(R.id.createsharebtn)
    public Button shareBtn;

    private AppDatabase database;
    private Disposable disposable;
    File file;

    @BindView(R.id.destId)
    AutoCompleteTextView destId;

    @BindViews({R.id.radioGroup, R.id.priorityRadioGroup})
    List<RadioGroup> radioGroups;

    @BindViews({R.id.staticPref, R.id.dynamicPref, R.id.pirorityPref})
    List<RadioButton> prefRadioButtons;

    @BindViews({R.id.lowPref, R.id.mediumPref, R.id.highPref})
    List<RadioButton> priorityRadioButtons;


    int dataNum, parityNum;

    /**
     * An observer object is created and attached to observable to compute CPU intensive tasks
     */
    Observer<File> shareObserver = new Observer<File>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable = d;
            Log.d(TAG, "Inside subscriber");
        }

        @Override
        public void onNext(File value) {
            String dest = destId.getText().toString().trim();
            AEScrypto aesCrypto = new AEScrypto();
            byte[] key = aesCrypto.GenerateKey();
            byte[] fileByte = new byte[(int) value.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(value);
                fileInputStream.read(fileByte);
                CreateDataShares createDataShares = new CreateDataShares(value.getName().
                        substring(0, value.getName().lastIndexOf(".")),
                        KeyConstant.OWNER_TYPE, database, fileByte, key, dest, dataNum, parityNum);
                byte[][] secretMsgs = createDataShares.generateDataShares();
                CreateKeyShares createKeyShares = new CreateKeyShares(value.getName().
                        substring(0, value.getName().lastIndexOf(".")),
                        KeyConstant.OWNER_TYPE, database, key, secretMsgs, dest);
                createKeyShares.generateKeyShares();
                DetailActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(DetailActivity.this, "Key and data shares are created!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        database = AppDatabase.getAppDatabase(this);
        file = (File) getIntent().getSerializableExtra(MainActivity.INTENT_IMG);
        Log.d(TAG, "Obtained file:" + file.getName());

        prefRadioButtons.get(0).setChecked(true);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);

        List<KeyStore> destIds = database.dao().getKeyStores();
        ArrayAdapter<KeyStore> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, destIds);
        destId.setAdapter(adapter);
        if(adapter.getCount() > 0)
            destId.showDropDown();

        radioGroups.get(0).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == prefRadioButtons.get(2).getId()) {
                    radioGroups.get(1).setVisibility(View.VISIBLE);
                } else {
                    radioGroups.get(1).setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Based on various parameters like static and dynamic, appropriate key and date share are generated
     * using RxAndroid multi threading feature
     */
    @OnClick(R.id.createsharebtn)
    public void createKeyDataShares() {
        shareBtn.setEnabled(false);
        String dest = destId.getText().toString().trim();
        if (dest == null || dest.trim().length() == 0) {
            Toast.makeText(DetailActivity.this, "Please enter the destID", Toast.LENGTH_SHORT).show();
            shareBtn.setEnabled(true);
            return;
        }


        int id = radioGroups.get(0).getCheckedRadioButtonId();
        if (id == prefRadioButtons.get(0).getId())
            calculateKN(STATIC, 0L);
        else if (id == prefRadioButtons.get(1).getId())
            calculateKN(DYNAMIC, file.length());
        else {
            int prefId = radioGroups.get(1).getCheckedRadioButtonId();
            if (prefId == priorityRadioButtons.get(0).getId())
                calculateKN(DYNAMIC, file.length());
            else if (prefId == priorityRadioButtons.get(1).getId())
                calculateKN(STATIC, 0L);
            else
                calculateKN(HIGH, 0L);
        }


        CompleteFiles completeFiles = new CompleteFiles(file.getName().
                substring(0, file.getName().lastIndexOf(".")), KeyConstant.OWNER_TYPE,
                destId.getText().toString(), file.getAbsolutePath());
        Log.d(TAG, "destId" + destId.getText().toString());
        database.dao().insertCompleteFile(completeFiles);
        Log.d(TAG, "Main thread name:" + Thread.currentThread().getName());
        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(shareObserver);
    }

    private void calculateKN(String type, Long fileSize) {
        switch (type) {
            case STATIC:
                dataNum = 4;
                parityNum = 2;
                break;
            case DYNAMIC:
                dataNum = (int) (fileSize / 500000) + 1;
                parityNum = dataNum;
                break;
            case HIGH:
                dataNum = 4;
                parityNum = 4;
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
        AppDatabase.destroyInstance();
    }
}
