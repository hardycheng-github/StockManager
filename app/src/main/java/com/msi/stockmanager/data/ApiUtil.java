package com.msi.stockmanager.data;

import android.content.Context;

import com.msi.stockmanager.data.news.INewsApi;
import com.msi.stockmanager.data.news.NewsApi;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockApi;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;

public class ApiUtil {
    public static IStockApi stockApi;
    public static ITransApi transApi;
    public static INewsApi newsApi;

    public static void init(Context context){
        stockApi = new StockApi(context);
        transApi = new TransApi(context);
        newsApi = new NewsApi(context);
    }
}
