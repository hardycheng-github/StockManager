package com.msi.stockmanager.ui.main.list;

import androidx.appcompat.app.AppCompatActivity;

import com.msi.stockmanager.R;
import com.msi.stockmanager.databinding.ActivityListBinding;
import com.msi.stockmanager.databinding.ActivityPagerBinding;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ListActivity extends AppCompatActivity {

    private ActivityListBinding binding;
    private Menu mMenu;
    private MenuItem mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_list, menu);
        mFilter = mMenu.findItem(R.id.app_bar_filter);
        setFilterActive(false);
        return true;
    }

    private void setFilterActive(boolean isActive){
        if(isActive){
            Drawable drawable = getDrawable(R.drawable.ic_filter_active);
            drawable.setColorFilter(getColor(R.color.sub_s), PorterDuff.Mode.SRC_ATOP);
            mFilter.setIcon(drawable);
            mFilter.setChecked(true);
        } else {
            mFilter.setIcon(R.drawable.ic_baseline_filter_alt_24);
            mFilter.setChecked(false);
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
                setFilterActive(!mFilter.isChecked());
                return true;
            case R.id.app_bar_search:
                //TODO search
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}