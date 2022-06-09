package com.msi.stockmanager.data.stock;

import android.content.Context;

public class StockApi implements IStockApi{

    public StockApi(Context context){

    }

    //TODO 實作網路爬蟲取得真實股價
    @Override
    public void getRegularStockPrice(String stock_id, ResultCallback callback) {
        Thread task = new Thread(()->{
            //網路相關操作屬於耗時操作，需要透過非主線Thread執行
            StockInfo info = StockUtilKt.getStockInfoOrNull(stock_id);
            if(info != null){
                //如果stock id是合法的，會進入這個區塊
                double price = Math.random() * 100;
                // TODO 將正確的價格設定到price
                // TODO 將正確的價格設定到price
                // TESTING
                // TODO 將正確的價格設定到price
                // TODO 將正確的價格設定到price
                info.setLastPrice(price);
                info.setLastUpdateTime(System.currentTimeMillis());
            }
            callback.onResult(info);
        });
        task.setName("getRegularStockPrice");
        task.start();
    }
}
