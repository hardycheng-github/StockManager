package com.msi.stockmanager.data.stock;

public interface IStockApi {
    int LAST_UPDATE_INTERVAL = 3600000; //1hr

    void getRegularStockPrice(String stock_id, ResultCallback callback);

    interface ResultCallback {
        void onResult(StockInfo info);
    }
}
