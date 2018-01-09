package com.example.masters.mixpic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
            picfinger.setImageDrawable(Imagefinger.getDrawable());



    }
}
