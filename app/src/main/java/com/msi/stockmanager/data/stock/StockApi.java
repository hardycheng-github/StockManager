package com.msi.stockmanager.data.stock;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class StockApi implements IStockApi{
    private static final String TAG = StockApi.class.getSimpleName();
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

    /* 嘗試獲取 JSONArray 目標，失敗傳回 null */
    private JSONArray getJSONArrayTarget(JSONObject parent, String name) throws JSONException {
        if ((parent != null) && (!parent.isNull(name))) {
            return parent.getJSONArray(name);
        }
        return null;
    }

    /* URL: https://query1.finance.yahoo.com/v8/finance/chart/2330.TW?interval=1d&range=1mo
     * JSON 預設格式 (adjclose 並不一定會有)
     *
     * {
     *   "chart": {
     *     "result": [
     *     {
     *       "meta":{ ... },
     *       "timestamp":[ ... ],
     *       "indicators":
     *       {
     *         "quote": [
     *           {
     *             "high": [ ... ],
     *             "opne": [ ... ],
     *             "low":  [ ... ],
     *             "close": [ ... ],
     *             "volum": [ ... ],
     *           }
     *         ],
     *         "adjclose":[
     *           {
     *             "adjclose": [ ... ]
     *           }
     *         ]
     *       }
     *     }],
     *     "error":null
     *   }
     * }
     *
     */
    @Override
    public void getHistoryStockData(String stock_id, String interval, String range, HistoryCallback callback) {
        Thread task = new Thread(()->{
            HttpURLConnection connection = null;

            try {
                List<StockHistory> data = new ArrayList<>();
                // TODO implement
                // https://query1.finance.yahoo.com/v8/finance/chart/2330.TW?interval=1d&range=1mo
                String httRequestUrl = String.format("https://query1.finance.yahoo.com/v8/finance/chart/"+stock_id+".TW?interval="+interval+"&range="+range);
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
                    // JSON: 嘗試取得 result
                    if ((new JSONObject(sb.toString())).getJSONObject("chart").isNull("result")) {
                        /* JSON 解析到 result 為 null，則開始嘗試解析 error 。
                         * PS: 通常 Yahoo Finance API 錯誤在這之前就會拋出 Exception，並不太可能執行到這裡。
                         */
                        if ((new JSONObject(sb.toString())).getJSONObject("chart").isNull("error")) {
                            /* JSON: 無法解析 error */
                            throw new Exception("Yahoo Finance API ERROR (error=UNKNOWN, URL=" + httRequestUrl + ")");
                        }
                        JSONObject error = (new JSONObject(sb.toString())).getJSONObject("chart").getJSONObject("error");
                        Log.d("HttpRequest", "error=" + error.toString());
                        throw new Exception("Yahoo Finance API ERROR (error=" + error.toString() + ", URL=" + httRequestUrl + ")");
                    }
                    // JSON: 取得 result, indicators, quote
                    JSONObject result = (new JSONObject(sb.toString())).getJSONObject("chart").getJSONArray("result").getJSONObject(0);
                    JSONObject indicators = result.getJSONObject("indicators");
                    JSONObject quote = indicators.getJSONArray("quote").getJSONObject(0);
                    // JSON: 取得 timestamp
                    JSONArray timestamp = getJSONArrayTarget(result, "timestamp");
                    Log.d("HttpRequest", "timestamp=" + timestamp.toString());
                    // JSON: 取得 open
                    JSONArray open = getJSONArrayTarget(quote, "open");
                    Log.d("HttpRequest", "open=" + open.toString());
                    // JSON: 取得 close
                    JSONArray close = getJSONArrayTarget(quote, "close");
                    Log.d("HttpRequest", "close=" + close.toString());
                    // JSON: 取得 high
                    JSONArray high = getJSONArrayTarget(quote, "high");
                    Log.d("HttpRequest", "high=" + high.toString());
                    // JSON: 取得 low
                    JSONArray low = getJSONArrayTarget(quote, "low");
                    Log.d("HttpRequest", "low=" + low.toString());
                    // JSON: 取得 volume
                    JSONArray volume = getJSONArrayTarget(quote, "volume");
                    Log.d("HttpRequest", "volume=" + volume.toString());
                    // JSON: 嘗試取得 adj_close (調整後的收盤價)
                    //JSONArray adj_close = getJSONArrayTarget(indicators.getJSONArray("adjclose").getJSONObject(0), "adjclose");
                    //Log.d("HttpRequest", "adjclose=" + adj_close.toString());
                    if (timestamp.length() > 0) {
                        for (int i=0; i<timestamp.length(); i++) {
                            StockHistory history   = new StockHistory();
                            history.stock_id       = stock_id;
                            history.date_timestamp = ((timestamp == null)? 0 : timestamp.getLong(i)*1000);
                            history.price_open     = ((open == null)? 0 : open.getDouble(i));
                            history.price_close    = ((close == null)? 0 : close.getDouble(i));
                            history.price_high     = ((high == null)? 0 : high.getDouble(i));
                            history.price_low      = ((low == null)? 0 : low.getDouble(i));
                            history.price_volume   = ((volume == null)? 0 : volume.getDouble(i));
                            //history.price_adjclose = ((adj_close == null)? 0 : adj_close.getDouble(i));
                            data.add(history);
                        }
                    }
                }
                // data.sort((i1, i2) -> (int)(i1.date_timestamp - i2.date_timestamp));
                callback.onResult(data);
            } catch (Exception e){
                Log.e(TAG, "getHistoryStockData err: " + e.getMessage());
                callback.onException(e);
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ex){}
                }
            }
        });
        task.setName("getHistoryStockData");
        task.start();
    }
}
