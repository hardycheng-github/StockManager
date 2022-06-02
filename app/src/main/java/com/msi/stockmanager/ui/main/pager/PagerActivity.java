package com.msi.stockmanager.ui.main.pager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.msi.stockmanager.InputCashInOut;
import com.msi.stockmanager.R;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.databinding.ActivityPagerBinding;
import com.msi.stockmanager.ui.main.overview.OverviewActivity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class PagerActivity extends AppCompatActivity {
    private static final String TAG = PagerActivity.class.getSimpleName();
    private static final int MSG_FAB_SHOW = 0x1001;
    private static final int DELAY_FAB_SHOW = 200;
    private ActivityPagerBinding binding;
    private PagerAdapter pagerAdapter;
    private int currentPagePosition = 0;
    private boolean isFabShowing = false;
    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int state) {
            String stateStr = "";
            switch (state){
                case ViewPager.SCROLL_STATE_IDLE:
                    stateStr = "SCROLL_STATE_IDLE";
                    currentPagePosition = binding.viewPager.getCurrentItem();
                    showFab(DELAY_FAB_SHOW);
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    stateStr = "SCROLL_STATE_DRAGGING";
                    hideFab();
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    stateStr = "SCROLL_STATE_SETTLING";
                    currentPagePosition = binding.viewPager.getCurrentItem();
                    showFab(DELAY_FAB_SHOW);
                    break;
            }
            Log.d(TAG, "onPageScrollStateChanged: " + stateStr);
        }
    };
    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Log.d(TAG, "onTabSelected");
            currentPagePosition = tab.getPosition();
            getSupportActionBar().setTitle(pagerAdapter.getPageTitleId(currentPagePosition));
            showFab(DELAY_FAB_SHOW);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            Log.d(TAG, "onTabUnselected");
            hideFab(false);
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            Log.d(TAG, "onTabReselected");
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_FAB_SHOW:
                    showFab();
                    break;
            }
        }
    };

    public PagerActivity(){
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            Log.d(TAG, "onStateChanged: " + event.name());
            if(event.equals(Lifecycle.Event.ON_CREATE)){
                binding = ActivityPagerBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                setSupportActionBar(binding.pagerToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                hideFab(false);

                pagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
                binding.viewPager.setAdapter(pagerAdapter);
                binding.viewPager.addOnPageChangeListener(onPageChangeListener);
                binding.tabs.setupWithViewPager(binding.viewPager);
                binding.tabs.addOnTabSelectedListener(onTabSelectedListener);

                binding.fabCashAdd.setOnClickListener(v -> {
                    startActivity(new Intent(PagerActivity.this, InputCashInOut.class));
                });
                binding.fabHoldingAdd.setOnClickListener(v->{
                    //TODO add stock transaction
                });
                binding.fabOtherAdd.setOnClickListener(v -> {
                    //TODO add dividend or reduction transaction
                });

                getSupportActionBar().setTitle(pagerAdapter.getPageTitleId(currentPagePosition));
                showFab();
            } else if(event.equals(Lifecycle.Event.ON_DESTROY)){
                if(binding.viewPager != null)
                    binding.viewPager.removeOnPageChangeListener(onPageChangeListener);
                if(binding.tabs != null)
                    binding.tabs.removeOnTabSelectedListener(onTabSelectedListener);
            }
        });
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

    private void hideFab(){
        hideFab(true);
    }

    private void hideFab(boolean animation){
        mHandler.removeMessages(MSG_FAB_SHOW);
        isFabShowing = false;
        if(!binding.fabCashAdd.isHidden()){
            binding.fabCashAdd.setVisibility(View.GONE);
            binding.fabCashAdd.hide(animation);
        }
        if(!binding.fabHoldingAdd.isHidden()){
            binding.fabHoldingAdd.setVisibility(View.GONE);
            binding.fabHoldingAdd.hide(animation);
        }
        if(binding.fabOtherAdd.isOpened()){
            binding.fabOtherAdd.close(animation);
        }
        if(!binding.fabOtherAdd.isMenuButtonHidden()){
            binding.fabOtherAdd.setVisibility(View.GONE);
            binding.fabOtherAdd.hideMenuButton(animation);
        }
    }

    private void showFab(){
        showFab(0);
    }

    private void showFab(int delay){
        if(isCurrentFabShowing()) return;
        mHandler.removeMessages(MSG_FAB_SHOW);
        if(isAnyFabShowing() || delay > 0){
            delay = Integer.max(delay, 10);
            Log.d(TAG, "showFab delay: " + delay);
            mHandler.sendEmptyMessageDelayed(MSG_FAB_SHOW, delay);
        }
        else {
            isFabShowing = true;
            switch (pagerAdapter.getPageTitleId(currentPagePosition)) {
                case R.string.tab_text_cash:
                    binding.fabCashAdd.setVisibility(View.VISIBLE);
                    binding.fabCashAdd.show(true);
                    break;
                case R.string.tab_text_stock_holding:
                    binding.fabHoldingAdd.setVisibility(View.VISIBLE);
                    binding.fabHoldingAdd.show(true);
                    break;
                case R.string.tab_text_other:
                    binding.fabOtherAdd.setVisibility(View.VISIBLE);
                    binding.fabOtherAdd.showMenuButton(true);
                    break;
                default:
                    isFabShowing = false;
                    break;
            }
        }
    }

    private boolean isAnyFabShowing(){
        if(isFabShowing) return true;
        if(!binding.fabCashAdd.isHidden()) return true;
        if(!binding.fabHoldingAdd.isHidden()) return true;
        if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
        return false;
    }

    private boolean isCurrentFabShowing(){
        if(!isFabShowing) return false;
        switch (pagerAdapter.getPageTitleId(currentPagePosition)) {
            case R.string.tab_text_cash:
                if(!binding.fabCashAdd.isHidden()) return true;
                break;
            case R.string.tab_text_stock_holding:
                if(!binding.fabHoldingAdd.isHidden()) return true;
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
            case R.string.tab_text_other:
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
        }
        return false;
    }
}