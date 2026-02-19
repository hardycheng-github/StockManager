package com.msi.stockmanager.data.news;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewsApi implements INewsApi {
    private static final String TAG = NewsApi.class.getSimpleName();
    private static final String API_BASE = "https://api.cnyes.com/media/api/v1/newslist/category/";
    private static final int API_LIMIT = 30;

    private Context parentsContext;
    private Map<Integer, List<NewsItem>> newsRecord;

    //0:全部, 1:股票, 2:股市公告, 3:外匯, 4:加密貨幣
    //{ source, type, url }
    private static final String[][] SOURCE_INFO = {
            {"CNYES","1","https://news.cnyes.com/news/cat/tw_quo"},                //鉅亨 - 台股
            {"CNYES","2","https://news.cnyes.com/news/cat/announcement"},          //鉅亨 - 公告
            {"CNYES","3","https://news.cnyes.com/news/cat/forex"},                 //鉅亨 - 外匯
            {"CNYES","4","https://news.cnyes.com/news/cat/bc_crypto"},             //鉅亨 - 虛擬貨幣
    };

    public NewsApi() {} // for test
    public NewsApi(Context context) {
        parentsContext = context;
        newsRecord = new HashMap<Integer, List<NewsItem>>();
    }

    @Override
    public void getNewsList(int type, boolean force, ResultCallback callback) {
        Thread task = new Thread(()->{
            try {
                List<NewsItem> newsItemList;
                if(!force){
                    if (newsRecord.get(type) != null){
                        Log.d(TAG, "NewsApi Info : Return already exist news data!");
                        newsItemList = newsRecord.get(type);
                    }else{
                        Log.d(TAG, "NewsApi Info : News data not found, crawler new data!");
                        newsItemList = NewsCrawler(type);
                        newsRecord.put(type, newsItemList);
                    }
                }else{
                    Log.d(TAG, "NewsApi Info : Force refresh news data, crawler new data!");
                    newsItemList = NewsCrawler(type);
                    newsRecord.put(type, newsItemList);
                }

                newsItemList.sort((a, b) -> (int)(b.timestamp - a.timestamp));

                ((Activity)parentsContext).runOnUiThread(()->callback.onResult(newsItemList));
            } catch (Exception e){
                Log.e(TAG, "getNewsList err: " + e.getMessage());
                ((Activity)parentsContext).runOnUiThread(()->callback.onException(e));
            }
        });
        task.setName("getNewsList");
        task.start();
    }

    /**
     * 透過URL下載圖片Bitmap
     * @param imgUrl 圖片連結
     * @return bitmap or null
     */
    private static Bitmap getImageFromUrl(String imgUrl){
        try {
            InputStream in = new URL(imgUrl).openStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * 取得各分類新聞（僅 CNYES API）
     * @param type 新聞種類
     * @return List<NewsItem>
     */
    private static List<NewsItem> NewsCrawler(int type){

        List<NewsItem> newsItemList = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(SOURCE_INFO.length);
        Log.d(TAG, "NewsApi Info : Thread sum = " + SOURCE_INFO.length);
        for(int x=0; x<SOURCE_INFO.length; x++) {
            int finalX = x;
            new Thread(() -> {
                if (Integer.parseInt(SOURCE_INFO[finalX][1]) == type || 0 == type){
                    List<NewsItem> itemList = CrawlerWithCNYES(SOURCE_INFO[finalX][2]);

                    for (NewsItem item : itemList) {
                        item.type = Integer.parseInt(SOURCE_INFO[finalX][1]);
                        synchronized(newsItemList) {
                            newsItemList.add(item);
                        }
                    }
                }
                Log.d(TAG, "NewsApi Info : Thread finished index : " + finalX);
                countDownLatch.countDown();
            }).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return newsItemList;
    }

    /**
     * 透過鉅亨 API 取得新聞列表
     * @param pageUrl 原網頁 URL，用於從 path 取出 category（例如 https://news.cnyes.com/news/cat/tw_quo）
     * @return List<NewsItem>
     */
    private static List<NewsItem> CrawlerWithCNYES(String pageUrl){
        List<NewsItem> newsItemList = new ArrayList<>();

        String category = pageUrl.substring(pageUrl.lastIndexOf('/') + 1).trim();
        String apiUrl = API_BASE + category + "?limit=" + API_LIMIT;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int code = conn.getResponseCode();
            if (code != 200) {
                Log.w(TAG, "CNYES API non-200: " + code + " " + apiUrl);
                return newsItemList;
            }

            String response = readStream(conn.getInputStream());
            JSONObject root = new JSONObject(response);
            JSONObject items = root.optJSONObject("items");
            if (items == null) {
                return newsItemList;
            }
            JSONArray data = items.optJSONArray("data");
            if (data == null) {
                return newsItemList;
            }

            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.optJSONObject(i);
                if (obj == null) continue;

                NewsItem item = new NewsItem();
                item.source = SOURCE_CNYES;
                item.title = obj.optString("title", "");
                long publishAt = obj.optLong("publishAt", 0L);
                item.timestamp = publishAt * 1000;
                long newsId = obj.optLong("newsId", 0L);
                item.link = "https://news.cnyes.com/news/id/" + newsId;

                JSONObject coverSrc = obj.optJSONObject("coverSrc");
                if (coverSrc != null) {
                    JSONObject s = coverSrc.optJSONObject("s");
                    if (s == null) s = coverSrc.optJSONObject("m");
                    if (s != null) {
                        String src = s.optString("src", "");
                        if (!src.isEmpty()) {
                            item.image = getImageFromUrl(src);
                        }
                    }
                }

                newsItemList.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "CrawlerWithCNYES error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return newsItemList;
    }

    private static String readStream(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            sb.append(new String(buf, 0, n, "UTF-8"));
        }
        in.close();
        return sb.toString();
    }
}
