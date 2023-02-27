package com.msi.stockmanager.ui.main.analysis;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.AccountUtil;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.analytics.ITaApi;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.databinding.ActivityAnalysisItemBinding;
import com.msi.stockmanager.databinding.ViewTaItemBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;

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
        holder.binding.analytics.setOnClickListener(v->onAnalyticsClick(holder));
        if(System.currentTimeMillis() - item.getLastUpdateTime() < 60*60*1000) {
            onRegularPriceUpdated(activity, holder, item);
        } else {
            ApiUtil.stockApi.getRegularStockPrice(holder.mValue.getStockId(), info -> {
                onRegularPriceUpdated(activity, holder, info);
            });
        }
        initAnalytics(activity, holder, item);

    }

    private void initAnalytics(Activity activity, ViewHolder holder, StockInfo info){
        holder.binding.taTotal.title.setText(R.string.total_score);
        holder.binding.taShort.title.setText(R.string.short_score);
        holder.binding.taLong.title.setText(R.string.long_score);
        holder.binding.taTotal.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        holder.binding.taTotal.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        holder.binding.taLong.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        holder.binding.taLong.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        holder.binding.taShort.indicator.setIndicatorColor(ColorUtil.getProfitEarnSoft());
        holder.binding.taShort.indicator.setTrackColor(ColorUtil.getProfitLoseSoft());
        AccountUtil.StockValue stockValue = AccountUtil.getAccount().stockValueMap.getOrDefault(info.getStockId(), null);
        if(stockValue != null && stockValue.taMap != null && !stockValue.taMap.isEmpty()){
            onTechnologyAnalysisUpdated(activity, holder, stockValue.taMap);
        } else if(stockValue != null && stockValue.dataList != null && !stockValue.dataList.isEmpty()){
            Log.d(TAG, info.getStockId() + " initAnalytics: calc technology analysis");
            ApiUtil.taApi.getAllIndicatorLastScores(stockValue.dataList, new ITaApi.Callback() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {}

                @Override
                public void onSuccess(@NonNull Map<String, ? extends Integer> stringMap) {
                    Map<String, Integer> taMap = new HashMap<String, Integer>();
                    taMap.putAll(stringMap);
                    onTechnologyAnalysisUpdated(activity, holder, taMap);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Log.e(TAG, info.getStockId() + " initAnalytics err: fail to calc technology analysis");
                }
            });
        } else {
            Log.d(TAG, info.getStockId() + " initAnalytics: get history data");
            ApiUtil.stockApi.getHistoryStockData(info.getStockId(), "1d", "1mo", new IStockApi.HistoryCallback() {
                @Override
                public void onResult(List<StockHistory> data) {
                    Log.d(TAG, info.getStockId() + " initAnalytics: calc technology analysis");
                    ApiUtil.taApi.getAllIndicatorLastScores(stockValue.dataList, new ITaApi.Callback() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {}

                        @Override
                        public void onSuccess(@NonNull Map<String, ? extends Integer> stringMap) {
                            Map<String, Integer> taMap = new HashMap<String, Integer>();
                            taMap.putAll(stringMap);
                            onTechnologyAnalysisUpdated(activity, holder, taMap);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e(TAG, info.getStockId() + " initAnalytics err: fail to calc technology analysis");
                        }
                    });
                }

                @Override
                public void onException(Exception e) {
                    Log.e(TAG, info.getStockId() + " initAnalytics err: fail to get history data");
                }
            });
        }
    }

    private void onTechnologyAnalysisUpdated(Activity activity, ViewHolder holder, Map<String, Integer> taMap){
        if(taMap == null) return;
        Log.d(TAG, "onTechnologyAnalysisUpdated: size " + taMap.size());
        int rsi = taMap.getOrDefault(ITaApi.KEY_RSI, ITaApi.SCORE_ERR);
        int ppo = taMap.getOrDefault(ITaApi.KEY_PPO, ITaApi.SCORE_ERR);
        int wr = taMap.getOrDefault(ITaApi.KEY_WILLIAMS_R, ITaApi.SCORE_ERR);
        int totalScore = taMap.getOrDefault(ITaApi.KEY_TOTAL, ITaApi.SCORE_ERR);
        int shortScore = (rsi + wr) / 2;
        int longScore = ppo;
        int totalLevel = Math.min(totalScore / 20, 4);
        int totalBg = ColorUtil.getProfitColor(totalLevel-2);
        activity.runOnUiThread(()->{
            String[] scoreList = activity.getResources().getStringArray(R.array.score_list);
            holder.binding.analyticsResult.setText(scoreList[totalLevel]);
            holder.binding.analyticsResult.setBackgroundColor(totalBg);
            updateTaView(holder.binding.taShort, shortScore);
            updateTaView(holder.binding.taLong, longScore);
            updateTaView(holder.binding.taTotal, totalScore);
        });
    }

    private void updateTaView(ViewTaItemBinding binding, int score){
//        int color = ColorUtil.getProfitScore(score);
        double alpha = (Math.abs(score-50) / 50.0) * 0.6;
        int bgColor = ColorUtil.getColorWithAlpha(ColorUtil.getProfitColorSoft(score-50), alpha);
//        binding.score.setTextColor(color);
        binding.score.setText(score+"");
        binding.indicator.setProgress(score);
        binding.indicator.setBackgroundTintList(ColorStateList.valueOf(bgColor));
    }

    private void onAnalyticsClick(ViewHolder holder){
        boolean isShowing = holder.binding.analyticsBtnOn.getVisibility() == View.VISIBLE;
        if(isShowing){
            holder.binding.analyticsBtnOn.setVisibility(View.INVISIBLE);
            holder.binding.analyticsBtnOff.setVisibility(View.VISIBLE);
            holder.binding.analyticsContainer.setVisibility(View.GONE);
        } else {
            holder.binding.analyticsBtnOn.setVisibility(View.VISIBLE);
            holder.binding.analyticsBtnOff.setVisibility(View.INVISIBLE);
            holder.binding.analyticsContainer.setVisibility(View.VISIBLE);
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