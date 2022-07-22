package com.msi.stockmanager.ui.main.analysis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityAnalysisBinding;
import com.msi.stockmanager.kline.KData;
import com.msi.stockmanager.kline.KLineView;
import com.msi.stockmanager.ui.main.StockFilterAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    public static final String TAG = AnalysisActivity.class.getSimpleName();
    public static final String EXTRA_STOCK_ID = "EXTRA_STOCK_ID";

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
    private IStockApi.HistoryCallback historyCallback = new IStockApi.HistoryCallback() {
        @Override
        public void onResult(List<StockHistory> data) {
            runOnUiThread(()->{
                setKlineData(data);
                binding.dataContainer.setVisibility(View.VISIBLE);
                binding.loading.setVisibility(View.INVISIBLE);
                binding.noData.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void onException(Exception e) {
            runOnUiThread(()-> {
                binding.dataContainer.setVisibility(View.INVISIBLE);
                binding.loading.setVisibility(View.INVISIBLE);
                binding.noData.setVisibility(View.VISIBLE);
            });
        }
    };

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
        if(targetStockInfo != null && !targetStockId.equals(targetStockInfo.getStockId()) &&
                binding != null && mMenu != null) {
            targetStockId = targetStockInfo.getStockId();
            getSupportActionBar().setTitle(getString(R.string.title_activity_analysis) +
                    "：" + targetStockInfo.getStockNameWithId());
            requestAsync();
        }
    }

    private void requestAsync(){
        if(targetStockInfo == null || targetStockId == null || targetStockId.isEmpty()) return;
        binding.dataContainer.setVisibility(View.INVISIBLE);
        binding.loading.setVisibility(View.VISIBLE);
        binding.noData.setVisibility(View.INVISIBLE);
        ApiUtil.stockApi.getHistoryStockData(targetStockId,
                "1d", "1y", historyCallback);
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
            reload();
        } else {
            binding.dataContainer.setVisibility(View.INVISIBLE);
            binding.loading.setVisibility(View.INVISIBLE);
            binding.noData.setVisibility(View.VISIBLE);
        }
        mSearchView.onActionViewCollapsed();
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
        } else {
            super.onBackPressed();
        }
    }

}