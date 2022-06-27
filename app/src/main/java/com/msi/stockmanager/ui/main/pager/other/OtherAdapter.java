package com.msi.stockmanager.ui.main.pager.other;

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
import com.msi.stockmanager.databinding.FragmentOtherItemBinding;
import com.msi.stockmanager.ui.main.form.FormActivity;
import com.msi.stockmanager.ui.main.trans_history.TransHistoryActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.ViewHolder> {
    private static final String TAG = OtherAdapter.class.getSimpleName();
    private Context mContext;

    public final List<AccountUtil.StockValue> mItems = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(FragmentOtherItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void reloadList(){
        try {
            mItems.clear();
            Map<String, AccountUtil.StockValue> map = AccountUtil.getAccount().stockValueMap;
            for(AccountUtil.StockValue value: map.values()){
                int total = value.dividendCash + value.dividendStock + value.reductionStock + value.reductionCash;
                if(total > 0){
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
        holder.binding.dividendCash.setTextColor(value.dividendCash == 0
                ? ColorUtil.getProfitNone() : ColorUtil.getProfitEarn());
        holder.binding.dividendCash.setText(FormatUtil.number(value.dividendCash));
        holder.binding.dividendStock.setTextColor(value.dividendStock == 0
                ? ColorUtil.getProfitNone() : ColorUtil.getProfitEarn());
        holder.binding.dividendStock.setText(FormatUtil.number(value.dividendStock));
        holder.binding.reductionCash.setTextColor(value.reductionCash == 0
                ? ColorUtil.getProfitNone() : ColorUtil.getProfitEarn());
        holder.binding.reductionCash.setText(FormatUtil.number(value.reductionCash));
        holder.binding.reductionStock.setTextColor(value.reductionStock == 0
                ? ColorUtil.getProfitNone() : ColorUtil.getProfitLose());
        holder.binding.reductionStock.setText(FormatUtil.number(value.reductionStock));
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
                TransType.TRANS_TYPE_CASH_DIVIDEND,
                TransType.TRANS_TYPE_STOCK_DIVIDEND,
                TransType.TRANS_TYPE_CASH_REDUCTION,
                TransType.TRANS_TYPE_STOCK_REDUCTION,
        });
        mContext.startActivity(intent);
    }

    private void onRemove(StockInfo info){
        Context context = mContext;
        new AlertDialog.Builder(context)
                .setMessage(String.format(context.getString(R.string.dialog_remove_stock_dividend_reduction_msg), info.getStockNameWithId()))
                .setPositiveButton(R.string.confirm, ((dialogInterface, i) -> {
                    for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
                        if(trans.stock_id.equals(info.getStockId())){
                            switch (trans.trans_type){
                                case TransType.TRANS_TYPE_CASH_DIVIDEND:
                                case TransType.TRANS_TYPE_STOCK_DIVIDEND:
                                case TransType.TRANS_TYPE_CASH_REDUCTION:
                                case TransType.TRANS_TYPE_STOCK_REDUCTION:
                                    ApiUtil.transApi.removeTrans(trans.trans_id);
                                    break;
                            }
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
        public final FragmentOtherItemBinding binding;
        public AccountUtil.StockValue mValue;

        public ViewHolder(FragmentOtherItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}