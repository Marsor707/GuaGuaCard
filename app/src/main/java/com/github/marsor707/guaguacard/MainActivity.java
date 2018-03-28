package com.github.marsor707.guaguacard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GuaGuaCard guaGuaCard = findViewById(R.id.guagua);
        guaGuaCard.setGuaGuaListener(new GuaGuaCard.GuaGuaListener() {
            @Override
            public void onComplete() {
                Log.d(TAG, "刮刮卡完成");
            }
        });
    }
}
