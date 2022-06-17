package com.msi.stockmanager.ui.main.pager.history;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ApiUtil;
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

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public final List<Transaction> mItems = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    public void reloadList(){
        mItems.clear();
        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
            switch (trans.trans_type){
                case TransType.TRANS_TYPE_STOCK_SELL:
                    mItems.add(trans);
                    break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Transaction trans = mItems.get(position);
        Context context = holder.binding.getRoot().getContext();
        Activity activity = (Activity) context;
        StockInfo info = StockUtilKt.getStockInfoOrNull(trans.stock_id);
        holder.mItem = trans;
        holder.mInfo = info;
        holder.binding.stockId.setTypeface(null, Typeface.BOLD);
        holder.binding.stockId.setText(info.getStockNameWithId());
        holder.binding.transDate.setTypeface(null, Typeface.BOLD);
        holder.binding.transDate.setText(DateUtil.toDateString(trans.trans_time));
        holder.binding.stockAmount.setTypeface(null, Typeface.BOLD);
        holder.binding.stockAmount.setText(FormatUtil.number(Math.abs(trans.stock_amount)));
        holder.binding.stockPrice.setTypeface(null, Typeface.BOLD);
        holder.binding.stockPrice.setText(FormatUtil.number(trans.stock_price));
        holder.binding.cashAmount.setTypeface(null, Typeface.BOLD);
        holder.binding.cashAmount.setText(FormatUtil.number(Math.abs(trans.cash_amount)));
        holder.binding.stockFee.setTypeface(null, Typeface.BOLD);
        holder.binding.stockFee.setText(FormatUtil.number(trans.fee));
        holder.binding.stockTax.setTypeface(null, Typeface.BOLD);
        holder.binding.stockTax.setText(FormatUtil.number(trans.tax));
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
                            Intent intent = new Intent(context, FormActivity.class);
                            intent.putExtra(Constants.EXTRA_TRANS_OBJECT, trans);
                            context.startActivity(intent);
                            return true;
                        case R.id.item2:
                            ApiUtil.transApi.removeTrans(trans.trans_id);
                            return true;
                    }
                    return onMenuItemClick(item);
                }
            });
            // Showing the popup menu
            popupMenu.show();
            return true;
        });
        calcUpdate(holder, false);
    }

    private void calcUpdate(ViewHolder holder, boolean forceUpdate){
        StockInfo info = holder.mInfo;
        Transaction trans = holder.mItem;
        Context context = holder.binding.getRoot().getContext();
        Activity activity = (Activity) context;

        if(!forceUpdate && holder.mInfo.getLastPrice() > 0){
            double diff = info.getLastPrice() - trans.stock_price;
            double percent = trans.stock_price > 0 ? (diff / trans.stock_price) : 0;
            int calcVal = (int) Math.floor(diff * trans.stock_amount);
            if(calcVal < 0){
                activity.runOnUiThread(()->{
                    holder.binding.calc.setTextColor(context.getColor(R.color.stock_lose));
                    holder.binding.calc.setText(String.format("%s (%s) ▼", FormatUtil.number(calcVal), FormatUtil.percent(percent)));
//                        holder.binding.calcImg.setColorFilter(context.getColor(R.color.stock_lose));
//                        holder.binding.calcImg.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
                });
            } else if(calcVal > 0){
                activity.runOnUiThread(()->{
                    holder.binding.calc.setTextColor(context.getColor(R.color.stock_earn));
                    holder.binding.calc.setText(String.format("%s (%s) ▲", FormatUtil.number(calcVal), FormatUtil.percent(percent)));
//                        holder.binding.calcImg.setColorFilter(context.getColor(R.color.stock_earn));
//                        holder.binding.calcImg.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                });
            } else {
                activity.runOnUiThread(()->{
                    holder.binding.calc.setTextColor(context.getColor(R.color.black));
                    holder.binding.calc.setText(String.format("%s (%s)", FormatUtil.number(calcVal), FormatUtil.percent(percent)));
//                        holder.binding.calcImg.setColorFilter(context.getColor(R.color.stock_earn));
//                        holder.binding.calcImg.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                });
            }
        } else {
            ApiUtil.stockApi.getRegularStockPrice(trans.stock_id, new IStockApi.ResultCallback() {
                @Override
                public void onResult(StockInfo info) {
                    if(info.getLastPrice() > 0){
                        calcUpdate(holder, false);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentHistoryItemBinding binding;
        public Transaction mItem;
        public StockInfo mInfo;

        public ViewHolder(FragmentHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }
}