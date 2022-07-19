package com.msi.stockmanager.ui.main.news;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.news.INewsApi;
import com.msi.stockmanager.ui.main.pager.cash.CashFragment;
import com.msi.stockmanager.ui.main.pager.history.HistoryFragment;
import com.msi.stockmanager.ui.main.pager.holding.HoldingFragment;
import com.msi.stockmanager.ui.main.pager.other.OtherFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class NewsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    private List<PageInfo> pageList = new ArrayList<>();

    public NewsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        int columnCount = 1;
        mContext = context;
        pageList.clear();
        pageList.add(new PageInfo(NewsFragment.newInstance(columnCount, INewsApi.TYPE_ALL), R.string.news_type_all));
        pageList.add(new PageInfo(NewsFragment.newInstance(columnCount, INewsApi.TYPE_STOCK), R.string.news_type_stock));
        pageList.add(new PageInfo(NewsFragment.newInstance(columnCount, INewsApi.TYPE_BULLETIN), R.string.news_type_bulletin));
        pageList.add(new PageInfo(NewsFragment.newInstance(columnCount, INewsApi.TYPE_EXCHANGE), R.string.news_type_exchange));
        pageList.add(new PageInfo(NewsFragment.newInstance(columnCount, INewsApi.TYPE_CRYPTO), R.string.news_type_crypto));
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
//        return PlaceholderFragment.newInstance(position + 1);
        return pageList.get(position).fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(getPageTitleId(position));
    }

    public int getPageTitleId(int position){
        return pageList.get(position).titleId;
    }

    @Override
    public int getCount() {
        return pageList.size();
    }

    private int indexOf(int titleId){
        for(int i = 0; i < pageList.size(); i++){
            PageInfo pageInfo = pageList.get(i);
            if(pageInfo.titleId == titleId){
                return i;
            }
        }
        return -1;
    }

    class PageInfo {
        public Fragment fragment;
        public int titleId;
        public PageInfo(Fragment f, int tid){
            fragment = f;
            titleId = tid;
        }
    }
}