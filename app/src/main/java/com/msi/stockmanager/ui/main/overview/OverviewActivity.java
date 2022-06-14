package com.msi.stockmanager.ui.main.overview;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.msi.stockmanager.DatabaseDemoActivity;
import com.msi.stockmanager.HttpDemoActivity;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.pager.PagerActivity;
import com.msi.stockmanager.R;
import com.msi.stockmanager.ui.main.setting.SettingsActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private static final int MSG_JUMP_PENDING = 0x1001;
    private static final int DELAY_JUMP_PENDING = 300;
    private boolean isTouchEnable = false;
    private ITransApi.TransUpdateListener transUpdateListener = new ITransApi.TransUpdateListener() {
        @Override
        public void onAdd(Transaction trans) {
            onUiDataChanged();
        }

        @Override
        public void onEdit(long transId, Transaction trans) {
            onUiDataChanged();
        }

        @Override
        public void onRemove(long transId) {
            onUiDataChanged();
        }
    };
    private class UiDataChangedTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //TODO calculate fixed
            assetsCalc = assetsTotal = 0;
            Map<String, Integer> holdingStockRemainingAmount = new HashMap<>();
            for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
                if(trans.stock_price * trans.stock_amount <= 0){
                    assetsCalc += trans.cash_amount;
                }
                assetsTotal += trans.cash_amount;
                if(!trans.stock_id.isEmpty()) {
                    int amount = holdingStockRemainingAmount.getOrDefault(trans.stock_id, 0);
                    holdingStockRemainingAmount.put(trans.stock_id, amount + trans.stock_amount);
                }
            }
            Lock lock = new ReentrantLock();
            lockCount = holdingStockRemainingAmount.size();
            for(Map.Entry<String, Integer> entry: holdingStockRemainingAmount.entrySet()){
                String stockId = entry.getKey();
                int stockAmount = entry.getValue();
                ApiUtil.stockApi.getRegularStockPrice(stockId, info -> {
                    if(info != null){
                        assetsCalc += stockAmount * info.getLastPrice();
                    }
                    if(--lockCount <= 0){
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
            }
            synchronized (lock){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            int profit = (int)(assetsCalc - assetsTotal);
            double percent = assetsTotal > 0 ? (profit * 1.0 / assetsTotal) : 0;
            binding.assetsCalc.setText(FormatUtil.number(assetsCalc));
            if(profit < 0){
                binding.assetsProfitCalc.setTextColor(getColor(R.color.stock_lose));
                binding.assetsProfitCalc.setText(String.format("%s (%s) ▼", FormatUtil.number(profit), FormatUtil.percent(percent)));
            } else if(profit > 0){
                binding.assetsProfitCalc.setTextColor(getColor(R.color.stock_earn));
                binding.assetsProfitCalc.setText(String.format("%s (%s) ▲", FormatUtil.number(profit), FormatUtil.percent(percent)));
            } else {
                binding.assetsProfitCalc.setTextColor(getColor(R.color.black));
                binding.assetsProfitCalc.setText("");
            }
        }
    }
    private UiDataChangedTask onUiDataChangedTask;

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

    private long assetsCalc = 0;
    private long assetsTotal = 0;
    private int lockCount = 0;

    private void onUiDataChanged(){
        if(onUiDataChangedTask != null && onUiDataChangedTask.getStatus() == AsyncTask.Status.RUNNING){
            Log.w(TAG, "onUiDataChangedTask already running, interrupt.");
            return;
        }
        onUiDataChangedTask = new UiDataChangedTask();
        onUiDataChangedTask.execute();
    }

    public OverviewActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                ApiUtil.init(this);
                binding = ActivityOverviewBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.overviewToolbar);

                binding.btnHttp.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, HttpDemoActivity.class)));
                binding.btnSqlTest.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, DatabaseDemoActivity.class)));
                binding.btnPager.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, PagerActivity.class)));
                binding.overviewCard.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, PagerActivity.class)));
                binding.fabOverviewAddCash.setOnClickListener(v -> {
                    Intent intent = new Intent(OverviewActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_CASH_IN));
                    startActivity(intent);
                });
                binding.fabOverviewAddStock.setOnClickListener(v -> {
                    Intent intent = new Intent(OverviewActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_BUY));
                    startActivity(intent);
                });
                binding.fabOverviewAddDividend.setOnClickListener(v -> {
                    Intent intent = new Intent(OverviewActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_DIVIDEND));
                    startActivity(intent);
                });
                binding.fabOverviewAddReduction.setOnClickListener(v -> {
                    Intent intent = new Intent(OverviewActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_REDUCTION));
                    startActivity(intent);
                });
            } else if(event.equals(Lifecycle.Event.ON_START)){
                isTouchEnable = true;
//                binding.fabOverviewAdd.hideMenuButton(false);
                binding.fabOverviewAdd.showMenuButton(true);
                ApiUtil.transApi.addTransUpdateListener(transUpdateListener);
                onUiDataChanged();
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOverviewAdd.close(false);
                binding.fabOverviewAdd.hideMenuButton(false);
                ApiUtil.transApi.removeTransUpdateListener(transUpdateListener);
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