package com.example.home.secureforwarding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.imageView)
    public ImageView imageView;

    @BindView(R.id.createsharebtn)
    public Button shareBtn;

    @BindView(R.id.deviceIds)
    Spinner deviceIds;

    private AppDatabase database;
    private Disposable disposable;
    File file;

    Observer<File> shareObserver = new Observer<File>() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable = d;
            Log.d(TAG, "Inside subscriber");
        }

        @Override
        public void onNext(File value) {
            Log.d(TAG, "OOH OOH, I'm here" + value.getName());
            AEScrypto aesCrypto = new AEScrypto();
            byte[] key = aesCrypto.GenerateKey();
            byte[] fileByte = new byte[(int) value.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(value);
                fileInputStream.read(fileByte);
                CreateDataShares createDataShares = new CreateDataShares(value.getName(), KeyConstant.OWNER_TYPE, database, fileByte, key);
                byte[] sign = createDataShares.generateDataShares();
                CreateKeyShares createKeyShares = new CreateKeyShares(value.getName(), KeyConstant.OWNER_TYPE, database, key, sign);
                createKeyShares.generateKeyShares();
                Log.d(TAG, "Total number of inserted data and key shares:" + database.dao().numShares());
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

        List<KeyStore> keystore = database.dao().getKeyStores();
        ArrayAdapter<KeyStore> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keystore);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceIds.setAdapter(adapter);

        file = (File) getIntent().getSerializableExtra(MainActivity.INTENT_IMG);
        Log.d(TAG, "Obtained file:" + file.getName());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
    }

    @OnClick(R.id.createsharebtn)
    public void createKeyDataShares(){
        CompleteFiles completeFiles = new CompleteFiles();
        completeFiles.setId(file.getName());
        completeFiles.setId(file.getAbsolutePath());
        database.dao().insertCompleteFile(completeFiles);
        Log.d(TAG, "Main thread name:" + Thread.currentThread().getName());
        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(shareObserver);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable !=null)
            disposable.dispose();
        AppDatabase.destroyInstance();
    }
}
