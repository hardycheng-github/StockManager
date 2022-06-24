package com.msi.stockmanager.ui.main.pager.cash;

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
import com.msi.stockmanager.databinding.FragmentCashBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 */
public class CashFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private CashAdapter mAdapter;
    //    private CashAdapter mAdapter;
    private FragmentCashBinding binding;
    private AccountUtil.AccountUpdateListener accountListener = accountValue -> {
        if(binding != null){
            binding.accountTotal.setText(FormatUtil.number(accountValue.accountTotal));
            binding.accountBalance.setText(FormatUtil.number(accountValue.cashBalance));

            if(accountValue.cashInTotal > 0) {
                binding.CashIn.setTextColor(ColorUtil.getProfitEarn());
            } else {
                binding.CashIn.setTextColor(ColorUtil.getProfitNone());
            }
            binding.CashIn.setText(FormatUtil.number(accountValue.cashInTotal));

            if(accountValue.cashOutTotal > 0) {
                binding.CashOut.setTextColor(ColorUtil.getProfitLose());
            } else {
                binding.CashOut.setTextColor(ColorUtil.getProfitNone());
            }
            binding.CashOut.setText(FormatUtil.number(accountValue.cashOutTotal));

            mAdapter.reloadList();
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CashFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CashFragment newInstance(int columnCount) {
        CashFragment fragment = new CashFragment();
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

        binding = FragmentCashBinding.inflate(getLayoutInflater());
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
            mAdapter = new CashAdapter();
            recyclerView.setAdapter(mAdapter);
            mAdapter.reloadList();
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