package com.msi.stockmanager.ui.main.trans_history;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.TransHistoryItemBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;

import java.util.ArrayList;
import java.util.List;

public class TransHistoryAdapter extends RecyclerView.Adapter<TransHistoryAdapter.ViewHolder> {

    public final List<Transaction> mItems = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(TransHistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void reloadList(){
        mItems.clear();
        mItems.addAll(TransHistoryUtil.getListWithFilter());
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
        holder.binding.title.setText(FormatUtil.transType(trans.trans_type));
        holder.binding.transDate.setText(DateUtil.toDateString(trans.trans_time));
        holder.binding.transStockNameContainer.setVisibility(View.GONE);
        holder.binding.transStockAmountContainer.setVisibility(View.GONE);
        holder.binding.transStockPriceContainer.setVisibility(View.GONE);
        holder.binding.transStockFeeContainer.setVisibility(View.GONE);
        holder.binding.transStockTaxContainer.setVisibility(View.GONE);
        holder.binding.transCashContainer.setVisibility(View.GONE);
        switch (trans.trans_type){
            case TransType.TRANS_TYPE_CASH_IN:
            case TransType.TRANS_TYPE_CASH_OUT:
                holder.binding.transCashContainer.setVisibility(View.VISIBLE);
                holder.binding.transCash.setText(FormatUtil.number(trans.cash_amount));
                break;
            case TransType.TRANS_TYPE_STOCK_SELL:
                holder.binding.transStockTaxContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockTax.setText(FormatUtil.number(trans.tax));
            case TransType.TRANS_TYPE_STOCK_BUY:
                holder.binding.transStockNameContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockName.setText(info.getStockNameWithId());
                holder.binding.transStockAmountContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockAmount.setText(FormatUtil.number(trans.stock_amount));
                holder.binding.transStockPriceContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockPrice.setText(FormatUtil.number(trans.stock_price));
                holder.binding.transStockFeeContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockFee.setText(FormatUtil.number(trans.fee));
                holder.binding.transCashContainer.setVisibility(View.VISIBLE);
                holder.binding.transCash.setText(FormatUtil.number(trans.cash_amount));
                break;
            case TransType.TRANS_TYPE_CASH_DIVIDEND:
                holder.binding.transStockNameContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockName.setText(info.getStockNameWithId());
                holder.binding.transCashContainer.setVisibility(View.VISIBLE);
                holder.binding.transCash.setText(FormatUtil.number(trans.cash_amount));
                break;
            case TransType.TRANS_TYPE_STOCK_DIVIDEND:
                holder.binding.transStockNameContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockName.setText(info.getStockNameWithId());
                holder.binding.transStockAmountContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockAmount.setText(FormatUtil.number(trans.stock_amount));
                break;
            case TransType.TRANS_TYPE_CASH_REDUCTION:
                holder.binding.transCashContainer.setVisibility(View.VISIBLE);
                holder.binding.transCash.setText(FormatUtil.number(trans.cash_amount));
            case TransType.TRANS_TYPE_STOCK_REDUCTION:
                holder.binding.transStockNameContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockName.setText(info.getStockNameWithId());
                holder.binding.transStockAmountContainer.setVisibility(View.VISIBLE);
                holder.binding.transStockAmount.setText(FormatUtil.number(trans.stock_amount));
                break;
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
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TransHistoryItemBinding binding;
        public Transaction mItem;
        public StockInfo mInfo;

        public ViewHolder(TransHistoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}