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
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 平均線突破檢測服務
 */
public class MaBreakthroughService {
    private static final String TAG = MaBreakthroughService.class.getSimpleName();
    
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
                
                // 檢測每個平均線的突破/跌破事件
                for (Integer maDays : maDaysList) {
                    detectBreakthrough(kDataList, stockId, stockInfo, maDays);
                }
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
     * 檢測指定平均線的突破/跌破事件
     * @param kDataList K線數據列表（已按時間排序）
     * @param stockId 股票代碼
     * @param stockInfo 股票信息
     * @param maDays 平均線天數
     */
    private static void detectBreakthrough(List<KData> kDataList, String stockId, StockInfo stockInfo, int maDays) {
        if (kDataList == null || kDataList.size() < maDays + 1) {
            // 數據不足，無法檢測
            return;
        }
        
        // 從第 maDays 天開始檢測（因為前 maDays-1 天沒有 MA 值）
        // 需要比較前一日和當日，所以從 maDays 開始（索引為 maDays）
        for (int i = maDays; i < kDataList.size(); i++) {
            KData current = kDataList.get(i);
            KData previous = kDataList.get(i - 1);
            
            // 獲取當日和前一日對應的 MA 值
            double currentMa = MaBreakthroughConfig.getMaValue(current, maDays);
            double previousMa = MaBreakthroughConfig.getMaValue(previous, maDays);
            
            // 如果 MA 值無效，跳過
            if (currentMa <= 0 || previousMa <= 0) {
                continue;
            }
            
            double currentPrice = current.getClosePrice();
            double previousPrice = previous.getClosePrice();
            
            // 檢測突破：前一日收盤價 < 前一日MA，且當日收盤價 >= 當日MA
            boolean isBreakthrough = previousPrice < previousMa && currentPrice >= currentMa;
            
            // 檢測跌破：前一日收盤價 > 前一日MA，且當日收盤價 <= 當日MA
            boolean isBreakdown = previousPrice > previousMa && currentPrice <= currentMa;
            
            if (isBreakthrough || isBreakdown) {
                // 創建通知
                createNotification(stockId, stockInfo, maDays, isBreakthrough, currentPrice, currentMa, current.getTime());
            }
        }
    }
    
    /**
     * 創建並插入通知
     * @param stockId 股票代碼
     * @param stockInfo 股票信息
     * @param maDays 平均線天數
     * @param isBreakthrough true 為突破，false 為跌破
     * @param price 收盤價
     * @param maValue 平均線值
     * @param eventTimestamp 事件發生日時間戳
     */
    private static void createNotification(String stockId, StockInfo stockInfo, int maDays, 
                                          boolean isBreakthrough, double price, double maValue, long eventTimestamp) {
        // 檢查是否已存在相同事件
        String notifyType = MaBreakthroughConfig.getNotifyType(maDays, isBreakthrough);
        
        // 使用 RxJava 檢查是否已存在
        ApiUtil.notifyRepository.findByTypeAndPayloadAndDate(notifyType, stockId, eventTimestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        existing -> {
                            Log.d(TAG, "Notification already exists for " + stockId + " on " + eventTimestamp);
                        },
                        error -> {
                            Log.e(TAG, "Error checking existing notification", error);
                            // 即使檢查失敗，也嘗試創建通知（避免重複檢查導致遺漏）
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
                                            error2 -> Log.e(TAG, "Error creating notification", error2)
                                    );
                        },
                        () -> {
                            // 不存在，創建新通知
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
                                    eventTimestamp, // 使用事件發生日作為時間戳
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
                );
    }
}
