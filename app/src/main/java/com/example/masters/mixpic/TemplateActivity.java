package com.example.masters.mixpic;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.credenceid.biometrics.BiometricsActivity;

public class TemplateActivity extends BiometricsActivity {
    ImageView Imagefinger;
    ImageView picfinger;



        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_template);

            picfinger = (ImageView)findViewById(R.id.picfinger);
            picfinger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Select Picture "),222);
                    picfinger.setImageDrawable(null);
                }
            });
//            picfinger.setImageDrawable(Imagefinger.findViewById(R.id.imageView));




    }
}
