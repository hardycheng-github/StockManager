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
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityTransHistoryBinding;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private TransHistoryAdapter mAdapter;
    private int mColumnCount = 1;

    private AccountUtil.AccountUpdateListener accountListener = accountValue -> {
        if(binding != null){
            mAdapter.reloadList();
            if(mAdapter.getItemCount() > 0){
                binding.noData.setVisibility(View.INVISIBLE);
            } else {
                binding.noData.setVisibility(View.VISIBLE);
            }
        }
    };

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

    private void onSearchChanged(String keyword){
        TransHistoryUtil.keyword = keyword;
        mAdapter.reloadList();
        if(mAdapter.getItemCount() > 0){
            binding.noData.setVisibility(View.INVISIBLE);
        } else {
            binding.noData.setVisibility(View.VISIBLE);
        }
    }

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
            mSearchView.setQuery("", true);
        });
        List<String> stockNameList = new ArrayList<>();
        for(StockInfo info: StockUtilKt.getStockUtil().getStockList()){
            stockNameList.add(info.getStockNameWithId());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(TransHistoryActivity.this,
                android.R.layout.simple_dropdown_item_1line, stockNameList);
        mSearchSrcTextView.setAdapter(adapter);
        mSearchSrcTextView.setOnItemClickListener((parent, view, position, id) -> {
            String searchString = (String)parent.getItemAtPosition(position);
            mSearchView.setQuery(searchString, true);
        });
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
                mSearchItem.collapseActionView();
                MenuItemCompat.collapseActionView(mSearchItem);
                onSearchChanged(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        setFilterActive(false);
        return true;
    }

    private void setFilterActive(boolean isActive){
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.app_bar_filter:
                //TODO filter
//                setFilterActive(!TransFilter.isChecked());
                if(!mFilterItem.isChecked()){
                    binding.drawer.openDrawer(GravityCompat.END);
                } else {
                    binding.drawer.closeDrawer(GravityCompat.END);
                }

                return true;
            case R.id.app_bar_search:
                //TODO search
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