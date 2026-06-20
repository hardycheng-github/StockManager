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
 * MACD 金叉/死叉事件檢測服務
 */
public class MacdSignalService {
    private static final String TAG = MacdSignalService.class.getSimpleName();

    /**
     * 檢查觀察清單中的所有股票，檢測 MACD 金叉/死叉事件
     * 取得 1 年歷史資料計算 MACD，僅在最近 31 天內偵測事件
     */
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

        MaAlertLevel alertLevel = Profile.maAlertLevel;
        if (alertLevel == null) {
            alertLevel = MaAlertLevel.DEFAULT;
        }

        MacdSignalConfig.MacdParams params = MacdSignalConfig.getMacdParams(alertLevel);
        Log.d(TAG, "Checking " + watchingList.size() + " stocks with alert level: "
                + alertLevel + " " + MacdSignalConfig.getMacdLabel(params));

        for (String stockId : watchingList) {
            checkStock(stockId, params, alertLevel);
        }
    }

    private static void checkStock(String stockId, MacdSignalConfig.MacdParams params, MaAlertLevel alertLevel) {
        if (stockId == null || stockId.isEmpty() || params == null) {
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

                QuotaUtil.initMACD(kDataList, params.fastPeriod, params.slowPeriod, params.signalPeriod, true);
                detectCross(kDataList, stockId, stockInfo, params, alertLevel);
            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, "Error getting history data for " + stockId, e);
            }
        });
    }

    private static void detectCross(List<KData> kDataList, String stockId, StockInfo stockInfo,
                                    MacdSignalConfig.MacdParams params, MaAlertLevel alertLevel) {
        int minBars = MacdSignalConfig.getMinWarmupBars(params);
        if (kDataList == null || kDataList.size() < minBars + 1) {
            return;
        }

        long scanCutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(MacdSignalConfig.getEventScanDays());

        for (int i = minBars; i < kDataList.size(); i++) {
            KData current = kDataList.get(i);
            if (current.getTime() < scanCutoff) {
                continue;
            }

            KData previous = kDataList.get(i - 1);

            double prevDif = previous.getDif();
            double prevDea = previous.getDea();
            double currDif = current.getDif();
            double currDea = current.getDea();

            if (prevDif == 0 && prevDea == 0 && currDif == 0 && currDea == 0) {
                continue;
            }

            boolean isGoldenCross = prevDif <= prevDea && currDif > currDea;
            boolean isDeathCross = prevDif >= prevDea && currDif < currDea;

            if (isGoldenCross || isDeathCross) {
                createNotification(stockId, stockInfo, params, alertLevel, isGoldenCross,
                        current.getClosePrice(), current.getTime());
            }
        }
    }

    private static void createNotification(String stockId, StockInfo stockInfo,
                                           MacdSignalConfig.MacdParams params, MaAlertLevel alertLevel,
                                           boolean isGoldenCross, double price, long eventTimestamp) {
        String notifyType = MacdSignalConfig.getNotifyType(params, isGoldenCross);
        String action = MacdSignalConfig.getActionLabel(isGoldenCross);
        String title = String.format("%s %s - %s", stockId, stockInfo.getStockName(), action);
        String body = MacdSignalConfig.getNotifyBody(alertLevel, isGoldenCross, price);

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
