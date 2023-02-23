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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

public class LaunchActivity extends AppCompatActivity {
    public static String TAG = LaunchActivity.class.getSimpleName();
    public static final int MIN_STAY_TIME = 3000;
    public static final int MAX_STAY_TIME = 9000;

    private ActivityLaunchBinding binding;
    private Thread initThread;
    private boolean isInit = false;
    private boolean isSkip = false;
    private boolean isAccountUpdated = false;
    private boolean isError = false;
    private ReentrantLock initLock = new ReentrantLock();
    private AccountUtil.AccountUpdateListener listener = accountValue -> {
        Log.d(TAG, "account updated!");
        isAccountUpdated = true;
        synchronized(initLock){
            initLock.notifyAll();
        }
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
        isInit = false;
        isSkip = false;
        isAccountUpdated = false;
        setContentView(binding.getRoot());
        binding.getRoot().setClickable(true);
        binding.getRoot().setOnClickListener(v->{
            if(isInit){
                isSkip = true;
                if(isInit) initThread.interrupt();
            }
        });
        binding.version.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        long t1 = System.currentTimeMillis();
        initThread = new Thread(()->{
            Log.d(TAG, "+++ init start +++");
            initLock.lock();
            Profile.load(this);
            ColorUtil.init(this);
            FormatUtil.init(this);
            ApiUtil.init(this);
            AccountUtil.init(this);
            AccountUtil.addListener(listener);
            long t2 = System.currentTimeMillis();
            try {
                if(!isAccountUpdated) {
                    synchronized (initLock) {
                        initLock.wait(MAX_STAY_TIME);
                    }
                }
                if (!isAccountUpdated) throw new Exception("timeout");
                Log.d(TAG, "AccountUtil init success: " + (System.currentTimeMillis() - t2));
            } catch (Exception e) {
                isError = true;
                Log.w(TAG, "AccountUtil init timeout: " + MAX_STAY_TIME);
            }
            AccountUtil.removeListener(listener);

            isInit = true;
            Log.d(TAG, "--- init finish ---");
            if(!isSkip) {
                long diff = System.currentTimeMillis() - t1;
                if (diff < MIN_STAY_TIME) {
                    try {
                        initThread.sleep(MIN_STAY_TIME - diff);
                    } catch (InterruptedException e) {}
                }
            }
            runOnUiThread(()->{
                if(isError){
                    Log.d(TAG, "timeout alert!");
                    // TODO: 2022/8/8
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.init_error_title)
                            .setMessage(R.string.init_error_message)
                            .setPositiveButton(R.string.retry, (dialogInterface, i) -> recreate())
                            .setNegativeButton(R.string.exit, (dialogInterface, i) -> finish())
                            .setOnDismissListener(dialogInterface -> finish())
                            .create()
                            .show();
                } else {
                    Log.d(TAG, "launch to overview activity");
                    startActivity(new Intent(this, OverviewActivity.class));
                    finish();
                }
            });
        });
        initThread.setName("initThread");
        initThread.start();
    }
}