package com.msi.stockmanager.ui.main.news;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.msi.stockmanager.databinding.ActivityNewsBinding;
import com.msi.stockmanager.ui.main.pager.PagerAdapter;

public class NewsActivity extends AppCompatActivity {
    private static final String TAG = NewsActivity.class.getSimpleName();
    private ActivityNewsBinding binding;
    private NewsPagerAdapter mAdapter;

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int state) {
            String stateStr = "";
            switch (state){
                case ViewPager.SCROLL_STATE_IDLE:
                    stateStr = "SCROLL_STATE_IDLE";
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    stateStr = "SCROLL_STATE_DRAGGING";
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    stateStr = "SCROLL_STATE_SETTLING";
                    onPageReadyToShow();
                    break;
            }
            Log.d(TAG, "onPageScrollStateChanged: " + stateStr);
        }
    };

    private void onPageReadyToShow(){
        int currentIdx = binding.viewPager.getCurrentItem();
        ((NewsFragment)mAdapter.getItem(currentIdx)).showContent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new NewsPagerAdapter(this, getSupportFragmentManager());
        binding.viewPager.addOnPageChangeListener(onPageChangeListener);
        binding.viewPager.setAdapter(mAdapter);
        binding.tabs.setupWithViewPager(binding.viewPager);
        onPageReadyToShow();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }
}