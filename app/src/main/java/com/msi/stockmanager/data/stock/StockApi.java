package com.msi.stockmanager.data.stock;

import android.content.Context;

public class StockApi implements IStockApi{

    public StockApi(Context context){

    }

    //TODO 實作網路爬蟲取得真實股價
    @Override
    public StockInfo getRegularStockPrice(String stock_id) {
        StockInfo info = new StockInfo();
        info.setStockId(stock_id);
        return info;
    }
}
