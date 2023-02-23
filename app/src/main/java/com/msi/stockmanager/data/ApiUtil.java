package com.msi.stockmanager.data;

import android.content.Context;

import com.msi.stockmanager.data.analytics.ITaApi;
import com.msi.stockmanager.data.analytics.TaApi;
import com.msi.stockmanager.data.news.INewsApi;
import com.msi.stockmanager.data.news.NewsApi;
import com.msi.stockmanager.data.revenue.IRevenueApi;
import com.msi.stockmanager.data.revenue.RevenueApi;
import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockApi;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;

public class ApiUtil {
    public static IStockApi stockApi;
    public static ITransApi transApi;
    public static INewsApi newsApi;
    public static ITaApi taApi;
    public static IRevenueApi revenueApi;
    private static Context mContext;

    public static void init(Context context){
        if(mContext != null) return;
        mContext = context;
        stockApi = new StockApi(context);
        transApi = new TransApi(context);
        newsApi = new NewsApi(context);
        taApi = new TaApi();
        revenueApi = new RevenueApi(context);
    }
}
