package com.msi.stockmanager.ui.main.overview;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.msi.stockmanager.DemoActivity;
import com.msi.stockmanager.HttpDemoActivity;
import com.msi.stockmanager.InputCashInOut;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.pager.PagerActivity;
import com.msi.stockmanager.R;
import com.msi.stockmanager.ui.main.setting.SettingsActivity;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private static final int MSG_JUMP_PENDING = 0x1001;
    private static final int DELAY_JUMP_PENDING = 300;
    private boolean isTouchEnable = false;

    private ActivityOverviewBinding binding;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_JUMP_PENDING:
                    startActivity((Intent)msg.obj);
                    break;
            }
        }
    };

    public OverviewActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                binding = ActivityOverviewBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.overviewToolbar);

                binding.btnHttp.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, HttpDemoActivity.class)));
                binding.btnSqlTest.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, DemoActivity.class)));
                binding.btnPager.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, PagerActivity.class)));
                binding.fabOverviewAddCash.setOnClickListener(v -> {
                    startActivity(new Intent(OverviewActivity.this, InputCashInOut.class));
                });
                binding.fabOverviewAddStock.setOnClickListener(v -> {
                    startActivity(new Intent(OverviewActivity.this, FormActivity.class));
                });
                binding.fabOverviewAddOther.setOnClickListener(v -> {
                    //TODO add dividend or reduction transaction
                });
            } else if(event.equals(Lifecycle.Event.ON_START)){
                isTouchEnable = true;
//                binding.fabOverviewAdd.hideMenuButton(false);
                binding.fabOverviewAdd.showMenuButton(true);
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOverviewAdd.close(false);
                binding.fabOverviewAdd.hideMenuButton(false);
            }
        });
    }

    @Override
    public void startActivity(Intent intent){
        isTouchEnable = false;
        if(binding.fabOverviewAdd.isOpened()){
            binding.fabOverviewAdd.close(true);
            Message msg = mHandler.obtainMessage(MSG_JUMP_PENDING, intent);
            mHandler.sendMessageDelayed(msg, DELAY_JUMP_PENDING);
        } else {
            super.startActivity(intent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!isTouchEnable) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_setting) {
            startActivity(new Intent(OverviewActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}