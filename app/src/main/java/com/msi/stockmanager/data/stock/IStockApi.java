package com.msi.stockmanager.data.stock;

import java.util.List;

public interface IStockApi {
    int LAST_UPDATE_INTERVAL = 3600000; //1hr

    /**
     * 取得最新股價
     * @param stock_id 股票代碼
     * @param callback result callback, exception with null
     */
    void getRegularStockPrice(String stock_id, ResultCallback callback);

    interface ResultCallback {
        void onResult(StockInfo info);
    }

    /**
     * 取得歷史股價資料
     * @param stock_id 股票代碼
     * @param interval 每筆資料間格, ex:　"1m","1h","1d","1wk","1mo","1y"
     * @param range 取得範圍, ex: "1m","1h","1d","1wk","1mo","1y","ytd","max"
     * @param callback result callback, exception with null
     */
    void getHistoryStockData(String stock_id, String interval, String range, HistoryCallback callback);

    interface HistoryCallback {
        void onResult(List<StockHistory> data);
        void onException(Exception e);
    }
}
