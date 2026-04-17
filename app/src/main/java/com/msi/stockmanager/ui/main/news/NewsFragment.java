package com.msi.stockmanager.ui.main.news;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.news.INewsApi;
import com.msi.stockmanager.databinding.FragmentNewsBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class NewsFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private int newsType;
    private NewsAdapter mAdapter;
    //    private NewsAdapter mAdapter;
    private FragmentNewsBinding binding;
    private boolean isRefresh = false;
    private boolean isInit = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    private NewsFragment(int type) {
        newsType = type;
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewsFragment newInstance(int columnCount, int type) {
        NewsFragment fragment = new NewsFragment(type);
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNewsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.list);

        binding.refresh.setOnRefreshListener(()->{
            refresh(true);
        });

        isInit = true;
        if(isRefresh){
            refresh(false);
        } else {
            binding.getRoot().setVisibility(View.INVISIBLE);
        }

        // Set the adapter
        if (recyclerView != null) {
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new NewsAdapter();
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    public void showContent(){
        if(isInit) binding.getRoot().setVisibility(View.VISIBLE);
        if(!isRefresh) refresh(false);
    }



    private void refresh(boolean force){
        isRefresh = true;
        if(!isInit) return;
        boolean hasCache = ApiUtil.newsApi != null && ApiUtil.newsApi.hasCache(newsType);
        if (force) {
            binding.refresh.setRefreshing(true);
            if (mAdapter == null || mAdapter.getItemCount() == 0) {
                binding.loading.setVisibility(View.VISIBLE);
            } else {
                binding.loading.setVisibility(View.INVISIBLE);
            }
        } else {
            binding.refresh.setRefreshing(false);
            binding.loading.setVisibility(hasCache ? View.INVISIBLE : View.VISIBLE);
        }
        binding.list.setVisibility(hasCache ? View.VISIBLE : View.INVISIBLE);
        binding.noData.setVisibility(View.INVISIBLE);

        ApiUtil.newsApi.getNewsList(newsType, force, new INewsApi.ResultCallback() {
            @Override
            public void onResult(List<INewsApi.NewsItem> newsItemList) {
                List<INewsApi.NewsItem> displayList = getDisplayList(newsItemList);
                binding.refresh.setRefreshing(false);
                binding.list.setVisibility(View.VISIBLE);
                if(displayList.size() > 0){
                    mAdapter.reloadList(displayList);
                    binding.noData.setVisibility(View.INVISIBLE);
                } else {
                    binding.noData.setVisibility(View.VISIBLE);
                }
                binding.loading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onException(Exception e) {
                binding.refresh.setRefreshing(false);
                binding.noData.setVisibility(View.VISIBLE);
                binding.list.setVisibility(View.INVISIBLE);
                binding.loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private List<INewsApi.NewsItem> getDisplayList(List<INewsApi.NewsItem> source) {
        if (newsType != INewsApi.TYPE_ALL) {
            return source;
        }
        List<INewsApi.NewsItem> filtered = new ArrayList<>();
        for (INewsApi.NewsItem item : source) {
            if (item.type != INewsApi.TYPE_BULLETIN) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }
}