package com.msi.stockmanager.ui.main.pager;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityPagerBinding;
import com.msi.stockmanager.ui.main.trans_history.TransHistoryActivity;
import com.msi.stockmanager.ui.main.form.FormActivity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class PagerActivity extends AppCompatActivity {
    private static final String TAG = PagerActivity.class.getSimpleName();
    private static final int MSG_FAB_SHOW = 0x1001;
    private static final int DELAY_FAB_SHOW = 200;
    private static final int MSG_JUMP_PENDING = 0x1002;
    private static final int DELAY_JUMP_PENDING = 350;
    private ActivityPagerBinding binding;
    private PagerAdapter pagerAdapter;
    private int currentPagePosition = 0;
    private boolean isFabShowing = false;
    private boolean isTouchEnable = false;
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
                case MSG_JUMP_PENDING:
                    startActivity((Intent)msg.obj);
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
                    Intent intent = new Intent(PagerActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_CASH_IN));
                    startActivity(intent);
                });
                binding.fabHoldingAdd.setOnClickListener(v->{
                    Intent intent = new Intent(PagerActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_BUY));
                    startActivity(intent);
                });
                binding.fabHistoryAdd.setOnClickListener(v->{
                    Intent intent = new Intent(PagerActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_SELL));
                    startActivity(intent);
                });
                binding.fabOtherAddDividend.setOnClickListener(v -> {
                    Intent intent = new Intent(PagerActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_DIVIDEND));
                    startActivity(intent);
                });
                binding.fabOtherAddReduction.setOnClickListener(v -> {
                    Intent intent = new Intent(PagerActivity.this, FormActivity.class);
                    intent.putExtra(Constants.EXTRA_TRANS_OBJECT, new Transaction(TransType.TRANS_TYPE_STOCK_REDUCTION));
                    startActivity(intent);
                });
                binding.fabOtherAdd.setOnMenuButtonClickListener(view -> {
                    if(!binding.fabOtherAdd.isOpened()) {
                        binding.fabOtherContainer.setClickable(true);
                        binding.fabOtherContainer.setBackgroundColor(getColor(R.color.transparent_bg_1));
                        binding.fabOtherAdd.open(true);
                    } else {
                        binding.fabOtherAdd.close(true);
                    }
                });
                binding.fabOtherAdd.setOnMenuToggleListener(opened -> {
                    if(!opened){
                        binding.fabOtherContainer.setClickable(false);
                        binding.fabOtherContainer.setBackgroundColor(getColor(R.color.transparent));
                    }
                });
                binding.fabOtherContainer.setOnClickListener(v->{
                    binding.fabOtherAdd.close(true);
                });
                binding.fabOtherContainer.setClickable(false);
                binding.fabOtherContainer.setBackgroundColor(getColor(R.color.transparent));

                getSupportActionBar().setTitle(pagerAdapter.getPageTitleId(currentPagePosition));
                showFab();
            } else if(event.equals(Lifecycle.Event.ON_START)){
                isTouchEnable = true;
                binding.fabOtherAdd.close(false);
            } else if(event.equals(Lifecycle.Event.ON_STOP)){
                binding.fabOtherAdd.close(false);
            } else if(event.equals(Lifecycle.Event.ON_DESTROY)){
                if(binding.viewPager != null)
                    binding.viewPager.removeOnPageChangeListener(onPageChangeListener);
                if(binding.tabs != null)
                    binding.tabs.removeOnTabSelectedListener(onTabSelectedListener);
            }
        });
    }

    @Override
    public void startActivity(Intent intent){
        isTouchEnable = false;
        if(binding.fabOtherAdd.isOpened()){
            binding.fabOtherAdd.close(true);
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
        getMenuInflater().inflate(R.menu.menu_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_history:
                Intent intent = new Intent(PagerActivity.this, TransHistoryActivity.class);
                switch (pagerAdapter.getPageTitleId(currentPagePosition)) {
                    case R.string.tab_text_cash:
                        intent.putExtra(TransHistoryActivity.EXTRA_TARGET_TYPES, new int[]{
                           TransType.TRANS_TYPE_CASH_IN,
                           TransType.TRANS_TYPE_CASH_OUT,
                        });
                        break;
                    case R.string.tab_text_stock_holding:
                    case R.string.tab_text_stock_history:
                        intent.putExtra(TransHistoryActivity.EXTRA_TARGET_TYPES, new int[]{
                                TransType.TRANS_TYPE_STOCK_SELL,
                                TransType.TRANS_TYPE_STOCK_BUY,
                        });
                        break;
                    case R.string.tab_text_other:
                        intent.putExtra(TransHistoryActivity.EXTRA_TARGET_TYPES, new int[]{
                                TransType.TRANS_TYPE_CASH_DIVIDEND,
                                TransType.TRANS_TYPE_STOCK_DIVIDEND,
                                TransType.TRANS_TYPE_CASH_REDUCTION,
                                TransType.TRANS_TYPE_STOCK_REDUCTION,
                        });
                        break;
                }
                startActivity(intent);
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
        if(!binding.fabHistoryAdd.isHidden()){
            binding.fabHistoryAdd.setVisibility(View.GONE);
            binding.fabHistoryAdd.hide(animation);
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
                case R.string.tab_text_stock_history:
                    binding.fabHistoryAdd.setVisibility(View.VISIBLE);
                    binding.fabHistoryAdd.show(true);
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
        if(!binding.fabHistoryAdd.isHidden()) return true;
        if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
        return false;
    }

    private boolean isCurrentFabShowing(){
        if(!isFabShowing) return false;
        switch (pagerAdapter.getPageTitleId(currentPagePosition)) {
            case R.string.tab_text_cash:
                if(!binding.fabCashAdd.isHidden()) return true;
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
            case R.string.tab_text_stock_holding:
                if(!binding.fabHoldingAdd.isHidden()) return true;
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
            case R.string.tab_text_stock_history:
                if(!binding.fabHistoryAdd.isHidden()) return true;
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
            case R.string.tab_text_other:
                if(!binding.fabOtherAdd.isMenuButtonHidden()) return true;
                break;
        }
        return false;
    }
}