package com.msi.stockmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.ui.main.overview.OverviewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends AppCompatActivity {
    public static String TAG = LaunchActivity.class.getSimpleName();
    public static final int MINIMUM_STAY_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        long t1 = System.currentTimeMillis();
        new Thread(()->{
            Log.d(TAG, "+++ init start +++");
            Profile.load(this);
            ApiUtil.init(this);
            ColorUtil.init(this);
            AccountUtil.init(this);
            Log.d(TAG, "--- init finish ---");
            long diff = System.currentTimeMillis() - t1;
            if(diff < MINIMUM_STAY_TIME){
                try {
                    Thread.sleep(MINIMUM_STAY_TIME-diff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(()->{
                Log.d(TAG, "launch to overview activity");
                startActivity(new Intent(this, OverviewActivity.class));
                finish();
            });
        }).start();
    }
}