package com.msi.stockmanager.data.stock;

public interface IStockApi {

    void getRegularStockPrice(String stock_id, ResultCallback callback);

    interface ResultCallback {
        void onResult(StockInfo info);
        void onException(Exception e);
    }
}
