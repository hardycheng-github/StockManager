package com.msi.stockmanager.ui.main.analysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.analytics.ITaApi;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityAnalysisBinding;
import com.msi.stockmanager.databinding.ActivityAnalysisItemBinding;
import com.msi.stockmanager.databinding.ViewTaItemBinding;
import com.msi.stockmanager.kline.KData;
import com.msi.stockmanager.kline.KLineView;
import com.msi.stockmanager.ui.main.StockFilterAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.Disposable;

public class AnalysisActivity extends AppCompatActivity {
    public static final String TAG = AnalysisActivity.class.getSimpleName();
    public static final String EXTRA_STOCK_ID = "EXTRA_STOCK_ID";

    public static final String URL_GOODINFO = "https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID={STOCK_ID}";
    public static final String URL_WANTGOO = "https://www.wantgoo.com/stock/{STOCK_ID}";
    public static final String URL_YAHOO = "https://tw.stock.yahoo.com/quote/{STOCK_ID}";
    public static final String URL_ANUE = "https://invest.cnyes.com/twstock/TWS/{STOCK_ID}";
    public static final String URL_TWSE = "https://www.twse.com.tw/pdf/ch/{STOCK_ID}_ch.pdf";
    public static final String URL_CMONEY_FORUM = "https://www.cmoney.tw/forum/stock/{STOCK_ID}";

    private ActivityAnalysisBinding binding;
    private Menu mMenu;
    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchSrcTextView;
    private ImageView mSearchCloseBtn;
    private boolean isSearchExpand = false;
    private int mColumnCount = 1;
    private boolean isKlineInit = false;
    private String targetStockId = "";
    private StockInfo targetStockInfo = null;
    private AnalysisAdapter mAdatper;

    private View.OnLayoutChangeListener mSearchLayoutChangListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom){
            if(isSearchExpand != !mSearchView.isIconified()){
                isSearchExpand = !mSearchView.isIconified();
                if(isSearchExpand){
                    mSearchView.setQuery(targetStockId, false);
                }
            }
        }
    };

    private KData toKData(StockHistory item){
        return new KData(
                item.date_timestamp,
                item.price_open,
                item.price_close,
                item.price_high,
                item.price_low,
                item.price_volume
                );
    }

    private void setKlineData(List<StockHistory> list){
        List<KData> kDataList = new ArrayList<>();

        for(StockHistory item: list){
            kDataList.add(toKData(item));
        }

        if(!isKlineInit){
            isKlineInit = true;
            binding.kline.initKDataList(kDataList);
        } else {
            binding.kline.resetDataList(kDataList);
        }
        binding.kline.setDeputyPicShow(true);
        binding.kline.setMainImgType(KLineView.MAIN_IMG_MA);
        binding.kline.setDeputyImgType(KLineView.DEPUTY_IMG_MACD);
        binding.kline.setColorChange(Profile.profit_color_reverse);
    }

    private void reload(){
        if(binding != null && mMenu != null) {
            if (targetStockInfo != null) {
                if(!targetStockId.equals(targetStockInfo.getStockId())) {
                    getSupportActionBar().setTitle(targetStockInfo.getStockNameWithId());
                    binding.dataContainer.setVisibility(View.INVISIBLE);
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.list.setVisibility(View.INVISIBLE);
                    binding.noData.setVisibility(View.INVISIBLE);
                    targetStockId = targetStockInfo.getStockId();
                    binding.include.stockName.setText(targetStockInfo.getStockNameWithId());
                    binding.include.stockPrice.setTextColor(ColorUtil.getProfitNone());
                    binding.include.stockPrice.setText(R.string.syncing);
                    binding.include.stockProfit.setTextColor(ColorUtil.getProfitNone());
                    binding.include.stockProfit.setText(R.string.syncing);

                    initAnalytics(binding.include);

                    AccountUtil.StockValue value = AccountUtil.getAccount().stockValueMap.getOrDefault(targetStockId, null);
                    if(value != null){
                        onHistoryListUpdated(value.dataList);
                    } else {
                        requestAsync();
                    }
                    if(value != null && System.currentTimeMillis() - value.info.getLastUpdateTime() < 60*60*1000) {
                        onRegularPriceUpdated(value.info);
                    } else {
                        ApiUtil.stockApi.getRegularStockPrice(targetStockId, info -> {
                            onRegularPriceUpdated(info);
                        });
                    }
                }
            } else {
                targetStockId = "";
                getSupportActionBar().setTitle(R.string.title_activity_analysis);
                if(mAdatper.getItemCount() > 0){
                    binding.dataContainer.setVisibility(View.INVISIBLE);
                    binding.loading.setVisibility(View.INVISIBLE);
                    binding.list.setVisibility(View.VISIBLE);
                    binding.noData.setVisibility(View.INVISIBLE);
                } else {
                    binding.dataContainer.setVisibility(View.INVISIBLE);
                    binding.loading.setVisibility(View.INVISIBLE);
                    binding.list.setVisibility(View.INVISIBLE);
                    binding.noData.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void onHistoryListUpdated(List<StockHistory> dataList){
        runOnUiThread(()->{
            if(dataList != null){
                setKlineData(dataList);
                binding.dataContainer.setVisibility(View.VISIBLE);
                binding.loading.setVisibility(View.INVISIBLE);
                binding.list.setVisibility(View.INVISIBLE);
                binding.noData.setVisibility(View.INVISIBLE);
            } else {
                binding.dataContainer.setVisibility(View.INVISIBLE);
                binding.loading.setVisibility(View.INVISIBLE);
                binding.list.setVisibility(View.INVISIBLE);
                binding.noData.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onRegularPriceUpdated(StockInfo info){
        if(info == null) return;
        runOnUiThread(()->{
            if(info.getLastChange() > 0){
                binding.include.stockPrice.setTextColor(ColorUtil.getProfitEarn());
                binding.include.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                binding.include.stockProfit.setTextColor(ColorUtil.getProfitEarn());
                binding.include.stockProfit.setText(String.format("▲ %s (%s)"
                        , FormatUtil.number(Math.abs(info.getLastChange()))
                        , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
            } else if(info.getLastChange() < 0){
                binding.include.stockPrice.setTextColor(ColorUtil.getProfitLose());
                binding.include.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                binding.include.stockProfit.setTextColor(ColorUtil.getProfitLose());
                binding.include.stockProfit.setText(String.format("▼ %s (%s)"
                        , FormatUtil.number(Math.abs(info.getLastChange()))
                        , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
            } else {
                binding.include.stockPrice.setTextColor(ColorUtil.getProfitNone());
                binding.include.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                binding.include.stockProfit.setTextColor(ColorUtil.getProfitNone());
                binding.include.stockProfit.setText(String.format("%s (%s)"
                        , FormatUtil.number(Math.abs(info.getLastChange()))
                        , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
            }
            binding.include.open.setText(FormatUtil.number(info.getLastOpen()));
            binding.include.high.setText(FormatUtil.number(info.getLastHigh()));
            binding.include.low.setText(FormatUtil.number(info.getLastLow()));
            binding.include.volume.setText(FormatUtil.number(info.getLastVolume()));
        });
    }

    private void initAnalytics(ActivityAnalysisItemBinding binding){
        binding.analyticsBtnOn.setVisibility(View.GONE);
        binding.analyticsBtnOff.setVisibility(View.GONE);
        binding.analyticsContainer.setVisibility(View.VISIBLE);
        binding.taTotal.title.setText(R.string.total_score);
        binding.taShort.title.setText(R.string.short_score);
        binding.taLong.title.setText(R.string.long_score);
        binding.taTotal.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        binding.taTotal.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        binding.taLong.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        binding.taLong.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        binding.taShort.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        binding.taShort.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        AccountUtil.StockValue stockValue = AccountUtil.getAccount().stockValueMap.getOrDefault(targetStockId, null);
        if(stockValue != null && stockValue.taMap != null && !stockValue.taMap.isEmpty()){
            onTechnologyAnalysisUpdated(binding, stockValue.taMap);
        } else if(stockValue != null && stockValue.dataList != null && !stockValue.dataList.isEmpty()){
            Log.d(TAG, targetStockId + " initAnalytics: calc technology analysis");
            ApiUtil.taApi.getAllIndicatorLastScores(stockValue.dataList, new ITaApi.Callback() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

                @Override
                public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Map<String, ? extends Integer> stringMap) {
                    Map<String, Integer> taMap = new HashMap<String, Integer>();
                    taMap.putAll(stringMap);
                    onTechnologyAnalysisUpdated(binding, taMap);
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    Log.e(TAG, targetStockId + " initAnalytics err: fail to calc technology analysis");
                }
            });
        } else {
            Log.d(TAG, targetStockId + " initAnalytics: get history data");
            ApiUtil.stockApi.getHistoryStockData(targetStockId, "1d", "1mo", new IStockApi.HistoryCallback() {
                @Override
                public void onResult(List<StockHistory> data) {
                    Log.d(TAG, targetStockId + " initAnalytics: calc technology analysis");
                    ApiUtil.taApi.getAllIndicatorLastScores(stockValue.dataList, new ITaApi.Callback() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

                        @Override
                        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull Map<String, ? extends Integer> stringMap) {
                            Map<String, Integer> taMap = new HashMap<String, Integer>();
                            taMap.putAll(stringMap);
                            onTechnologyAnalysisUpdated(binding, taMap);
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            Log.e(TAG, targetStockId + " initAnalytics err: fail to calc technology analysis");
                        }
                    });
                }

                @Override
                public void onException(Exception e) {
                    Log.e(TAG, targetStockId + " initAnalytics err: fail to get history data");
                }
            });
        }
    }

    private void onTechnologyAnalysisUpdated(ActivityAnalysisItemBinding binding, Map<String, Integer> taMap){
        if(taMap == null) return;
        Log.d(TAG, "onTechnologyAnalysisUpdated: size " + taMap.size());
        int rsi = taMap.getOrDefault(ITaApi.KEY_RSI, ITaApi.SCORE_ERR);
        int ppo = taMap.getOrDefault(ITaApi.KEY_PPO, ITaApi.SCORE_ERR);
        int wr = taMap.getOrDefault(ITaApi.KEY_WILLIAMS_R, ITaApi.SCORE_ERR);
        int totalScore = taMap.getOrDefault(ITaApi.KEY_TOTAL, ITaApi.SCORE_ERR);
        int shortScore = (rsi + wr) / 2;
        int longScore = ppo;
        int totalLevel = Math.min(totalScore / 20, 4);
        int totalBg = ColorUtil.getProfitColor(totalLevel-2);
        runOnUiThread(()->{
            String[] scoreList = getResources().getStringArray(R.array.score_list);
            binding.analyticsResult.setText(scoreList[totalLevel]);
            binding.analyticsResult.setBackgroundColor(totalBg);
            updateTaView(binding.taShort, shortScore);
            updateTaView(binding.taLong, longScore);
            updateTaView(binding.taTotal, totalScore);
        });
    }

    private void updateTaView(ViewTaItemBinding binding, int score){
//        int color = ColorUtil.getProfitScore(score);
        double alpha = (Math.abs(score-50) / 50.0) * 0.6;
        int bgColor = ColorUtil.getColorWithAlpha(ColorUtil.getProfitColorSoft(score-50), alpha);
//        binding.score.setTextColor(color);
        binding.score.setText(score+"");
        binding.indicator.setProgress(score);
        binding.indicator.setBackgroundTintList(ColorStateList.valueOf(bgColor));
    }

    private void requestAsync(){
        if(targetStockInfo == null || targetStockId == null || targetStockId.isEmpty()) return;
        ApiUtil.stockApi.getHistoryStockData(targetStockId,
                "1d", "1y", new IStockApi.HistoryCallback() {
                    @Override
                    public void onResult(List<StockHistory> data) {
                        onHistoryListUpdated(data);
                    }

                    @Override
                    public void onException(Exception e) {
                        onHistoryListUpdated(null);
                    }
                });
    }

    private void toUrl(String link){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        startActivity(i);
    }

    public AnalysisActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                binding = ActivityAnalysisBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                View view = binding.getRoot();
                Context context = view.getContext();
                RecyclerView recyclerView = view.findViewById(R.id.list);
                binding.include.img.setImageDrawable(getDrawable(R.drawable.ic_baseline_read_more_24));
                binding.include.cardView.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(context, v, Gravity.RIGHT);

                    // Inflating popup menu from popup_menu.xml file
                    popupMenu.getMenuInflater().inflate(R.menu.stock_more, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch(item.getItemId())
                            {
                                case R.id.goodinfo:
                                    toUrl(URL_GOODINFO.replace("{STOCK_ID}", targetStockId));
                                    return true;
                                case R.id.wantgoo:
                                    toUrl(URL_WANTGOO.replace("{STOCK_ID}", targetStockId));
                                    return true;
                                case R.id.yahoo:
                                    toUrl(URL_YAHOO.replace("{STOCK_ID}", targetStockId));
                                    return true;
                                case R.id.anue:
                                    toUrl(URL_ANUE.replace("{STOCK_ID}", targetStockId));
                                    return true;
                                case R.id.cmoney_forum:
                                    toUrl(URL_CMONEY_FORUM.replace("{STOCK_ID}", targetStockId));
                                    return true;
                                case R.id.twse:
                                    toUrl(URL_TWSE.replace("{STOCK_ID}", targetStockId));
                                    return true;
                            }
                            return onMenuItemClick(item);
                        }
                    });
                    // Showing the popup menu
                    popupMenu.show();
                });
                mAdatper = new AnalysisAdapter() {
                    @Override
                    public void onStockSelected(StockInfo target) {
                        onSearchApply(target.getStockId());
                    }
                };
//                binding.list.setLayoutManager(new LinearLayoutManager(this));
                binding.list.setAdapter(mAdatper);

                try {
                    String sVal = getIntent().getStringExtra(EXTRA_STOCK_ID);
                    if(sVal != null) onSearchApply(sVal);
                } catch (Exception e){}

                // Set the adapter
                if (recyclerView != null) {
                    if (mColumnCount <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                    }
                }
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                mSearchView.removeOnLayoutChangeListener(mSearchLayoutChangListener);
            }
        });
    }

    private void onSearchApply(@NonNull String keyword){
        StockInfo info = StockUtilKt.getStockInfoOrNull(keyword);
        if(info != null){
            targetStockInfo = info;
        } else {
            targetStockInfo = null;
        }
        mSearchView.onActionViewCollapsed();
        reload();
    }

    public int getListPreferredItemHeightInPixels() {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, value, true);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_analysis, menu);
        mSearchItem = mMenu.findItem(R.id.app_bar_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchSrcTextView = mSearchView.findViewById(R.id.search_src_text);
        mSearchCloseBtn = mSearchView.findViewById(R.id.search_close_btn);
        mSearchCloseBtn.setOnClickListener(v -> {
            mSearchView.onActionViewCollapsed();
        });
        List<String> stockNameList = new ArrayList<>();

        for(StockInfo info: StockUtilKt.getStockUtil().getStockList()){
            stockNameList.add(info.getStockNameWithId());
        }
        StockFilterAdapter adapter = new StockFilterAdapter(this, stockNameList) {
            @Override
            public void onItemSelected(int position, String target) {
                onSearchApply(target);
            }
        };
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                int count = adapter.getCount();
                int maxHeight = (int)(getResources().getDisplayMetrics().heightPixels * .33);
                int itemHeight = getListPreferredItemHeightInPixels();
                int listHeight = maxHeight;
                if(count > 0){
                    listHeight = Integer.min(maxHeight, itemHeight * count);
                }
                mSearchSrcTextView.setDropDownHeight(listHeight);
            }
        });
        mSearchSrcTextView.setAdapter(adapter);
        mSearchSrcTextView.setDropDownBackgroundResource(R.color.white);
//        mSearchSrcTextView.setDropDownHeight((int) (getResources().getDisplayMetrics().heightPixels*.33));
        mSearchSrcTextView.setThreshold(1);
        mSearchView.addOnLayoutChangeListener(mSearchLayoutChangListener);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.onActionViewExpanded();
            }
        });
        mSearchView.setQueryHint(getString(R.string.hint_stock_search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.onActionViewCollapsed();
                onSearchApply(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        reload();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        if(!mSearchView.isIconified()){
            mSearchView.onActionViewCollapsed();
            mSearchItem.collapseActionView();
            MenuItemCompat.collapseActionView(mSearchItem);
        } else if (targetStockInfo != null) {
            onSearchApply("");
        } else {
            super.onBackPressed();
        }
    }

}