package com.msi.stockmanager.data.notify;

import android.content.Context;
import android.util.Log;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.FinMindApiDisabledException;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.kline.KData;
import com.msi.stockmanager.kline.QuotaUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 平均線突破檢測服務
 */
public class MaBreakthroughService {
    private static final String TAG = MaBreakthroughService.class.getSimpleName();

    private static final class MaEventCandidate {
        final long eventTimestamp;
        final boolean isBreakthrough;
        final int maDays;
        final double price;
        final double maValue;

        MaEventCandidate(long eventTimestamp, boolean isBreakthrough, int maDays,
                         double price, double maValue) {
            this.eventTimestamp = eventTimestamp;
            this.isBreakthrough = isBreakthrough;
            this.maDays = maDays;
            this.price = price;
            this.maValue = maValue;
        }
    }
    
    /**
     * 檢查觀察清單中的所有股票，檢測平均線突破/跌破事件
     * 會獲取過去一個月的歷史數據（約30天）來檢查歷史事件
     * @param context Context
     */
    public static void checkWatchingList(Context context) {
        if (context == null || ApiUtil.revenueApi == null || ApiUtil.stockApi == null) {
            Log.e(TAG, "Context or API is null");
            return;
        }
        
        // 獲取觀察清單
        List<String> watchingList = ApiUtil.revenueApi.getWatchingList();
        if (watchingList == null || watchingList.isEmpty()) {
            Log.d(TAG, "Watching list is empty");
            return;
        }
        
        // 獲取當前關注等級
        MaAlertLevel alertLevel = Profile.maAlertLevel;
        if (alertLevel == null) {
            alertLevel = MaAlertLevel.DEFAULT;
        }
        
        // 獲取要檢測的平均線天數列表
        List<Integer> maDaysList = MaBreakthroughConfig.getMaDays(alertLevel);
        Log.d(TAG, "Checking " + watchingList.size() + " stocks with alert level: " + alertLevel);
        
        // 對於每個股票進行檢測
        for (String stockId : watchingList) {
            checkStock(stockId, maDaysList);
        }
    }
    
    /**
     * 檢查單個股票的平均線突破/跌破事件
     * @param stockId 股票代碼
     * @param maDaysList 要檢測的平均線天數列表
     */
    private static void checkStock(String stockId, List<Integer> maDaysList) {
        if (stockId == null || stockId.isEmpty() || maDaysList == null || maDaysList.isEmpty()) {
            return;
        }
        
        // 獲取股票信息
        StockInfo stockInfo = StockUtilKt.getStockInfoOrNull(stockId);
        if (stockInfo == null) {
            Log.w(TAG, "Stock info not found for: " + stockId);
            return;
        }
        
        // 獲取過去30天的歷史數據
        ApiUtil.stockApi.getHistoryStockData(stockId, "1d", "1mo", new IStockApi.HistoryCallback() {
            @Override
            public void onResult(List<StockHistory> data) {
                if (data == null || data.isEmpty()) {
                    Log.w(TAG, "No history data for: " + stockId);
                    return;
                }
                
                // 確保數據按時間排序（升序）
                data.sort((a, b) -> Long.compare(a.date_timestamp, b.date_timestamp));
                
                // 轉換為 KData
                List<KData> kDataList = new ArrayList<>();
                for (StockHistory history : data) {
                    KData kData = new KData(
                            history.date_timestamp,
                            history.price_open,
                            history.price_close,
                            history.price_high,
                            history.price_low,
                            history.price_volume
                    );
                    kDataList.add(kData);
                }
                
                // 計算平均線
                QuotaUtil.initMa(kDataList, true);
                
                List<MaEventCandidate> candidates = new ArrayList<>();
                for (Integer maDays : maDaysList) {
                    collectBreakthroughEvents(kDataList, maDays, candidates);
                }
                mergeAndNotify(stockId, stockInfo, candidates);
            }
            
            @Override
            public void onException(Exception e) {
                if (e instanceof FinMindApiDisabledException) {
                    Log.i(TAG, "History data skipped (FinMind API disabled): " + stockId);
                } else {
                    Log.e(TAG, "Error getting history data for " + stockId, e);
                }
            }
        });
    }
    
    /**
     * 收集指定平均線的突破/跌破候選事件（不寫入通知）
     */
    private static void collectBreakthroughEvents(List<KData> kDataList, int maDays,
                                                List<MaEventCandidate> candidates) {
        if (kDataList == null || kDataList.size() < maDays + 1 || candidates == null) {
            return;
        }
        
        for (int i = maDays; i < kDataList.size(); i++) {
            KData current = kDataList.get(i);
            KData previous = kDataList.get(i - 1);
            
            double currentMa = MaBreakthroughConfig.getMaValue(current, maDays);
            double previousMa = MaBreakthroughConfig.getMaValue(previous, maDays);
            
            if (currentMa <= 0 || previousMa <= 0) {
                continue;
            }
            
            double currentPrice = current.getClosePrice();
            double previousPrice = previous.getClosePrice();
            
            boolean isBreakthrough = previousPrice < previousMa && currentPrice >= currentMa;
            boolean isBreakdown = previousPrice > previousMa && currentPrice <= currentMa;
            
            if (isBreakthrough || isBreakdown) {
                candidates.add(new MaEventCandidate(
                        current.getTime(),
                        isBreakthrough,
                        maDays,
                        currentPrice,
                        currentMa
                ));
            }
        }
    }

    /**
     * 同日同向多均線觸發時合併，僅保留週期最長者（30 > 10 > 5）並建立通知
     */
    private static void mergeAndNotify(String stockId, StockInfo stockInfo,
                                     List<MaEventCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        Map<String, MaEventCandidate> best = new HashMap<>();
        for (MaEventCandidate candidate : candidates) {
            String key = candidate.eventTimestamp + "_" + candidate.isBreakthrough;
            MaEventCandidate existing = best.get(key);
            if (existing == null
                    || MaBreakthroughConfig.isHigherMaPriority(candidate.maDays, existing.maDays)) {
                best.put(key, candidate);
            }
        }

        for (MaEventCandidate event : best.values()) {
            createNotification(
                    stockId,
                    stockInfo,
                    event.maDays,
                    event.isBreakthrough,
                    event.price,
                    event.maValue,
                    event.eventTimestamp
            );
        }
    }
    
    /**
     * 創建並插入通知
     */
    private static void createNotification(String stockId, StockInfo stockInfo, int maDays, 
                                          boolean isBreakthrough, double price, double maValue, long eventTimestamp) {
        String notifyType = MaBreakthroughConfig.getNotifyType(maDays, isBreakthrough);
        
        ApiUtil.notifyRepository.findByTypeAndPayloadAndDate(notifyType, stockId, eventTimestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        existing -> {
                            Log.d(TAG, "Notification already exists for " + stockId + " on " + eventTimestamp);
                        },
                        error -> {
                            Log.e(TAG, "Error checking existing notification", error);
                            insertNotification(stockId, stockInfo, maDays, isBreakthrough, price, maValue, eventTimestamp, notifyType);
                        },
                        () -> insertNotification(stockId, stockInfo, maDays, isBreakthrough, price, maValue, eventTimestamp, notifyType)
                );
    }

    private static void insertNotification(String stockId, StockInfo stockInfo, int maDays,
                                           boolean isBreakthrough, double price, double maValue,
                                           long eventTimestamp, String notifyType) {
        String maName = MaBreakthroughConfig.getMaName(maDays);
        String action = isBreakthrough ? "突破" : "跌破";
        String title = String.format("%s %s - %s%s",
                stockId, stockInfo.getStockName(), action, maName);
        String body = String.format("收盤價：%.2f，%s：%.2f", price, maName, maValue);

        NotifyEntity notify = new NotifyEntity(
                0,
                notifyType,
                title,
                body,
                eventTimestamp,
                false,
                false,
                "OPEN_STOCK",
                stockId
        );

        ApiUtil.notifyRepository.add(notify)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        id -> Log.d(TAG, "Notification created: " + title),
                        insertError -> Log.e(TAG, "Error creating notification", insertError)
                );
    }
}
