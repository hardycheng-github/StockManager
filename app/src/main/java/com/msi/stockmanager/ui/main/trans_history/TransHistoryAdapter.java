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
        holder.binding.title.setTypeface(null, Typeface.BOLD);
        holder.binding.title.setText(FormatUtil.transType(trans.trans_type));
        if(trans.trans_type == TransType.TRANS_TYPE_CASH_IN){
            holder.binding.title.setTextColor(ColorUtil.getProfitEarn());
        } else {
            holder.binding.title.setTextColor(ColorUtil.getProfitLose());
        }
        holder.binding.transDate.setTypeface(null, Typeface.BOLD);
        holder.binding.transDate.setText(DateUtil.toDateString(trans.trans_time));
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