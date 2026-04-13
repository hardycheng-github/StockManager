package com.msi.stockmanager.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleEventObserver;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.news.NewsPreloadService;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.databinding.ActivityLaunchBinding;
import com.msi.stockmanager.ui.main.overview.OverviewActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

public class LaunchActivity extends AppCompatActivity {
    public static String TAG = LaunchActivity.class.getSimpleName();
    public static final int MIN_STAY_TIME = 3000;
    public static final int MAX_STAY_TIME = 16000;
    private static final long STATUS_SWITCH_INTERVAL_MS = 1000L;
    private static final long STATUS_FADE_DURATION_MS = 750L;
    private static final long FINAL_STATUS_DELAY_MS = 1000L;

    private ActivityLaunchBinding binding;
    private Thread initThread;
    private boolean isInit = false;
    private boolean isSkip = false;
    private boolean isAccountUpdated = false;
    private boolean isNewsPreloaded = false;
    private boolean isError = false;
    private int statusIndex = 0;
    private final int[] initStatusTexts = {
            R.string.launch_init_preparing,
            R.string.launch_init_loading_account,
            R.string.launch_init_loading_news
    };
    // The status messages are intentionally cosmetic and not tied to real init progress.
    private final Runnable statusTicker = new Runnable() {
        @Override
        public void run() {
            if (binding == null || isDestroyed()) return;
            statusIndex = (statusIndex + 1) % initStatusTexts.length;
            showStatusWithFade(initStatusTexts[statusIndex], LaunchActivity.this::scheduleNextStatusTick);
        }
    };
    private final Object initSignal = new Object();
    private AccountUtil.AccountUpdateListener listener = accountValue -> {
        Log.d(TAG, "account updated!");
        isAccountUpdated = true;
        synchronized(initSignal){
            initSignal.notifyAll();
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
        isNewsPreloaded = false;
        isError = false;
        setContentView(binding.getRoot());
        binding.getRoot().setClickable(true);
        binding.getRoot().setOnClickListener(v->{
            if(isInit){
                isSkip = true;
                if(isInit) initThread.interrupt();
            }
        });
        binding.version.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
        String copyright = getString(R.string.copyright_msg).replace(
                "YYYY", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        binding.copyright.setText(copyright);
        startStatusRotation();
        long t1 = System.currentTimeMillis();
        initThread = new Thread(()->{
            Log.d(TAG, "+++ init start +++");
            Profile.load(this);
            ColorUtil.init(this);
            FormatUtil.init(this);
            ApiUtil.init(this);
            NewsPreloadService.preload(ApiUtil.newsApi, false, new com.msi.stockmanager.data.news.INewsApi.TaskCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "news preloaded!");
                    isNewsPreloaded = true;
                    synchronized (initSignal) {
                        initSignal.notifyAll();
                    }
                }

                @Override
                public void onException(Exception e) {
                    Log.e(TAG, "news preload failed: " + e.getMessage());
                    isNewsPreloaded = false;
                    isError = true;
                    synchronized (initSignal) {
                        initSignal.notifyAll();
                    }
                }
            });
            AccountUtil.init(this);
            AccountUtil.addListener(listener);
            long t2 = System.currentTimeMillis();
            try {
                long waitStart = System.currentTimeMillis();
                synchronized (initSignal) {
                    while (!isError && (!isAccountUpdated || !isNewsPreloaded)) {
                        long elapsed = System.currentTimeMillis() - waitStart;
                        long remain = MAX_STAY_TIME - elapsed;
                        if (remain <= 0) {
                            break;
                        }
                        initSignal.wait(remain);
                    }
                }
                if (!isAccountUpdated || !isNewsPreloaded) throw new Exception("timeout");
                Log.d(TAG, "Init success (account + news): " + (System.currentTimeMillis() - t2));
            } catch (Exception e) {
                isError = true;
                Log.w(TAG, "Init timeout: " + MAX_STAY_TIME + ", account=" + isAccountUpdated + ", news=" + isNewsPreloaded);
            }
            AccountUtil.removeListener(listener);

            isInit = true;
            Log.d(TAG, "--- init finish ---");
            if(!isSkip) {
                long diff = System.currentTimeMillis() - t1;
                if (diff < MIN_STAY_TIME) {
                    try {
                        Thread.sleep(MIN_STAY_TIME - diff);
                    } catch (InterruptedException e) {}
                }
            }
            runOnUiThread(()->{
                if(isError){
                    stopStatusRotation();
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
                    stopStatusRotation();
                    showStatusWithFade(R.string.launch_init_done, () -> binding.initStatus.postDelayed(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        Log.d(TAG, "launch to overview activity");
                        startActivity(new Intent(this, OverviewActivity.class));
                        finish();
                    }, FINAL_STATUS_DELAY_MS));
                }
            });
        });
        initThread.setName("initThread");
        initThread.start();
    }

    private void startStatusRotation() {
        statusIndex = 0;
        binding.initStatus.setText(initStatusTexts[statusIndex]);
        binding.initStatus.setAlpha(0f);
        binding.initStatus.animate()
                .alpha(1f)
                .setDuration(STATUS_FADE_DURATION_MS)
                .start();
        scheduleNextStatusTick();
    }

    private void scheduleNextStatusTick() {
        if (binding == null) return;
        binding.initStatus.removeCallbacks(statusTicker);
        long minTransitionMs = STATUS_FADE_DURATION_MS * 2L;
        long nextDelayMs = Math.max(STATUS_SWITCH_INTERVAL_MS, minTransitionMs);
        binding.initStatus.postDelayed(statusTicker, nextDelayMs);
    }

    private void stopStatusRotation() {
        if (binding == null) return;
        binding.initStatus.removeCallbacks(statusTicker);
        binding.initStatus.animate().cancel();
    }

    private void showStatusWithFade(int textResId, Runnable endAction) {
        binding.initStatus.animate()
                .alpha(0f)
                .setDuration(STATUS_FADE_DURATION_MS)
                .withEndAction(() -> {
                    if (binding == null || isDestroyed()) return;
                    binding.initStatus.setText(textResId);
                    binding.initStatus.setAlpha(0f);
                    binding.initStatus.animate()
                            .alpha(1f)
                            .setDuration(STATUS_FADE_DURATION_MS)
                            .withEndAction(endAction)
                            .start();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        stopStatusRotation();
        super.onDestroy();
    }

    public void recreate(){
        //fixed xiaomi device recreate fail
        finish();
        startActivity(new Intent(this, LaunchActivity.class));
    }
}