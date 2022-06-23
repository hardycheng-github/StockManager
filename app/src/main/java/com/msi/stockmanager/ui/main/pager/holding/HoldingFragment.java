package com.msi.stockmanager.ui.main.pager.holding;

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
import com.msi.stockmanager.databinding.FragmentHoldingBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class HoldingFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private HoldingSummaryAdapter mSummaryAdapter;
//    private HoldingAdapter mAdapter;
    private FragmentHoldingBinding binding;
    private AccountUtil.AccountUpdateListener accountListener = accountValue -> {
        if(binding != null){
            binding.stockValueCalc.setTypeface(null, Typeface.BOLD);
            binding.stockValueCalc.setText(FormatUtil.number(accountValue.stockCalcTotal));
            binding.investedCost.setTypeface(null, Typeface.BOLD);
            binding.investedCost.setText(FormatUtil.number(accountValue.stockCostTotal));
            binding.profitCalc.setTypeface(null, Typeface.BOLD);
            binding.profitRate.setTypeface(null, Typeface.BOLD);
            int profit = Math.abs(accountValue.stockProfitTotal);
            double percent = Math.abs(accountValue.stockProfitRate);

            if(accountValue.stockProfitTotal < 0){
                binding.profitCalc.setTextColor(ColorUtil.getProfitLose());
                binding.profitCalc.setText("-"+FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitLose());
                binding.profitRate.setText("-"+FormatUtil.percent(percent));
            } else if(accountValue.stockProfitTotal > 0){
                binding.profitCalc.setTextColor(ColorUtil.getProfitEarn());
                binding.profitCalc.setText("+"+FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitEarn());
                binding.profitRate.setText("+"+FormatUtil.percent(percent));
            } else {
                binding.profitCalc.setTextColor(ColorUtil.getProfitNone());
                binding.profitCalc.setText(FormatUtil.number(profit));
                binding.profitRate.setTextColor(ColorUtil.getProfitNone());
                binding.profitRate.setText(FormatUtil.percent(percent));
            }
            mSummaryAdapter.reloadList();
        }
    };
//    private ITransApi.TransUpdateListener listener = new ITransApi.TransUpdateListener() {
//        @Override
//        public void onAdd(Transaction trans) {
//            switch (trans.trans_type){
//                case TransType.TRANS_TYPE_STOCK_BUY: case TransType.TRANS_TYPE_STOCK_SELL:
//                    mAdapter.mItems.add(trans);
//                    mAdapter.notifyItemInserted(mAdapter.mItems.size());
//                    break;
//            }
//            onTitleValueChange();
//        }
//
//        @Override
//        public void onEdit(long transId, Transaction trans) {
//            for(int i = 0; i < mAdapter.mItems.size(); i++) {
//                if(mAdapter.mItems.get(i).trans_id == transId) {
//                    mAdapter.mItems.set(i, trans);
//                    mAdapter.notifyItemChanged(i);
//                    break;
//                }
//            }
//            onTitleValueChange();
//        }
//
//        @Override
//        public void onRemove(long transId) {
//            for(int i = 0; i < mAdapter.mItems.size(); i++) {
//                if(mAdapter.mItems.get(i).trans_id == transId) {
//                    mAdapter.mItems.remove(i);
//                    mAdapter.notifyItemRemoved(i);
//                    break;
//                }
//            }
//            onTitleValueChange();
//        }
//    };

//    private void onTitleValueChange(){
//        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
//            Map<String, Integer> stockRemaining = new HashMap<>();
//            //TODO title value change
//        }
//    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HoldingFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HoldingFragment newInstance(int columnCount) {
        HoldingFragment fragment = new HoldingFragment();
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

        binding = FragmentHoldingBinding.inflate(getLayoutInflater());
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
//            mAdapter = new HoldingAdapter();
//            recyclerView.setAdapter(mAdapter);
//            mAdapter.reloadList();
            mSummaryAdapter = new HoldingSummaryAdapter();
            recyclerView.setAdapter(mSummaryAdapter);
            mSummaryAdapter.reloadList();
            AccountUtil.addListener(accountListener);
        }
        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        AccountUtil.removeListener(accountListener);
//        ApiUtil.transApi.removeTransUpdateListener(listener);
    }
}