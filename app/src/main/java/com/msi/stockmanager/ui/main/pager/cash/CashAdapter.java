package com.msi.stockmanager.ui.main.pager.cash;

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
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.Constants;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.databinding.FragmentCashItemBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;

import java.util.ArrayList;
import java.util.List;

public class CashAdapter extends RecyclerView.Adapter<CashAdapter.ViewHolder> {

    public final List<Transaction> mItems = new ArrayList<>();
    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(FragmentCashItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    public void reloadList(){
        mItems.clear();
        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
            switch (trans.trans_type){
                case TransType.TRANS_TYPE_CASH_IN:
                case TransType.TRANS_TYPE_CASH_OUT:
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
        holder.binding.title.setTypeface(null, Typeface.BOLD);
        holder.binding.title.setText(FormatUtil.transType(trans.trans_type));
        if(trans.trans_type == TransType.TRANS_TYPE_CASH_IN){
            holder.binding.title.setTextColor(ColorUtil.getProfitEarn());
        } else {
            holder.binding.title.setTextColor(ColorUtil.getProfitLose());
        }
        holder.binding.transDate.setTypeface(null, Typeface.BOLD);
        holder.binding.transDate.setText(DateUtil.toDateString(trans.trans_time));
        holder.binding.cashAmount.setTypeface(null, Typeface.BOLD);
        holder.binding.cashAmount.setText(FormatUtil.number(Math.abs(trans.cash_amount)));
        holder.binding.cardView.setOnClickListener(v -> onEdit(trans));
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
                            onEdit(trans);
                            return true;
                        case R.id.item2:
                            onRemove(trans);
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

    private void onEdit(Transaction trans){
        Intent intent = new Intent(mContext, FormActivity.class);
        intent.putExtra(Constants.EXTRA_TRANS_OBJECT, trans);
        mContext.startActivity(intent);
    }

    private void onRemove(Transaction trans){
        ApiUtil.transApi.removeTrans(trans.trans_id);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentCashItemBinding binding;
        public Transaction mItem;
        public StockInfo mInfo;

        public ViewHolder(FragmentCashItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}