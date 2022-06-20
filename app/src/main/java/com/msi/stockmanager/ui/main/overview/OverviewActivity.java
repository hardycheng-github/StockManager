package com.msi.stockmanager.ui.main.overview;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.msi.stockmanager.DatabaseDemoActivity;
import com.msi.stockmanager.HttpDemoActivity;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.pager.PagerActivity;
import com.msi.stockmanager.R;
import com.msi.stockmanager.ui.main.setting.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private static final int MSG_JUMP_PENDING = 0x1001;
    private static final int DELAY_JUMP_PENDING = 300;
    private boolean isTouchEnable = false;
    private int color_balance;
    private int color_invest;
//    private ITransApi.TransUpdateListener transUpdateListener = new ITransApi.TransUpdateListener() {
//        @Override
//        public void onAdd(Transaction trans) {
//            onUiDataChanged();
//        }
//
//        @Override
//        public void onEdit(long transId, Transaction trans) {
//            onUiDataChanged();
//        }
//
//        @Override
//        public void onRemove(long transId) {
//            onUiDataChanged();
//        }
//    };
//    private class UiDataChangedTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... voids) {
//            //TODO calculate fixed
//            accountCalc = cashTotal = 0;
//            Map<String, Integer> holdingStockAmount = ApiUtil.transApi.getHoldingStockAmount();
//            for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
//                switch (trans.trans_type){
//                    case TransType.TRANS_TYPE_CASH_IN: case TransType.TRANS_TYPE_CASH_OUT:
//                        cashTotal += trans.cash_amount;
//                        break;
//                }
//                accountCalc += trans.cash_amount;
//            }
//            Lock lock = new ReentrantLock();
//            lockCount = holdingStockAmount.size();
//            for(Map.Entry<String, Integer> entry: holdingStockAmount.entrySet()){
//                String stockId = entry.getKey();
//                int stockAmount = entry.getValue();
//                ApiUtil.stockApi.getRegularStockPrice(stockId, info -> {
//                    if(info != null){
//                        accountCalc += stockAmount * info.getLastPrice();
//                    }
//                    if(--lockCount <= 0){
//                        synchronized (lock) {
//                            lock.notifyAll();
//                        }
//                    }
//                });
//            }
//            if(lockCount > 0) {
//                synchronized (lock) {
//                    try {
//                        lock.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result){
//            int profit = (int)(accountCalc - cashTotal);
//            double percent = cashTotal > 0 ? (profit * 1.0 / cashTotal) : 0;
//            binding.accountCalc.setText(FormatUtil.currency(accountCalc));
//            if(profit < 0){
//                binding.accountProfitCalc.setTextColor(getColor(R.color.stock_lose));
//                binding.accountProfitCalc.setText(String.format("%s (%s) ▼", FormatUtil.number(profit), FormatUtil.percent(percent)));
//            } else if(profit > 0){
//                binding.accountProfitCalc.setTextColor(getColor(R.color.stock_earn));
//                binding.accountProfitCalc.setText(String.format("%s (%s) ▲", FormatUtil.number(profit), FormatUtil.percent(percent)));
//            } else {
//                binding.accountProfitCalc.setTextColor(getColor(R.color.black));
//                binding.accountProfitCalc.setText(String.format("%s (%s)", FormatUtil.number(profit), FormatUtil.percent(percent)));
//            }
//        }
//    }
//    private UiDataChangedTask onUiDataChangedTask;

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
    private ViewTreeObserver.OnGlobalLayoutListener onPieChartLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            View view = binding.pieChart;
            int width = view.getWidth();
            if(width != 0 && width != view.getHeight()){
                view.setLayoutParams(new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT, width));
            }
        }
    };
    private AccountUtil.AccountUpdateListener accountUpdateListener = new AccountUtil.AccountUpdateListener() {
        @Override
        public void onUpdate(AccountUtil.AccountValue account) {
            binding.accountCalc.setText(FormatUtil.currency(account.accountCalcTotal));
            if(account.stockProfitTotal < 0){
                binding.accountProfitCalc.setTextColor(getColor(R.color.stock_lose));
                binding.accountProfitCalc.setText(String.format("%s (%s) ▼",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            } else if(account.stockProfitTotal > 0){
                binding.accountProfitCalc.setTextColor(getColor(R.color.stock_earn));
                binding.accountProfitCalc.setText(String.format("%s (%s) ▲",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            } else {
                binding.accountProfitCalc.setTextColor(getColor(R.color.black));
                binding.accountProfitCalc.setText(String.format("%s (%s)",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            }

            List<PieEntry> pieEntryList = new ArrayList<>();
            pieEntryList.add(new PieEntry(account.cashBalance, getString(R.string.account_balance)));
            pieEntryList.add(new PieEntry(account.stockCostTotal, getString(R.string.invested_cost)));
            PieDataSet pieDataSet = new PieDataSet(pieEntryList, getString(R.string.account_calc));
            pieDataSet.setColors(new int[]{color_balance, color_invest});
            pieDataSet.setSliceSpace(5f);

            PieData pieData = new PieData(pieDataSet);
            pieData.setDrawValues(false);
            binding.pieChart.setData(pieData);
            binding.pieChart.invalidate();
            binding.pieChart.animateY(1000, Easing.EaseInOutQuart);
        }
    };

//    private int accountCalc = 0;
//    private int cashTotal = 0;
//    private int lockCount = 0;

//    private void onUiDataChanged(){
//        if(onUiDataChangedTask != null && onUiDataChangedTask.getStatus() == AsyncTask.Status.RUNNING){
//            Log.w(TAG, "onUiDataChangedTask already running, interrupt.");
//            return;
//        }
//        onUiDataChangedTask = new UiDataChangedTask();
//        onUiDataChangedTask.execute();
//    }

    public OverviewActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                color_balance = getColor(R.color.sub_l);
                color_invest = getColor(R.color.sub_s);
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
                AccountUtil.init(this);

                binding.pieChart.setDrawEntryLabels(false);
                binding.pieChart.getLegend().setEnabled(false);
                binding.pieChart.getDescription().setEnabled(false);

                binding.pieChart.setNoDataText(null);
                binding.pieChart.setTransparentCircleRadius(0f);
                binding.pieChart.setHoleRadius(80f);
                binding.pieChart.setHoleColor(Color.TRANSPARENT);
//                binding.pieChart.setNoDataTextColor(Color.BLACK);
//                Paint paint = binding.pieChart.getPaint(Chart.PAINT_INFO);
//                int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, getResources().getDisplayMetrics());
//                paint.setTextSize(size);
//

            } else if(event.equals(Lifecycle.Event.ON_START)){
                isTouchEnable = true;
//                binding.fabOverviewAdd.hideMenuButton(false);
                binding.fabOverviewAdd.showMenuButton(true);
                int width = binding.pieChart.getWidth();
                int paddingHorizontal;
                binding.pieChart.getViewTreeObserver().addOnGlobalLayoutListener(onPieChartLayoutListener);
                AccountUtil.addListener(accountUpdateListener);
//                ApiUtil.transApi.addTransUpdateListener(transUpdateListener);
//                onUiDataChanged();
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOverviewAdd.close(false);
                binding.fabOverviewAdd.hideMenuButton(false);
                binding.pieChart.getViewTreeObserver().removeOnGlobalLayoutListener(onPieChartLayoutListener);
                AccountUtil.removeListener(accountUpdateListener);
//                ApiUtil.transApi.removeTransUpdateListener(transUpdateListener);
            } else if(event.equals(Lifecycle.Event.ON_DESTROY)){
                AccountUtil.close();
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