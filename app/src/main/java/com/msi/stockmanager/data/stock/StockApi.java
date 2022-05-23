package com.msi.stockmanager.data.stock;

import android.content.Context;

public class StockApi implements IStockApi{

    public StockApi(Context context){

    }

    //TODO 實作網路爬蟲取得真實股價
    @Override
    public StockInfo getRegularStockPrice(String stock_id) {
        StockInfo info = new StockInfo();
        info.stock_id = stock_id;
        info.last_price = FakeDataGenerator.getRandomPrice();
        info.last_update_time = FakeDataGenerator.getRandomDateTime();
        return info;
    }

    //TODO 開始實作API時請將FakeDataGenerator移除
    static class FakeDataGenerator {
        static double getRandomPrice(){
            return Math.random() * 100;
        }

        static long getRandomDateTime(){
            return System.currentTimeMillis() - (long)(Math.random()*30*24*60*60*1000);
        }
    }
}
