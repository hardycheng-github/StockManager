package com.msi.stockmanager.ui.main.pager.history;

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
import com.msi.stockmanager.databinding.FragmentHistoryItemBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.trans_history.TransHistoryActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = HistoryAdapter.class.getSimpleName();
    private Context mContext;

    public final List<AccountUtil.StockValue> mItems = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(FragmentHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void reloadList(){
        try {
            mItems.clear();
            Map<String, AccountUtil.StockValue> map = AccountUtil.getAccount().stockValueMap;
            for(AccountUtil.StockValue value: map.values()){
                if(value.sellAmount > 0){
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
        holder.binding.title.setText(info.getStockNameWithId());
        holder.binding.averageBuy.setText(FormatUtil.number(value.avgBuyPrice));
        holder.binding.averageSell.setText(FormatUtil.number(value.avgSellPrice));
        holder.binding.historyCost.setText(FormatUtil.number(value.historyCost));
        holder.binding.transStockAmount.setText(FormatUtil.number(value.sellAmount));
        int profit = Math.abs(value.historyProfit);
        double percent = Math.abs(value.historyProfitRate);

        if(value.historyProfit < 0){
            holder.binding.historyProfit.setTextColor(ColorUtil.getProfitLose());
            holder.binding.historyProfit.setText("-"+FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitLose());
            holder.binding.profitRate.setText("-"+FormatUtil.percent(percent));
        } else if(value.historyProfit > 0){
            holder.binding.historyProfit.setTextColor(ColorUtil.getProfitEarn());
            holder.binding.historyProfit.setText("+"+FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitEarn());
            holder.binding.profitRate.setText("+"+FormatUtil.percent(percent));
        } else {
            holder.binding.historyProfit.setTextColor(ColorUtil.getProfitNone());
            holder.binding.historyProfit.setText(FormatUtil.number(profit));
            holder.binding.profitRate.setTextColor(ColorUtil.getProfitNone());
            holder.binding.profitRate.setText(FormatUtil.percent(percent));
        }
        holder.binding.cardView.setOnClickListener(v->onEdit(info));
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
                            onEdit(info);
                            return true;
                        case R.id.item2:
                            onRemove(info);
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

    private void onEdit(StockInfo info){
        Intent intent = new Intent(mContext, TransHistoryActivity.class);
        intent.putExtra(TransHistoryActivity.EXTRA_KEYWORD, info.getStockNameWithId());
        intent.putExtra(TransHistoryActivity.EXTRA_TARGET_TYPES, new int[]{
                TransType.TRANS_TYPE_STOCK_BUY,
                TransType.TRANS_TYPE_STOCK_SELL
        });
        mContext.startActivity(intent);
    }

    private void onRemove(StockInfo info){
        new AlertDialog.Builder(mContext)
                .setMessage(String.format(mContext.getString(R.string.dialog_remove_stock_msg), info.getStockNameWithId()))
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
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentHistoryItemBinding binding;
        public AccountUtil.StockValue mValue;

        public ViewHolder(FragmentHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}