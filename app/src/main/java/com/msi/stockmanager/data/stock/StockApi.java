package com.msi.stockmanager.data.stock;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class StockApi implements IStockApi{
    private Context parentsContext;
    public StockApi(Context context) {
        parentsContext = context;
    }

    @Override
    public void getRegularStockPrice(String stock_id, ResultCallback callback) {
        Thread task = new Thread(()->{
            //網路相關操作屬於耗時操作，需要透過非主線Thread執行
            StockInfo info = StockUtilKt.getStockInfoOrNull(stock_id);
            if (info == null) {
//                callback.onException(new Exception("Invalid stock ID (" + stock_id + ")"));
                try {
                    callback.onResult(null);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                return;
            }
            //如果interval時間內取得過，直接返回值
            if(System.currentTimeMillis() - info.getLastUpdateTime() < LAST_UPDATE_INTERVAL){
                try {
                    callback.onResult(info);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                return;
            }
            //如果stock id是合法的，會進入這個區塊
            //double price = 0;
            //info.setStockId(stock_id);
            //info.setLastPrice(0.0);
            //info.setLastUpdateTime(System.currentTimeMillis());
            HttpURLConnection connection = null;
            try {
                // 取得股票資訊
                String httRequestUrl = String.format("https://query1.finance.yahoo.com/v6/finance/quote?symbols=" + stock_id + ".TW");
                URL url = new URL(httRequestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                InputStream inputStream = connection.getInputStream();
                int status = connection.getResponseCode();
                if ((status != 200) || (inputStream == null)) {
                    throw new Exception("HTTP error fetching URL (status=" + String.valueOf(status)
                            + ", URL=" + httRequestUrl + ")");
                } else {
                    // 讀取 JSON RESPONSE --> sb
                    InputStreamReader reader = new InputStreamReader(inputStream,"UTF-8");
                    BufferedReader in = new BufferedReader(reader);
                    StringBuilder sb = new StringBuilder();
                    String line="";
                    while ((line = in.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    in.close();
                    //Log.d("HttpRequest", sb.toString());
                    // 直接取得 JSON RESPONSE ARRAY
                    JSONObject object = (new JSONObject(sb.toString())).getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(0);
                    Log.d("HttpRequest", "object=" + object.toString());
                    // 取得股價
                    info.setLastPrice(object.getDouble("regularMarketPrice"));
                    // 取得股價更新時間
                    info.setLastUpdateTime(object.getLong("regularMarketTime")*1000);
                    try {
                        callback.onResult(info);
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e("HttpRequest", e.getMessage());
                e.printStackTrace();
//                callback.onException(e);
                try {
                    callback.onResult(null);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                return;
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ex){}
                }
            }
        });
        task.setName("getRegularStockPrice");
        task.start();
    }
}
