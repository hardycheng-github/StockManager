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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.databinding.ActivityTransHistoryBinding;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private boolean activeSearch = false;

    private View dateStart;
    private View dateEnd;
    private TextView dateStartValue;
    private TextView dateEndValue;
    private Button btnToday;
    private Button btnRecentWeek;
    private Button btnRecentMonth;
    private Button btnRecentYear;
    private Button btnReset;
    private Button btnApply;
    private Button btnTypeStockBuy;
    private Button btnTypeStockSell;
    private Button btnTypeCashIn;
    private Button btnTypeCashOut;
    private Button btnTypeDividendStock;
    private Button btnTypeDividendCash;
    private Button btnTypeReductionStock;
    private Button btnTypeReductionCash;
    private Date selectedDate;

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
            updateFilter();
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
                findView();
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

    private void findView(){
        dateStart = binding.navView.findViewById(R.id.date_start);
        dateEnd = binding.navView.findViewById(R.id.date_end);
        dateStartValue = binding.navView.findViewById(R.id.date_start_value);
        dateEndValue = binding.navView.findViewById(R.id.date_end_value);
        btnToday = binding.navView.findViewById(R.id.today);
        btnRecentWeek = binding.navView.findViewById(R.id.recent_week);
        btnRecentMonth = binding.navView.findViewById(R.id.recent_month);
        btnRecentYear = binding.navView.findViewById(R.id.recent_year);
        btnReset = binding.navView.findViewById(R.id.reset);
        btnApply = binding.navView.findViewById(R.id.apply);

        btnTypeStockBuy = binding.navView.findViewById(R.id.TRANS_TYPE_STOCK_BUY);
        btnTypeStockSell = binding.navView.findViewById(R.id.TRANS_TYPE_STOCK_SELL);
        btnTypeCashIn = binding.navView.findViewById(R.id.TRANS_TYPE_CASH_IN);
        btnTypeCashOut = binding.navView.findViewById(R.id.TRANS_TYPE_CASH_OUT);
        btnTypeDividendStock = binding.navView.findViewById(R.id.TRANS_TYPE_STOCK_DIVIDEND);
        btnTypeDividendCash = binding.navView.findViewById(R.id.TRANS_TYPE_CASH_DIVIDEND);
        btnTypeReductionStock = binding.navView.findViewById(R.id.TRANS_TYPE_STOCK_REDUCTION);
        btnTypeReductionCash = binding.navView.findViewById(R.id.TRANS_TYPE_CASH_REDUCTION);
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

        dateStart.setOnClickListener(v -> {
            selectedDate = new Date();
            long timestamp = TransHistoryUtil.startTime == 0 ? System.currentTimeMillis() : TransHistoryUtil.startTime;
            Date date = new Date(timestamp);
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.setYear(year-1900);
                selectedDate.setMonth(month);
                selectedDate.setDate(dayOfMonth);
                dateStartValue.setText(DateUtil.toDateString(selectedDate.getTime()));
//                new TimePickerDialog(this, (view2, hourOfDay, minute) -> {
//                    selectedDate.setHours(hourOfDay);
//                    selectedDate.setMinutes(minute);
//                    dateStartValue.setText(DateUtil.toDateTimeString(selectedDate.getTime()));
//                }, date.getHours(), date.getMinutes(), false).show();
            }, date.getYear()+1900, date.getMonth(), date.getDate()).show();
        });
        dateEnd.setOnClickListener(v->{
            selectedDate = new Date();
            long timestamp = TransHistoryUtil.endTime == Long.MAX_VALUE ? System.currentTimeMillis() : TransHistoryUtil.endTime;
            Date date = new Date(timestamp);
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.setYear(year-1900);
                selectedDate.setMonth(month);
                selectedDate.setDate(dayOfMonth);
                dateEndValue.setText(DateUtil.toDateString(selectedDate.getTime()));
//                new TimePickerDialog(this, (view2, hourOfDay, minute) -> {
//                    selectedDate.setHours(hourOfDay);
//                    selectedDate.setMinutes(minute);
//                    dateEndValue.setText(DateUtil.toDateTimeString(selectedDate.getTime()));
//                }, date.getHours(), date.getMinutes(), false).show();
            }, date.getYear()+1900, date.getMonth(), date.getDate()).show();
        });
        btnToday.setOnClickListener(v->{
            updateButton(btnToday, !btnToday.isSelected());
            updateButton(btnRecentWeek, false);
            updateButton(btnRecentMonth, false);
            updateButton(btnRecentYear, false);
            if(v.isSelected()){
                Calendar end = Calendar.getInstance();
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);

                Calendar start = Calendar.getInstance();
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                dateStartValue.setText(DateUtil.toDateString(start.getTimeInMillis()));
                dateEndValue.setText(DateUtil.toDateString(end.getTimeInMillis()));
            } else {
                dateStartValue.setText(getString(R.string.not_select));
                dateEndValue.setText(getString(R.string.not_select));
            }
        });
        btnRecentWeek.setOnClickListener(v->{
            updateButton(btnToday, false);
            updateButton(btnRecentWeek, !btnRecentWeek.isSelected());
            updateButton(btnRecentMonth, false);
            updateButton(btnRecentYear, false);
            if(v.isSelected()){
                Calendar end = Calendar.getInstance();
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);

                Calendar start = Calendar.getInstance();
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                start.add(Calendar.DATE, -7);

                dateStartValue.setText(DateUtil.toDateString(start.getTimeInMillis()));
                dateEndValue.setText(DateUtil.toDateString(end.getTimeInMillis()));
            } else {
                dateStartValue.setText(getString(R.string.not_select));
                dateEndValue.setText(getString(R.string.not_select));
            }
        });
        btnRecentMonth.setOnClickListener(v->{
            updateButton(btnToday, false);
            updateButton(btnRecentWeek, false);
            updateButton(btnRecentMonth, !btnRecentMonth.isSelected());
            updateButton(btnRecentYear, false);
            if(v.isSelected()){
                Calendar end = Calendar.getInstance();
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);

                Calendar start = Calendar.getInstance();
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                start.add(Calendar.MONTH, -1);

                dateStartValue.setText(DateUtil.toDateString(start.getTimeInMillis()));
                dateEndValue.setText(DateUtil.toDateString(end.getTimeInMillis()));
            } else {
                dateStartValue.setText(getString(R.string.not_select));
                dateEndValue.setText(getString(R.string.not_select));
            }
        });
        btnRecentYear.setOnClickListener(v->{
            updateButton(btnToday, false);
            updateButton(btnRecentWeek, false);
            updateButton(btnRecentMonth, false);
            updateButton(btnRecentYear, !btnRecentYear.isSelected());
            if(v.isSelected()){
                Calendar end = Calendar.getInstance();
                end.set(Calendar.HOUR_OF_DAY, 23);
                end.set(Calendar.MINUTE, 59);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);

                Calendar start = Calendar.getInstance();
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                start.add(Calendar.YEAR, -1);

                dateStartValue.setText(DateUtil.toDateString(start.getTimeInMillis()));
                dateEndValue.setText(DateUtil.toDateString(end.getTimeInMillis()));
            } else {
                dateStartValue.setText(getString(R.string.not_select));
                dateEndValue.setText(getString(R.string.not_select));
            }
        });
        btnReset.setOnClickListener(v->{
            updateButton(btnToday, false);
            updateButton(btnRecentWeek, false);
            updateButton(btnRecentMonth, false);
            updateButton(btnRecentYear, false);
            TransHistoryUtil.resetFilter();
            binding.drawer.closeDrawer(GravityCompat.END);
            reload();
        });
        btnApply.setOnClickListener(v->{
            TransHistoryUtil.startTime = DateUtil.parseDate(dateStartValue.getText().toString());
            TransHistoryUtil.endTime = DateUtil.parseDate(dateEndValue.getText().toString());
            if(TransHistoryUtil.endTime == 0) TransHistoryUtil.endTime = Long.MAX_VALUE;
            TransHistoryUtil.targetTypes.clear();
            if(btnTypeStockBuy.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_STOCK_BUY);
            if(btnTypeStockSell.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_STOCK_SELL);
            if(btnTypeCashIn.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_CASH_IN);
            if(btnTypeCashOut.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_CASH_OUT);
            if(btnTypeDividendCash.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_CASH_DIVIDEND);
            if(btnTypeDividendStock.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_STOCK_DIVIDEND);
            if(btnTypeReductionCash.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_CASH_REDUCTION);
            if(btnTypeReductionStock.isSelected()) TransHistoryUtil.targetTypes.add(TransType.TRANS_TYPE_STOCK_REDUCTION);
            binding.drawer.closeDrawer(GravityCompat.END);
            reload();
        });

        btnTypeStockBuy.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeStockSell.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeCashIn.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeCashOut.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeDividendStock.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeDividendCash.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeReductionStock.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        btnTypeReductionCash.setOnClickListener(v->updateButton((Button)v, !v.isSelected()));
        updateButton(btnToday, false);
        updateButton(btnRecentWeek, false);
        updateButton(btnRecentMonth, false);
        updateButton(btnRecentYear, false);
        updateFilter();
    }

    private void updateFilter(){
        dateStartValue.setText(TransHistoryUtil.startTime == 0 ? getString(R.string.not_select)
                : DateUtil.toDateString(TransHistoryUtil.startTime));
        dateEndValue.setText(TransHistoryUtil.endTime == Long.MAX_VALUE ? getString(R.string.not_select)
                : DateUtil.toDateString(TransHistoryUtil.endTime));

        updateButton(btnTypeStockBuy, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_STOCK_BUY));
        updateButton(btnTypeStockSell, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_STOCK_SELL));
        updateButton(btnTypeCashIn, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_CASH_IN));
        updateButton(btnTypeCashOut, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_CASH_OUT));
        updateButton(btnTypeDividendStock, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_STOCK_DIVIDEND));
        updateButton(btnTypeDividendCash, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_CASH_DIVIDEND));
        updateButton(btnTypeReductionStock, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_STOCK_REDUCTION));
        updateButton(btnTypeReductionCash, TransHistoryUtil.targetTypes.contains(TransType.TRANS_TYPE_CASH_REDUCTION));
    }

    private void updateButton(Button btn, boolean isSelected){
        if(isSelected){
            btn.setSelected(true);
            btn.setBackground(getDrawable(R.drawable.ic_btn_main));
            btn.setTextColor(getColor(R.color.white));
        } else {
            btn.setSelected(false);
            btn.setBackground(getDrawable(R.drawable.ic_btn_sub));
            btn.setTextColor(getColor(R.color.main_m));
        }
    }

    private void onSearchApply(String keyword){
        activeSearch = true;
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
            drawable.setColorFilter(getColor(R.color.sub_s), PorterDuff.Mode.SRC_ATOP);
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
        } else if(activeSearch && TransHistoryUtil.keyword != null && !TransHistoryUtil.keyword.isEmpty()) {
            onSearchApply("");
        } else {
            super.onBackPressed();
        }
    }

}