package com.msi.stockmanager.ui.main.pager.holding;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.FragmentHoldingSummaryItemBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HoldingSummaryAdapter extends RecyclerView.Adapter<HoldingSummaryAdapter.ViewHolder> {
    private static final String TAG = HoldingSummaryAdapter.class.getSimpleName();

    public final List<AccountUtil.StockValue> mItems = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentHoldingSummaryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void reloadList(){
        try {
            mItems.clear();
            Map<String, AccountUtil.StockValue> map = AccountUtil.getAccount().stockValueMap;
            for(AccountUtil.StockValue value: map.values()){
                if(value.holdingAmount > 0){
                    mItems.add(value);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAG, "reloadList err: " + e.getMessage());
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AccountUtil.StockValue value = mItems.get(position);
        StockInfo info = value.info;
        Context context = holder.binding.getRoot().getContext();
        Activity activity = (Activity) context;
        holder.mValue = value;
        holder.binding.stockId.setTypeface(null, Typeface.BOLD);
        holder.binding.stockId.setText(info.getStockNameWithId());
        holder.binding.holdingAmount.setTypeface(null, Typeface.BOLD);
        holder.binding.holdingAmount.setText(FormatUtil.number(value.holdingAmount));
        holder.binding.holdingCalc.setTypeface(null, Typeface.BOLD);
        holder.binding.holdingCalc.setText(FormatUtil.number(value.holdingCalc));
        holder.binding.holdingCost.setTypeface(null, Typeface.BOLD);
        holder.binding.holdingCost.setText(FormatUtil.number(value.holdingCost));
        holder.binding.averageBuy.setTypeface(null, Typeface.BOLD);
        holder.binding.averageBuy.setText(FormatUtil.number(value.avgBuyPrice));
        holder.binding.realtimePrice.setTypeface(null, Typeface.BOLD);
        holder.binding.realtimePrice.setText(FormatUtil.number(info.getLastPrice()));
        holder.binding.profitCalc.setTypeface(null, Typeface.BOLD);
        holder.binding.profitRate.setTypeface(null, Typeface.BOLD);
        int profit = Math.abs(value.holdingProfit);
        double percent = Math.abs(value.holdingProfitRate);

        if(value.holdingProfit < 0){
            holder.binding.profitCalc.setTextColor(ColorUtil.getProfitLose());
            holder.binding.profitCalc.setText("-"+FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitLose());
            holder.binding.profitRate.setText("-"+FormatUtil.percent(percent));
        } else if(value.holdingProfit > 0){
            holder.binding.profitCalc.setTextColor(ColorUtil.getProfitEarn());
            holder.binding.profitCalc.setText("+"+FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitEarn());
            holder.binding.profitRate.setText("+"+FormatUtil.percent(percent));
        } else {
            holder.binding.profitCalc.setTextColor(ColorUtil.getProfitNone());
            holder.binding.profitCalc.setText(FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitNone());
            holder.binding.profitRate.setText(FormatUtil.percent(percent));
        }
        holder.binding.cardView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.RIGHT);

            // Inflating popup menu from popup_menu.xml file
            popupMenu.getMenuInflater().inflate(R.menu.item_edit, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId())
                    {
                        case R.id.item1:
//                            Intent intent = new Intent(context, FormActivity.class);
//                            intent.putExtra(Constants.EXTRA_TRANS_OBJECT, trans);
//                            context.startActivity(intent);
                            return true;
                        case R.id.item2:
                            new AlertDialog.Builder(context)
                                    .setMessage(String.format(context.getString(R.string.dialog_remove_stock_msg), info.getStockNameWithId()))
                                    .setPositiveButton(R.string.confirm, ((dialogInterface, i) -> {
                                        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
                                            if(trans.stock_id.equals(info.getStockId())){
                                                ApiUtil.transApi.removeTrans(trans.trans_id);
                                            }
                                        }
                                        reloadList();
                                        dialogInterface.dismiss();
                                    }))
                                    .setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                                        dialogInterface.cancel();
                                    }))
                                    .create()
                                    .show();
                            return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            // Showing the popup menu
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentHoldingSummaryItemBinding binding;
        public AccountUtil.StockValue mValue;

        public ViewHolder(FragmentHoldingSummaryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}