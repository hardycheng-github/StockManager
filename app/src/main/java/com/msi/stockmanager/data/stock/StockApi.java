package com.msi.stockmanager.data.stock;

import android.content.Context;
import android.util.Log;

import com.msi.stockmanager.BuildConfig;
import com.msi.stockmanager.data.DateUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class StockApi implements IStockApi{
    private static final String TAG = StockApi.class.getSimpleName();
    private static final String FINMIND_BASE_URL = "https://api.finmindtrade.com/api/v4/data";
    private static final String DATASET_TAIWAN_STOCK_PRICE = "TaiwanStockPrice";
    private static final int HTTP_CONNECT_TIMEOUT_MS = 6000;
    private static final int HTTP_READ_TIMEOUT_MS = 6000;

    private Context parentsContext;
    public StockApi(Context context) {
        parentsContext = context;
    }

    /** 取得今日日期字串 yyyy-MM-dd（使用裝置預設時區） */
    private static String getTodayDateString() {
        Calendar cal = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    /** 取得今日往前推 N 天的日期字串 yyyy-MM-dd */
    private static String getDateStringDaysAgo(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 依 range 參數計算 FinMind 所需的 date、end_date。
     * 僅支援 1d 搭配 1mo、1y；其他 fallback 為 1mo。
     */
    private static String[] rangeToDateEndDate(String range) {
        String endDate = getTodayDateString();
        String startDate;
        if ("1y".equals(range)) {
            startDate = getDateStringDaysAgo(365);
        } else if ("1mo".equals(range) || "1m".equals(range)) {
            startDate = getDateStringDaysAgo(31);
        } else {
            startDate = getDateStringDaysAgo(31);
        }
        return new String[]{ startDate, endDate };
    }

    /** 對 FinMind API V4 發送 GET 請求，回傳 response body 字串；失敗拋出 Exception */
    private static String finmindHttpGet(String requestUrl, String token) throws Exception {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);
        try {
            int status = connection.getResponseCode();
            if (status != 200) {
                String errMsg = "";
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) sb.append(line).append("\n");
                    errMsg = sb.toString().trim();
                } catch (Exception ignored) {}
                throw new Exception("HTTP error (status=" + status + ", url=" + requestUrl + ", msg=" + errMsg + ")");
            }
            try (InputStream is = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                 BufferedReader in = new BufferedReader(reader)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) sb.append(line).append("\n");
                return sb.toString();
            }
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public void getRegularStockPrice(String stock_id, ResultCallback callback) {
        Thread task = new Thread(()->{
            StockInfo info = StockUtilKt.getStockInfoOrNull(stock_id);
            if (info == null) {
                try { callback.onResult(null); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
            if (System.currentTimeMillis() - info.getLastUpdateTime() < LAST_UPDATE_INTERVAL) {
                try { callback.onResult(info); } catch (Exception ex) { ex.printStackTrace(); }
                return;
            }
            try {
                String token = BuildConfig.FINMIND_API_TOKEN.trim();
                if (token.isEmpty()) {
                    throw new Exception("FINMIND_API_TOKEN is empty");
                }
                String startDate = getDateStringDaysAgo(10);
                String endDate = getTodayDateString();
                String requestUrl = FINMIND_BASE_URL + "?dataset=" + DATASET_TAIWAN_STOCK_PRICE
                        + "&data_id=" + stock_id + "&start_date=" + startDate + "&end_date=" + endDate;
                Log.d(TAG, "request url: " + requestUrl);
                String responseBody = finmindHttpGet(requestUrl, token);
                JSONObject root = new JSONObject(responseBody);
                if (root.optInt("status", 0) != 200) {
                    throw new Exception("FinMind API error: " + root.optString("msg", "unknown"));
                }
                JSONArray data = root.optJSONArray("data");
                if (data == null || data.length() == 0) {
                    throw new Exception("FinMind API returned no data");
                }
                JSONObject latest = data.getJSONObject(data.length() - 1);
                double prevClose = data.length() >= 2
                        ? data.getJSONObject(data.length() - 2).optDouble("close", 0)
                        : latest.optDouble("close", 0);
                double close = latest.optDouble("close", 0);
                double spread = latest.optDouble("spread", 0);
                double changePercent = (prevClose != 0) ? (spread / prevClose) : 0;

                info.setLastPrice(close);
                info.setLastOpen(latest.optDouble("open", 0));
                info.setLastHigh(latest.optDouble("max", 0));
                info.setLastLow(latest.optDouble("min", 0));
                info.setLastVolume(latest.optDouble("Trading_Volume", 0));
                info.setPreviosClose(prevClose);
                info.setLastChange(spread);
                info.setLastChangePercent(changePercent);
                String dateStr = latest.optString("date", "");
                info.setLastUpdateTime(dateStr.isEmpty() ? System.currentTimeMillis() : DateUtil.parseDate(dateStr));

                try { callback.onResult(info); } catch (Exception ex) { ex.printStackTrace(); }
            } catch (Exception e) {
                Log.e(TAG, "getRegularStockPrice err: " + e.getMessage());
                e.printStackTrace();
                try { callback.onResult(null); } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        task.setName("getRegularStockPrice");
        task.start();
    }

    @Override
    public void getHistoryStockData(String stock_id, String interval, String range, HistoryCallback callback) {
        Thread task = new Thread(()->{
            try {
                String token = BuildConfig.FINMIND_API_TOKEN.trim();
                if (token.isEmpty()) {
                    throw new Exception("FINMIND_API_TOKEN is empty");
                }
                String[] dateRange = rangeToDateEndDate(range);
                String startDate = dateRange[0];
                String endDate = dateRange[1];
                String requestUrl = FINMIND_BASE_URL + "?dataset=" + DATASET_TAIWAN_STOCK_PRICE
                        + "&data_id=" + stock_id + "&start_date=" + startDate + "&end_date=" + endDate;
                String responseBody = finmindHttpGet(requestUrl, token);
                JSONObject root = new JSONObject(responseBody);
                if (root.optInt("status", 0) != 200) {
                    throw new Exception("FinMind API error: " + root.optString("msg", "unknown"));
                }
                JSONArray dataArray = root.optJSONArray("data");
                List<StockHistory> data = new ArrayList<>();
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject row = dataArray.getJSONObject(i);
                        StockHistory history = new StockHistory();
                        history.stock_id = stock_id;
                        String dateStr = row.optString("date", "");
                        history.date_timestamp = dateStr.isEmpty() ? 0 : DateUtil.parseDate(dateStr);
                        history.price_open = row.optDouble("open", 0);
                        history.price_close = row.optDouble("close", 0);
                        history.price_high = row.optDouble("max", 0);
                        history.price_low = row.optDouble("min", 0);
                        history.price_volume = row.optDouble("Trading_Volume", 0);
                        data.add(history);
                    }
                }
                data.sort((a, b) -> Long.compare(a.date_timestamp, b.date_timestamp));
                callback.onResult(data);
            } catch (Exception e) {
                Log.e(TAG, "getHistoryStockData err: " + e.getMessage());
                callback.onException(e);
            }
        });
        task.setName("getHistoryStockData");
        task.start();
    }
}
