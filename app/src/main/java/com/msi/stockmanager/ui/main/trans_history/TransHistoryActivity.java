package com.msi.stockmanager.ui.main.trans_history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityTransHistoryBinding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class TransHistoryActivity extends AppCompatActivity {
    public static final String TAG = TransHistoryActivity.class.getSimpleName();
    public static final String EXTRA_KEYWORD = "EXTRA_KEYWORD";
    public static final String EXTRA_TARGET_TYPES = "EXTRA_TARGET_TYPES";
    public static final String EXTRA_START_TIME = "EXTRA_START_TIME";
    public static final String EXTRA_END_TIME = "EXTRA_END_TIME";

    private ActivityTransHistoryBinding binding;
    private Menu mMenu;
    private MenuItem mFilterItem, mSearchItem;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchSrcTextView;
    private ImageView mSearchCloseBtn;
    private boolean isSearchExpand = false;
    private TransHistoryAdapter mAdapter;
    private int mColumnCount = 1;

    private View.OnLayoutChangeListener mSearchLayoutChangListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom){
            if(isSearchExpand != !mSearchView.isIconified()){
                isSearchExpand = !mSearchView.isIconified();
                if(isSearchExpand){
                    mSearchView.setQuery(TransHistoryUtil.keyword, false);
                }
            }
        }
    };

    private AccountUtil.AccountUpdateListener accountListener = accountValue -> {
        reload();
    };

    private void reload(){
        if(binding != null && mMenu != null) {
            String keyword = TransHistoryUtil.keyword;
            if (keyword != null && !keyword.isEmpty()) {
                getSupportActionBar().setTitle(getString(R.string.search) + ": " + keyword);
            } else {
                getSupportActionBar().setTitle(R.string.title_activity_list);
            }
            mAdapter.reloadList();
            if (mAdapter.getItemCount() > 0) {
                binding.noData.setVisibility(View.INVISIBLE);
            } else {
                binding.noData.setVisibility(View.VISIBLE);
            }
            setFilterIconActive(TransHistoryUtil.isFilterActive());
        }
    }

    public TransHistoryActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                binding = ActivityTransHistoryBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                binding.drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                    }

                    @Override
                    public void onDrawerOpened(@NonNull View drawerView) {
//                setFilterActive(true);
                    }

                    @Override
                    public void onDrawerClosed(@NonNull View drawerView) {
//                setFilterActive(false);
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {

                    }
                });
                initFilter(getIntent());
                View view = binding.getRoot();
                Context context = view.getContext();
                RecyclerView recyclerView = view.findViewById(R.id.list);

                // Set the adapter
                if (recyclerView != null) {
                    if (mColumnCount <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                    }
//            mAdapter = new CashAdapter();
//            recyclerView.setAdapter(mAdapter);
//            mAdapter.reloadList();
                    mAdapter = new TransHistoryAdapter();
                    recyclerView.setAdapter(mAdapter);
                }
            } else if(event.equals(Lifecycle.Event.ON_START)){
                AccountUtil.addListener(accountListener);
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                AccountUtil.removeListener(accountListener);
                mSearchView.removeOnLayoutChangeListener(mSearchLayoutChangListener);
            }
        });
    }

    private void initFilter(Intent intent){
        try {
            String sVal = intent.getStringExtra(EXTRA_KEYWORD);
            if(sVal != null) TransHistoryUtil.keyword = sVal;
        } catch (Exception e){}
        try {
            int[] iArray = intent.getIntArrayExtra(EXTRA_TARGET_TYPES);
            if(iArray != null && iArray.length > 0){
                TransHistoryUtil.targetTypes.clear();
                for(int type: iArray) TransHistoryUtil.targetTypes.add(type);
            }
        } catch (Exception e){}
        TransHistoryUtil.startTime = intent.getLongExtra(EXTRA_START_TIME, TransHistoryUtil.startTime);
        TransHistoryUtil.endTime = intent.getLongExtra(EXTRA_END_TIME, TransHistoryUtil.endTime);
    }

    private void onSearchApply(String keyword){
        mSearchView.onActionViewCollapsed();
        TransHistoryUtil.keyword = keyword;
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
        getMenuInflater().inflate(R.menu.menu_list, menu);
        mFilterItem = mMenu.findItem(R.id.app_bar_filter);
        mSearchItem = mMenu.findItem(R.id.app_bar_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchSrcTextView = mSearchView.findViewById(R.id.search_src_text);
        mSearchCloseBtn = mSearchView.findViewById(R.id.search_close_btn);
        mSearchCloseBtn.setOnClickListener(v -> {
            onSearchApply("");
        });
        List<String> stockNameList = new ArrayList<>();

        for(AccountUtil.StockValue value: AccountUtil.getAccount().stockValueMap.values()){
            stockNameList.add(value.info.getStockNameWithId());
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
        mSearchSrcTextView.setThreshold(0);
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

    private void setFilterIconActive(boolean isActive){
        if(isActive == isFilterIconActive()) return;
        if(isActive){
            Drawable drawable = getDrawable(R.drawable.ic_filter_active);
            drawable.setColorFilter(getColor(R.color.sub_m), PorterDuff.Mode.SRC_ATOP);
            mFilterItem.setIcon(drawable);
            mFilterItem.setChecked(true);
        } else {
            mFilterItem.setIcon(R.drawable.ic_baseline_filter_alt_24);
            mFilterItem.setChecked(false);
        }
    }

    private boolean isFilterIconActive(){
        return mFilterItem.isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.app_bar_filter:
                //TODO filter
//                setFilterActive(!TransFilter.isChecked());
                binding.drawer.openDrawer(GravityCompat.END);
                return true;
//            case R.id.app_bar_search:
//                //TODO search
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        if(binding.drawer.isDrawerVisible(GravityCompat.END)){
            binding.drawer.closeDrawer(GravityCompat.END);
        } else if(!mSearchView.isIconified()){
            mSearchView.onActionViewCollapsed();
            mSearchItem.collapseActionView();
            MenuItemCompat.collapseActionView(mSearchItem);
        } else if(TransHistoryUtil.keyword != null && !TransHistoryUtil.keyword.isEmpty()) {
            onSearchApply("");
        } else {
            super.onBackPressed();
        }
    }

}