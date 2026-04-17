package com.msi.stockmanager.ui.main.overview;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import com.msi.stockmanager.data.notify.INotifyRepository;
import com.msi.stockmanager.data.notify.NotifyEntity;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.databinding.LayoutNotifyDrawerBinding;
import com.msi.stockmanager.ui.main.analysis.AnalysisActivity;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.news.NewsActivity;
import com.msi.stockmanager.ui.main.notify.NotifyAdapter;
import com.msi.stockmanager.ui.main.pager.PagerActivity;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.notify.MaBreakthroughService;
import com.msi.stockmanager.ui.main.revenue.RevenueActivity;
import com.msi.stockmanager.ui.main.setting.SettingsActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
    private LayoutNotifyDrawerBinding notifyBinding;
    private NotifyAdapter notifyAdapter;
    private MenuItem notifyMenuItem;
    private View notifyBadgeView;
    private TextView badgeCountText;
    /** 快取未讀數，供 menu 晚於 onStart 建立時立即顯示，-1 表示尚未取得 */
    private int lastUnreadCount = -1;
    private CompositeDisposable disposables = new CompositeDisposable();
    private static final String PREF_NOTIFY_TEST_INSERTED = "notify_test_inserted_this_launch";
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

//                int color0 = ColorUtil.getColorWithAlpha(0xFFFF0000, 0);
//                int color1 = ColorUtil.getColorWithAlpha(0xFFFF0000, 0.2);
//                int color2 = ColorUtil.getColorWithAlpha(0xFFFF0000, 0.4);
//                int color3 = ColorUtil.getColorWithAlpha(0xFFFF0000, 1);

                binding = ActivityOverviewBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.overviewToolbar);

                color_balance = getColor(R.color.main_l);
                color_invest = getColor(R.color.sub_m);
                binding.colorBalance.setBackgroundColor(color_balance);
                binding.colorInvest.setBackgroundColor(color_invest);
//                binding.profitValueTitle.setText(getString(R.string.stock_value_calc) + " (" + getString(R.string.profit_calc)+")");

                binding.btnNews.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, NewsActivity.class)));
                binding.btnHttp.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, HttpDemoActivity.class)));
                binding.btnSqlTest.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, DatabaseDemoActivity.class)));
//                binding.btnPager.setOnClickListener(v->startActivity(new Intent(OverviewActivity.this, PagerActivity.class)));
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
                
                // 初始化通知相關 UI（在 ON_START 時確保 binding 已準備好）
                initNotifyUI();
                
                // 載入通知列表和未讀數
                loadNotifyList();
                updateUnreadCount();
                
                // 檢查平均線突破事件
                MaBreakthroughService.checkWatchingList(OverviewActivity.this);

                // 測試插入通知（每啟動一次）
                // insertTestNotifications(); // 已停用自動插入測試通知功能

            } else if(event.equals(Lifecycle.Event.ON_RESUME)){
                // 回到畫面時重新整理未讀數（離開再回來或從其他 Activity 返回）
                updateUnreadCount();
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOverviewAdd.close(false);
                binding.fabOverviewAdd.hideMenuButton(false);
                binding.pieChart.getViewTreeObserver().removeOnGlobalLayoutListener(onPieChartLayoutListener);
                AccountUtil.removeListener(accountUpdateListener);
                disposables.clear();
            } else if(event.equals(Lifecycle.Event.ON_DESTROY)){
                AccountUtil.close();
                disposables.dispose();
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
        
        // 取得通知 MenuItem 並設定 actionView（使用 actionLayout 時點擊不會自動觸發 onOptionsItemSelected，需手動轉發）
        notifyMenuItem = menu.findItem(R.id.menu_notify);
        if (notifyMenuItem != null && notifyMenuItem.getActionView() != null) {
            notifyBadgeView = notifyMenuItem.getActionView();
            badgeCountText = notifyBadgeView.findViewById(R.id.badge_count);
            notifyBadgeView.setOnClickListener(v -> {
                onOptionsItemSelected(notifyMenuItem);
                // 若 OnTouchListener 未收到事件，至少點擊時短暫顯示 ripple
                v.setPressed(true);
                v.postOnAnimation(() -> v.setPressed(false));
            });
            // actionLayout 的 view 常收不到 touch 的 pressed 狀態，手動設定以觸發 ripple
            notifyBadgeView.setOnTouchListener((v, ev) -> {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setPressed(false);
                        break;
                }
                return false; // 不消費事件，讓 click 仍可觸發
            });
            // Menu 可能晚於 onStart 建立，此時 badge 已就緒，立即套用快取並重新拉取未讀數
            if (lastUnreadCount >= 0) {
                applyUnreadCountToBadge(badgeCountText, lastUnreadCount);
            }
            updateUnreadCount();
        }
        
        return true;
    }
    
    /** 將未讀數寫入 badge TextView（可傳入 null 會略過）；顯示上限 99 */
    private void applyUnreadCountToBadge(TextView badge, int count) {
        if (badge == null) return;
        if (count > 0) {
            badge.setText(String.valueOf(Math.min(count, 99)));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "onOptionsItemSelected: " + id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_setting) {
            startActivity(new Intent(OverviewActivity.this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.menu_analysis) {
            Intent intent = new Intent(OverviewActivity.this, AnalysisActivity.class);
//          intent.putExtra("EXTRA_STOCK_ID", "2330");
            startActivity(intent);
            return true;
        }
        if (id == R.id.menu_news) {
            startActivity(new Intent(OverviewActivity.this, NewsActivity.class));
            return true;
        }
        if (id == R.id.menu_revenue) {
            startActivity(new Intent(OverviewActivity.this, RevenueActivity.class));
            return true;
        }
        if (id == R.id.menu_notify) {
            binding.drawer.openDrawer(GravityCompat.END);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerVisible(GravityCompat.END)) {
            binding.drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
    
    private void initNotifyUI() {
        if (notifyBinding != null) {
            return; // 已經初始化過
        }
        
        // NavigationView 的第一個子 View 應該是 include 的 layout_notify_drawer
        View drawerView = binding.navView.getChildAt(0);
        if (drawerView != null && drawerView.getId() == R.id.layout_notify_drawer) {
            notifyBinding = LayoutNotifyDrawerBinding.bind(drawerView);
        } else {
            // 如果無法取得，直接從 root 尋找
            View root = binding.getRoot();
            View notifyRoot = root.findViewById(R.id.layout_notify_drawer);
            if (notifyRoot != null) {
                notifyBinding = LayoutNotifyDrawerBinding.bind(notifyRoot);
            } else {
                Log.e(TAG, "Cannot find notify drawer layout");
                return;
            }
        }
        
        // 設定 RecyclerView
        notifyAdapter = new NotifyAdapter();
        notifyAdapter.setOnItemClickListener(item -> {
            // 點擊通知：標記為已讀
            if (!item.getRead()) {
                Disposable d = ApiUtil.notifyRepository.markRead(item.getId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        count -> {
                            updateUnreadCount();
                            loadNotifyList();
                        },
                        error -> Log.e(TAG, "markRead error", error)
                    );
                disposables.add(d);
            }
        });
        
        if (notifyBinding.recyclerNotify != null) {
            notifyBinding.recyclerNotify.setLayoutManager(new LinearLayoutManager(this));
            notifyBinding.recyclerNotify.setAdapter(notifyAdapter);
        }
        
        // 全部已讀按鈕
        if (notifyBinding.btnMarkAllRead != null) {
            notifyBinding.btnMarkAllRead.setOnClickListener(v -> {
                Disposable d = ApiUtil.notifyRepository.markAllRead()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        count -> {
                            updateUnreadCount();
                            loadNotifyList();
                        },
                        error -> Log.e(TAG, "markAllRead error", error)
                    );
                disposables.add(d);
            });
        }
        
        // 全部刪除按鈕
        if (notifyBinding.btnDeleteAll != null) {
            notifyBinding.btnDeleteAll.setOnClickListener(v -> {
                Disposable d = ApiUtil.notifyRepository.markAllDeleted()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        count -> {
                            updateUnreadCount();
                            loadNotifyList();
                        },
                        error -> Log.e(TAG, "markAllDeleted error", error)
                    );
                disposables.add(d);
            });
        }
        
        // 滑動刪除
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (!notifyAdapter.isValidPosition(position)) {
                    return;
                }
                NotifyEntity item = notifyAdapter.getItem(position);
                if (item == null) {
                    return;
                }
                
                Disposable d = ApiUtil.notifyRepository.markDeleted(item.getId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        count -> {
                            notifyAdapter.removeItem(position);
                            updateUnreadCount();
                            updateEmptyState();
                        },
                        error -> {
                            Log.e(TAG, "markDeleted error", error);
                            notifyAdapter.notifyItemChanged(position);
                        }
                    );
                disposables.add(d);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(notifyBinding.recyclerNotify);
    }
    
    private void loadNotifyList() {
        if (notifyAdapter == null) return;
        
        Disposable d = ApiUtil.notifyRepository.getList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                list -> {
                    notifyAdapter.setItems(list);
                    updateEmptyState();
                },
                error -> Log.e(TAG, "getList error", error)
            );
        disposables.add(d);
    }
    
    private void updateUnreadCount() {
        Disposable d = ApiUtil.notifyRepository.getUnreadCount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                count -> {
                    lastUnreadCount = count;
                    // 非同步回調時 badge 可能尚未建立，嘗試從 MenuItem 解析
                    TextView badge = badgeCountText;
                    if (badge == null && notifyMenuItem != null && notifyMenuItem.getActionView() != null) {
                        badge = notifyMenuItem.getActionView().findViewById(R.id.badge_count);
                    }
                    applyUnreadCountToBadge(badge, count);
                },
                error -> Log.e(TAG, "getUnreadCount error", error)
            );
        disposables.add(d);
    }
    
    private void updateEmptyState() {
        if (notifyBinding == null || notifyAdapter == null) return;
        
        if (notifyAdapter.getItemCount() == 0) {
            notifyBinding.textEmpty.setVisibility(View.VISIBLE);
            notifyBinding.recyclerNotify.setVisibility(View.GONE);
        } else {
            notifyBinding.textEmpty.setVisibility(View.GONE);
            notifyBinding.recyclerNotify.setVisibility(View.VISIBLE);
        }
    }
    
    private void insertTestNotifications() {
//        SharedPreferences prefs = getSharedPreferences("notify_prefs", MODE_PRIVATE);
//        boolean alreadyInserted = prefs.getBoolean(PREF_NOTIFY_TEST_INSERTED, false);
//
//        if (alreadyInserted) {
//            return;
//        }
//
//        // 標記為已插入
//        prefs.edit().putBoolean(PREF_NOTIFY_TEST_INSERTED, true).apply();
        
        // 隨機產生 3～5 筆通知
        final int count = 3 + (int)(Math.random() * 3); // 3-5
        
        String[] testTitles = {
            "測試通知 1",
            "測試通知 2",
            "測試通知 3",
            "測試通知 4",
            "測試通知 5"
        };
        
        String[] testBodies = {
            "這是一則測試通知內容",
            "通知功能測試中",
            "MACD 事件通知測試",
            "智慧分析通知測試",
            "觀察清單通知測試"
        };
        
        // 使用 AtomicInteger 來追蹤完成的數量
        final java.util.concurrent.atomic.AtomicInteger completedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (int i = 0; i < count; i++) {
            final int index = i; // 創建 final 變數供 lambda 使用
            final long createdAt = System.currentTimeMillis() - (i * 60000); // 每筆間隔 1 分鐘
            
            NotifyEntity item = new NotifyEntity(
                0,
                "TEST",
                testTitles[index % testTitles.length],
                testBodies[index % testBodies.length],
                createdAt,
                false,
                false,
                "",
                ""
            );
            
            Disposable d = ApiUtil.notifyRepository.add(item)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    id -> {
                        // 當所有通知都插入完成後更新 UI
                        if (completedCount.incrementAndGet() == count) {
                            updateUnreadCount();
                            loadNotifyList();
                        }
                    },
                    error -> {
                        Log.e(TAG, "add test notification error", error);
                        // 即使失敗也計數，避免永遠等待
                        if (completedCount.incrementAndGet() == count) {
                            updateUnreadCount();
                            loadNotifyList();
                        }
                    }
                );
            disposables.add(d);
        }
    }
}