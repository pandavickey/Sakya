package com.panda.sakya.example;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.panda.sakya.Sakya;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Sakya.getSingleton().loadApk(new File("/storage/emulated/0/amigo.apk"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
