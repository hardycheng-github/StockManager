package com.msi.stockmanager.ui.main.overview;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.msi.stockmanager.DatabaseDemoActivity;
import com.msi.stockmanager.HttpDemoActivity;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.pager.PagerActivity;
import com.msi.stockmanager.R;
import com.msi.stockmanager.ui.main.setting.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private static final int MSG_JUMP_PENDING = 0x1001;
    private static final int DELAY_JUMP_PENDING = 350;
    private boolean isTouchEnable = false;
    private int color_balance;
    private int color_invest;

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
                binding.colorProfit.setBackgroundColor(ColorUtil.getProfitLose());
//                binding.stockValueCalc.setTextColor(ColorUtil.getProfitLose());
                binding.stockValueCalc.setText(FormatUtil.currency(account.stockCalcTotal));
                binding.profitValue.setTextColor(ColorUtil.getProfitLose());
                binding.profitValue.setText("-"+FormatUtil.number(Math.abs(account.stockProfitTotal)));
//                binding.profitValue.setText(String.format("-%s (-%s)"
//                        ,FormatUtil.number(Math.abs(account.stockProfitTotal))
//                        ,FormatUtil.percent(Math.abs(account.stockProfitRate))));
//                binding.profitValue.setText(FormatUtil.currency(account.stockCalcTotal) + " (-"+FormatUtil.currency(Math.abs(account.stockProfitTotal))+")");
                binding.accountProfitCalc.setTextColor(ColorUtil.getProfitLose());
                binding.accountProfitCalc.setText(String.format("%s (%s) ▼",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            } else if(account.stockProfitTotal > 0){
                binding.colorProfit.setBackgroundColor(ColorUtil.getProfitEarn());
//                binding.stockValueCalc.setTextColor(ColorUtil.getProfitEarn());
                binding.stockValueCalc.setText(FormatUtil.currency(account.stockCalcTotal));
                binding.profitValue.setTextColor(ColorUtil.getProfitEarn());
                binding.profitValue.setText("+"+FormatUtil.number(Math.abs(account.stockProfitTotal)));
//                binding.profitValue.setText(String.format("+%s (+%s)"
//                        ,FormatUtil.number(Math.abs(account.stockProfitTotal))
//                        ,FormatUtil.percent(Math.abs(account.stockProfitRate))));
//                binding.profitValue.setText(FormatUtil.currency(account.stockCalcTotal) + " (+"+FormatUtil.currency(Math.abs(account.stockProfitTotal))+")");
                binding.accountProfitCalc.setTextColor(ColorUtil.getProfitEarn());
                binding.accountProfitCalc.setText(String.format("%s (%s) ▲",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            } else {
                binding.colorProfit.setBackgroundColor(ColorUtil.getProfitNone());
//                binding.stockValueCalc.setTextColor(ColorUtil.getProfitNone());
                binding.stockValueCalc.setText(FormatUtil.currency(account.stockCalcTotal));
                binding.profitValue.setTextColor(ColorUtil.getProfitNone());
                binding.profitValue.setText(FormatUtil.number(Math.abs(account.stockProfitTotal)));
//                binding.profitValue.setText(String.format("%s (%s)"
//                        ,FormatUtil.number(Math.abs(account.stockProfitTotal))
//                        ,FormatUtil.percent(Math.abs(account.stockProfitRate))));
//                binding.profitValue.setText(FormatUtil.currency(account.stockCalcTotal) + " (+" + FormatUtil.currency(account.stockProfitTotal) + ")");
                binding.accountProfitCalc.setTextColor(ColorUtil.getProfitNone());
                binding.accountProfitCalc.setText(String.format("%s (%s)",
                        FormatUtil.number(account.stockProfitTotal), FormatUtil.percent(account.accountProfitRate)));
            }

            List<PieEntry> pieEntryList = new ArrayList<>();
            int[] colors = new int[]{color_balance, color_invest, ColorUtil.getProfitNone()};
            int[] values = new int[]{0, 0, 0};
            if(account.accountTotal == 0){
                values[0] = 100;
            } else if(account.stockProfitTotal > 0){
                values[0] = account.cashBalance;
                values[1] = account.stockCostTotal;
                values[2] = account.stockProfitTotal;
                colors[2] = ColorUtil.getProfitEarn();
            } else if(account.stockProfitTotal < 0){
                values[0] = account.cashBalance;
                values[1] = account.stockCalcTotal;
                values[2] = -account.stockProfitTotal;
                colors[2] = ColorUtil.getProfitLose();
            } else {
                values[0] = account.cashBalance;
                values[1] = account.stockCostTotal;
            }
            int valueTotal = values[0] + values[1] + values[2];
            int valueMin = valueTotal > 100 ? valueTotal / 100 : 1;
            values[0] = Integer.max(values[0], valueMin);
            values[1] = Integer.max(values[1], valueMin);
            values[2] = Integer.max(values[2], valueMin);

            PieDataSet pieDataSet = new PieDataSet(pieEntryList, getString(R.string.account_calc));
            pieDataSet.addEntry(new PieEntry(values[0]));
            pieDataSet.addEntry(new PieEntry(values[1]));
            pieDataSet.addEntry(new PieEntry(values[2]));
            pieDataSet.setColors(colors);
            pieDataSet.setSliceSpace(5f);

            PieData pieData = new PieData(pieDataSet);
            pieData.setDrawValues(false);
            binding.pieChart.setData(pieData);
            binding.pieChart.invalidate();
            binding.pieChart.animateY(1000, Easing.EaseInOutQuart);

            binding.accountBalance.setText(FormatUtil.currency(account.cashBalance));
            binding.investValue.setText(FormatUtil.currency(account.stockCostTotal));
        }
    };

    public OverviewActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
//                Profile.load(this);
//                ApiUtil.init(this);
//                ColorUtil.init(this);
//                AccountUtil.init(this);

                binding = ActivityOverviewBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.overviewToolbar);

                color_balance = getColor(R.color.main_l);
                color_invest = getColor(R.color.sub_m);
                binding.colorBalance.setBackgroundColor(color_balance);
                binding.colorInvest.setBackgroundColor(color_invest);
//                binding.profitValueTitle.setText(getString(R.string.stock_value_calc) + " (" + getString(R.string.profit_calc)+")");

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
                binding.fabOverviewAdd.setOnMenuButtonClickListener(view -> {
                    if(!binding.fabOverviewAdd.isOpened()) {
                        binding.fabOverviewContainer.setClickable(true);
                        binding.fabOverviewContainer.setBackgroundColor(getColor(R.color.transparent_bg_1));
                        binding.fabOverviewAdd.open(true);
                    } else {
                        binding.fabOverviewAdd.close(true);
                    }
                });
                binding.fabOverviewAdd.setOnMenuToggleListener(opened -> {
                    if(!opened){
                        binding.fabOverviewContainer.setClickable(false);
                        binding.fabOverviewContainer.setBackgroundColor(getColor(R.color.transparent));
                    }
                });
                binding.fabOverviewContainer.setOnClickListener(v->{
                    binding.fabOverviewAdd.close(true);
                });
                binding.fabOverviewContainer.setClickable(false);
                binding.fabOverviewContainer.setBackgroundColor(getColor(R.color.transparent));

                binding.pieChart.setDrawEntryLabels(false);
                binding.pieChart.getLegend().setEnabled(false);
                binding.pieChart.getDescription().setEnabled(false);

                binding.pieChart.setTouchEnabled(false);
                binding.pieChart.setRotationEnabled(false);
                binding.pieChart.setNoDataText(null);
                binding.pieChart.setTransparentCircleRadius(0f);
                binding.pieChart.setHoleRadius(85f);
                binding.pieChart.setHoleColor(Color.TRANSPARENT);

            } else if(event.equals(Lifecycle.Event.ON_START)){
                isTouchEnable = true;
                binding.fabOverviewAdd.showMenuButton(true);
                binding.pieChart.getViewTreeObserver().addOnGlobalLayoutListener(onPieChartLayoutListener);
                AccountUtil.addListener(accountUpdateListener);
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOverviewAdd.close(false);
                binding.fabOverviewAdd.hideMenuButton(false);
                binding.pieChart.getViewTreeObserver().removeOnGlobalLayoutListener(onPieChartLayoutListener);
                AccountUtil.removeListener(accountUpdateListener);
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