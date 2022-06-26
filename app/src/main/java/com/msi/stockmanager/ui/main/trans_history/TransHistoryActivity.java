package com.msi.stockmanager.ui.main.trans_history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.msi.stockmanager.R;
import com.msi.stockmanager.databinding.ActivityTransHistoryBinding;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TransHistoryActivity extends AppCompatActivity {
    public static final String TAG = TransHistoryActivity.class.getSimpleName();
    public static final String EXTRA_KEYWORD = "EXTRA_KEYWORD";
    public static final String EXTRA_TARGET_TYPES = "EXTRA_TARGET_TYPES";
    public static final String EXTRA_START_TIME = "EXTRA_START_TIME";
    public static final String EXTRA_END_TIME = "EXTRA_END_TIME";

    private ActivityTransHistoryBinding binding;
    private Menu mMenu;
    private MenuItem TransFilterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_list, menu);
        TransFilterItem = mMenu.findItem(R.id.app_bar_filter);
        setFilterActive(false);
        return true;
    }

    private void setFilterActive(boolean isActive){
        if(isActive){
            Drawable drawable = getDrawable(R.drawable.ic_filter_active);
            drawable.setColorFilter(getColor(R.color.sub_m), PorterDuff.Mode.SRC_ATOP);
            TransFilterItem.setIcon(drawable);
            TransFilterItem.setChecked(true);
        } else {
            TransFilterItem.setIcon(R.drawable.ic_baseline_filter_alt_24);
            TransFilterItem.setChecked(false);
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
                if(!TransFilterItem.isChecked()){
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

}