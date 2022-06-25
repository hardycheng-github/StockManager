package com.msi.stockmanager.ui.main.pager.history;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.ActivityOverviewBinding;
import com.msi.stockmanager.databinding.FragmentHistoryBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private HistoryAdapter mAdapter;
    //    private HistoryAdapter mAdapter;
    private FragmentHistoryBinding binding;
    private AccountUtil.AccountUpdateListener accountListener = accountValue -> {
        if(binding != null){
            binding.historyCost.setText(FormatUtil.number(accountValue.historyCostTotal));
            int profit = Math.abs(accountValue.historyProfitTotal);
            double percent = Math.abs(accountValue.historyProfitRate);

            if(accountValue.historyProfitTotal < 0){
                binding.historyProfit.setTextColor(ColorUtil.getProfitLose());
                binding.historyProfit.setText("-"+FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitLose());
                binding.profitRate.setText("-"+FormatUtil.percent(percent));
            } else if(accountValue.historyProfitTotal > 0){
                binding.historyProfit.setTextColor(ColorUtil.getProfitEarn());
                binding.historyProfit.setText("+"+FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitEarn());
                binding.profitRate.setText("+"+FormatUtil.percent(percent));
            } else {
                binding.historyProfit.setTextColor(ColorUtil.getProfitNone());
                binding.historyProfit.setText(FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitNone());
                binding.profitRate.setText(FormatUtil.percent(percent));
            }
            mAdapter.reloadList();
            if(mAdapter.getItemCount() > 0){
                binding.noData.setVisibility(View.INVISIBLE);
            } else {
                binding.noData.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HistoryFragment newInstance(int columnCount) {
        HistoryFragment fragment = new HistoryFragment();
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
//        ApiUtil.transApi.addTransUpdateListener(listener);
//        onTitleValueChange();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
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
            mAdapter = new HistoryAdapter();
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        AccountUtil.addListener(accountListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        AccountUtil.removeListener(accountListener);
    }
}