package com.msi.stockmanager.ui.main.analysis;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityAnalysisItemBinding;
import com.msi.stockmanager.ui.main.pager.PagerActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class AnalysisAdapter extends RecyclerView.Adapter<AnalysisAdapter.ViewHolder> {
    private static final String TAG = AnalysisAdapter.class.getSimpleName();

    public final List<StockInfo> mItems = new ArrayList<>();

    public AnalysisAdapter(){
        try {
            mItems.clear();
            for(String stockId: ApiUtil.transApi.getHoldingStockList()){
                StockInfo info = StockUtilKt.getStockInfoOrNull(stockId);
                if(info != null){
                    mItems.add(info);
                }
            }
        } catch (Exception e){
            Log.e(TAG, "reloadList err: " + e.getMessage());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ActivityAnalysisItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Context context = holder.binding.getRoot().getContext();
        Activity activity = (Activity) context;
        StockInfo item = mItems.get(position);
        holder.mValue = item;
        holder.binding.cardView.setOnClickListener(v->onStockSelected(item));
        holder.binding.stockName.setText(item.getStockNameWithId());
        holder.binding.stockPrice.setTextColor(ColorUtil.getProfitNone());
        holder.binding.stockPrice.setText(R.string.syncing);
        holder.binding.stockProfit.setTextColor(ColorUtil.getProfitNone());
        holder.binding.stockProfit.setText(R.string.syncing);
        onRegularPriceUpdated(activity, holder, item);
        if(System.currentTimeMillis() - item.getLastUpdateTime() < 60*60*1000) {
            onRegularPriceUpdated(activity, holder, item);
        } else {
            ApiUtil.stockApi.getRegularStockPrice(holder.mValue.getStockId(), info -> {
                onRegularPriceUpdated(activity, holder, info);
            });
        }
    }

    private void onRegularPriceUpdated(Activity activity, ViewHolder holder, StockInfo info){
        if(info != null) {
            activity.runOnUiThread(()->{
                if(info.getLastChange() > 0){
                    holder.binding.stockPrice.setTextColor(ColorUtil.getProfitEarn());
                    holder.binding.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                    holder.binding.stockProfit.setTextColor(ColorUtil.getProfitEarn());
                    holder.binding.stockProfit.setText(String.format("▲ %s (%s)"
                            , FormatUtil.number(Math.abs(info.getLastChange()))
                            , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
                } else if(info.getLastChange() < 0){
                    holder.binding.stockPrice.setTextColor(ColorUtil.getProfitLose());
                    holder.binding.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                    holder.binding.stockProfit.setTextColor(ColorUtil.getProfitLose());
                    holder.binding.stockProfit.setText(String.format("▼ %s (%s)"
                            , FormatUtil.number(Math.abs(info.getLastChange()))
                            , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
                } else {
                    holder.binding.stockPrice.setTextColor(ColorUtil.getProfitNone());
                    holder.binding.stockPrice.setText(FormatUtil.number(info.getLastPrice()));
                    holder.binding.stockProfit.setTextColor(ColorUtil.getProfitNone());
                    holder.binding.stockProfit.setText(String.format("%s (%s)"
                            , FormatUtil.number(Math.abs(info.getLastChange()))
                            , FormatUtil.percent(Math.abs(info.getLastChangePercent()))));
                }
                holder.binding.open.setText(FormatUtil.number(info.getLastOpen()));
                holder.binding.high.setText(FormatUtil.number(info.getLastHigh()));
                holder.binding.low.setText(FormatUtil.number(info.getLastLow()));
                holder.binding.volume.setText(FormatUtil.number(info.getLastVolume()));
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ActivityAnalysisItemBinding binding;
        public StockInfo mValue;

        public ViewHolder(ActivityAnalysisItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    abstract public void onStockSelected(StockInfo target);
}