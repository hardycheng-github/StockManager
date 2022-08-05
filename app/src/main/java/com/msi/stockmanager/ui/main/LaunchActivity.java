package com.msi.stockmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleEventObserver;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.databinding.ActivityLaunchBinding;
import com.msi.stockmanager.ui.main.overview.OverviewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends AppCompatActivity {
    public static String TAG = LaunchActivity.class.getSimpleName();
    public static final int MIN_STAY_TIME = 3000;
    public static final int MAX_STAY_TIME = 15000;

    private ActivityLaunchBinding binding;
    private Thread initThread;
    private boolean isInit = false;
    private AccountUtil.AccountUpdateListener listener = accountValue -> {
        isInit = true;
    };

    public LaunchActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().setClickable(true);
        binding.getRoot().setOnClickListener(v->{
            if(isInit) initThread.interrupt();
        });
        binding.version.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        long t1 = System.currentTimeMillis();
        initThread = new Thread(()->{
            Log.d(TAG, "+++ init start +++");
            Profile.load(this);
            ApiUtil.init(this);
            ColorUtil.init(this);
            FormatUtil.init(this);
            AccountUtil.init(this);
            AccountUtil.addListener(listener);
            @TODO("init listener sync")
            try {
                synchronized (listener) {
                    listener.wait(MAX_STAY_TIME);
                }
                Log.d(TAG, "AccountUtil init success!");
            } catch (Exception e){
                Log.w(TAG, "AccountUtil init timeout: " + MAX_STAY_TIME);
            }
            AccountUtil.removeListener(listener);

            isInit = true;
            Log.d(TAG, "--- init finish ---");
            long diff = System.currentTimeMillis() - t1;
            if(diff < MIN_STAY_TIME){
                try {
                    initThread.sleep(MIN_STAY_TIME -diff);
                } catch (InterruptedException e) {}
            }
            runOnUiThread(()->{
                Log.d(TAG, "launch to overview activity");
                startActivity(new Intent(this, OverviewActivity.class));
                finish();
            });
        });
        initThread.setName("initThread");
        initThread.start();
    }
}