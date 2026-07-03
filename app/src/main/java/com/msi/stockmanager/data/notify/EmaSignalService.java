package com.msi.stockmanager.data.notify;

import android.content.Context;
import android.util.Log;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockHistory;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.kline.KData;
import com.msi.stockmanager.kline.QuotaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * EMA20 突破/跌落事件檢測服務
 */
public class EmaSignalService {
    private static final String TAG = EmaSignalService.class.getSimpleName();

    public static void checkWatchingList(Context context) {
        if (context == null || ApiUtil.revenueApi == null || ApiUtil.stockApi == null) {
            Log.e(TAG, "Context or API is null");
            return;
        }

        List<String> watchingList = ApiUtil.revenueApi.getWatchingList();
        if (watchingList == null || watchingList.isEmpty()) {
            Log.d(TAG, "Watching list is empty");
            return;
        }

        Log.d(TAG, "Checking " + watchingList.size() + " stocks for EMA20 events");

        for (String stockId : watchingList) {
            checkStock(stockId);
        }
    }

    private static void checkStock(String stockId) {
        if (stockId == null || stockId.isEmpty()) {
            return;
        }

        StockInfo stockInfo = StockUtilKt.getStockInfoOrNull(stockId);
        if (stockInfo == null) {
            Log.w(TAG, "Stock info not found for: " + stockId);
            return;
        }

        ApiUtil.stockApi.getHistoryStockData(stockId, "1d", "1y", new IStockApi.HistoryCallback() {
            @Override
            public void onResult(List<StockHistory> data) {
                if (data == null || data.isEmpty()) {
                    Log.w(TAG, "No history data for: " + stockId);
                    return;
                }

                data.sort((a, b) -> Long.compare(a.date_timestamp, b.date_timestamp));

                List<KData> kDataList = new ArrayList<>();
                for (StockHistory history : data) {
                    kDataList.add(new KData(
                            history.date_timestamp,
                            history.price_open,
                            history.price_close,
                            history.price_high,
                            history.price_low,
                            history.price_volume
                    ));
                }

                QuotaUtil.initEma(kDataList, true);
                detectBreakthrough(kDataList, stockId, stockInfo);
            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, "Error getting history data for " + stockId, e);
            }
        });
    }

    private static void detectBreakthrough(List<KData> kDataList, String stockId, StockInfo stockInfo) {
        int minBars = EmaSignalConfig.getMinWarmupBars();
        if (kDataList == null || kDataList.size() < minBars + 1) {
            return;
        }

        long scanCutoff = System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(EmaSignalConfig.getEventScanDays());

        for (int i = minBars; i < kDataList.size(); i++) {
            KData current = kDataList.get(i);
            if (current.getTime() < scanCutoff) {
                continue;
            }

            KData previous = kDataList.get(i - 1);

            double prevEma = EmaSignalConfig.getEmaValue(previous);
            double currEma = EmaSignalConfig.getEmaValue(current);
            if (prevEma <= 0 || currEma <= 0) {
                continue;
            }

            double prevClose = previous.getClosePrice();
            double currClose = current.getClosePrice();

            boolean isBreakthrough = prevClose < prevEma && currClose >= currEma;
            boolean isBreakdown = prevClose > prevEma && currClose <= currEma;

            if (isBreakthrough && !Profile.isEventSubscribed(EventSubscriptionConfig.EVENT_EMA_BREAKTHROUGH)) {
                continue;
            }
            if (isBreakdown && !Profile.isEventSubscribed(EventSubscriptionConfig.EVENT_EMA_BREAKDOWN)) {
                continue;
            }

            if (isBreakthrough || isBreakdown) {
                createNotification(stockId, stockInfo, isBreakthrough,
                        currClose, currEma, current.getTime());
            }
        }
    }

    private static void createNotification(String stockId, StockInfo stockInfo,
                                           boolean isBreakthrough, double price,
                                           double emaValue, long eventTimestamp) {
        String notifyType = EmaSignalConfig.getNotifyType(isBreakthrough);
        String action = EmaSignalConfig.getActionLabel(isBreakthrough);
        String title = String.format("%s %s - %s", stockId, stockInfo.getStockName(), action);
        String body = EmaSignalConfig.getNotifyBody(price, emaValue);

        ApiUtil.notifyRepository.findByTypeAndPayloadAndDate(notifyType, stockId, eventTimestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        existing -> Log.d(TAG, "Notification already exists for " + stockId + " on " + eventTimestamp),
                        error -> {
                            Log.e(TAG, "Error checking existing notification", error);
                            insertNotification(notifyType, title, body, eventTimestamp, stockId);
                        },
                        () -> insertNotification(notifyType, title, body, eventTimestamp, stockId)
                );
    }

    private static void insertNotification(String notifyType, String title, String body,
                                           long eventTimestamp, String stockId) {
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
                        error -> Log.e(TAG, "Error creating notification", error)
                );
    }
}
