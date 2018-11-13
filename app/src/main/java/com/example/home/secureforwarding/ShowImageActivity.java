package com.example.home.secureforwarding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.home.secureforwarding.CompleteFileActivites.CompleteFileActivity;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ShowImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        imageView = findViewById(R.id.imageView2);

        String filePath = getIntent().getStringExtra(CompleteFileActivity.FILEPATH);
        if(filePath != null){
            File file = new File(filePath);
            if(file.exists()){
                Picasso.get().load(file).into(imageView);
            }
        }
    }
}
