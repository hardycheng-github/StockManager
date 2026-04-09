package com.msi.stockmanager.data.news;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;
import java.net.URLEncoder;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewsApi implements INewsApi {
    private static final String TAG = NewsApi.class.getSimpleName();
    private static final String CNYES_API_BASE = "https://api.cnyes.com/media/api/v1/newslist/category/";
    private static final int CNYES_API_LIMIT = 30;
    private static final String MARKETAUX_API_URL = "https://api.marketaux.com/v1/news/all";
    // Free plan currently allows up to 3 articles per request.
    private static final int MARKETAUX_API_LIMIT = 3;
    private static final long MARKETAUX_LOOKBACK_HOURS = 12L;
    private static final String MARKETAUX_ENTITY_TYPES_STOCK = "equity,index,etf,mutualfund";
    private static final String MARKETAUX_ENTITY_TYPES_EXCHANGE = "currency";
    private static final String MARKETAUX_ENTITY_TYPES_CRYPTO = "cryptocurrency";
    private static final String MARKETAUX_COUNTRIES = "us";
    private static final DateTimeFormatter MARKETAUX_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withZone(ZoneOffset.UTC);

    private Context parentsContext;
    private Map<Integer, List<NewsItem>> newsRecord;
    private final Object newsCacheLock = new Object();

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
                List<NewsItem> newsItemList = new ArrayList<>();
                synchronized (newsCacheLock) {
                    ensureNewsCache(type, force);
                    List<NewsItem> cachedList = newsRecord.get(type);
                    if (cachedList != null) {
                        newsItemList.addAll(cachedList);
                    }
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

    private void ensureNewsCache(int type, boolean force) {
        if (force || newsRecord.get(TYPE_ALL) == null) {
            Log.d(TAG, "NewsApi Info : Fetch all news from network and rebuild cache.");
            List<NewsItem> allNews = NewsCrawler(TYPE_ALL);
            rebuildTypeCache(allNews);
            return;
        }
        if (newsRecord.get(type) == null) {
            Log.d(TAG, "NewsApi Info : Build missing type cache from TYPE_ALL.");
            List<NewsItem> allNews = newsRecord.get(TYPE_ALL);
            newsRecord.put(type, filterNewsByType(allNews, type));
        }
    }

    private void rebuildTypeCache(List<NewsItem> allNews) {
        newsRecord.clear();
        newsRecord.put(TYPE_ALL, new ArrayList<>(allNews));
        newsRecord.put(TYPE_STOCK, filterNewsByType(allNews, TYPE_STOCK));
        newsRecord.put(TYPE_BULLETIN, filterNewsByType(allNews, TYPE_BULLETIN));
        newsRecord.put(TYPE_EXCHANGE, filterNewsByType(allNews, TYPE_EXCHANGE));
        newsRecord.put(TYPE_CRYPTO, filterNewsByType(allNews, TYPE_CRYPTO));
    }

    private List<NewsItem> filterNewsByType(List<NewsItem> source, int type) {
        List<NewsItem> filtered = new ArrayList<>();
        if (source == null) {
            return filtered;
        }
        if (type == TYPE_ALL) {
            filtered.addAll(source);
            return filtered;
        }
        for (NewsItem item : source) {
            if (item.type == type) {
                filtered.add(item);
            }
        }
        return filtered;
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
     * 取得各分類新聞（CNYES + Marketaux）
     * Marketaux 使用單次 all 抓取後本地分類，Bulletin 僅保留 CNYES。
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

        if (shouldQueryMarketaux(type)) {
            newsItemList.addAll(CrawlerWithMarketauxByType(type));
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
        String apiUrl = CNYES_API_BASE + category + "?limit=" + CNYES_API_LIMIT;

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

    private static List<NewsItem> CrawlerWithMarketauxByType(int type) {
        if (type == TYPE_STOCK) {
            return CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_STOCK, TYPE_STOCK);
        }
        if (type == TYPE_EXCHANGE) {
            return CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_EXCHANGE, TYPE_EXCHANGE);
        }
        if (type == TYPE_CRYPTO) {
            return CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_CRYPTO, TYPE_CRYPTO);
        }
        if (type == TYPE_ALL) {
            List<NewsItem> allItems = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(3);
            new Thread(() -> {
                try {
                    List<NewsItem> items = CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_STOCK, TYPE_STOCK);
                    synchronized (allItems) { allItems.addAll(items); }
                } finally {
                    latch.countDown();
                }
            }).start();
            new Thread(() -> {
                try {
                    List<NewsItem> items = CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_EXCHANGE, TYPE_EXCHANGE);
                    synchronized (allItems) { allItems.addAll(items); }
                } finally {
                    latch.countDown();
                }
            }).start();
            new Thread(() -> {
                try {
                    List<NewsItem> items = CrawlerWithMarketaux(MARKETAUX_ENTITY_TYPES_CRYPTO, TYPE_CRYPTO);
                    synchronized (allItems) { allItems.addAll(items); }
                } finally {
                    latch.countDown();
                }
            }).start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "Parallel Marketaux fetch interrupted: " + e.getMessage());
            }
            return allItems;
        }
        return new ArrayList<>();
    }

    private static boolean shouldQueryMarketaux(int type) {
        return type == TYPE_ALL || type == TYPE_STOCK || type == TYPE_EXCHANGE || type == TYPE_CRYPTO;
    }

    private static List<NewsItem> CrawlerWithMarketaux(String entityTypes, int fallbackType) {
        List<NewsItem> newsItemList = new ArrayList<>();
        String token = getMarketauxApiToken();
        if (token.isEmpty()) {
            Log.w(TAG, "MARKETAUX_API_TOKEN is empty, skip Marketaux.");
            return newsItemList;
        }

        String apiUrl = MARKETAUX_API_URL
                + "?api_token=" + encodeQuery(token)
                // + "&countries=" + MARKETAUX_COUNTRIES
                + "&language=en"
                + "&published_after=" + getMarketauxPublishedAfter()
                + "&must_have_entities=true"
                + "&filter_entities=true"
                + "&entity_types=" + entityTypes
                + "&limit=" + MARKETAUX_API_LIMIT;

        Log.d(TAG, "Marketaux API URL: " + apiUrl);

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
                Log.w(TAG, "Marketaux API non-200: " + code);
                return newsItemList;
            }

            String response = readStream(conn.getInputStream());
            JSONObject root = new JSONObject(response);
            JSONArray data = root.optJSONArray("data");
            if (data == null) {
                return newsItemList;
            }

            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.optJSONObject(i);
                if (obj == null) continue;

                NewsItem item = new NewsItem();
                item.source = SOURCE_MARKETAUX;
                String sourceDomain = obj.optString("source", "");
                if (!sourceDomain.isEmpty()) {
                    item.source = sourceDomain;
                }
                item.title = obj.optString("title", "");
                item.link = obj.optString("url", "");
                item.timestamp = parseIsoToTimestamp(obj.optString("published_at", ""));
                item.type = classifyMarketauxType(obj);
                if (item.type == TYPE_ALL) {
                    item.type = fallbackType;
                }

                String imageUrl = obj.optString("image_url", "");
                if (!imageUrl.isEmpty()) {
                    item.image = getImageFromUrl(imageUrl);
                }

                if (!item.title.isEmpty() && !item.link.isEmpty() && item.type != TYPE_BULLETIN) {
                    newsItemList.add(item);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "CrawlerWithMarketauxAll error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return newsItemList;
    }

    private static String getMarketauxApiToken() {
        try {
            Class<?> buildConfigClass = Class.forName("com.msi.stockmanager.BuildConfig");
            Field field = buildConfigClass.getField("MARKETAUX_API_TOKEN");
            Object value = field.get(null);
            if (value instanceof String) {
                return ((String) value).trim();
            }
        } catch (Exception e) {
            Log.w(TAG, "Read MARKETAUX_API_TOKEN from BuildConfig failed: " + e.getMessage());
        }
        return "";
    }

    private static String getMarketauxPublishedAfter() {
        return MARKETAUX_TIME_FORMAT.format(
                Instant.now().minusSeconds(MARKETAUX_LOOKBACK_HOURS * 60L * 60L)
        );
    }

    private static String encodeQuery(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private static long parseIsoToTimestamp(String isoDateTime) {
        try {
            return Instant.parse(isoDateTime).toEpochMilli();
        } catch (Exception ignore) {
            return 0L;
        }
    }

    private static int classifyMarketauxType(JSONObject obj) {
        JSONArray entities = obj.optJSONArray("entities");
        if (entities != null) {
            for (int i = 0; i < entities.length(); i++) {
                JSONObject entity = entities.optJSONObject(i);
                if (entity == null) continue;
                String entityType = entity.optString("type", "").toLowerCase();
                if (!entityType.isEmpty()) {
                    return classifyMarketauxTypeByEntityAndText(
                        entityType,
                        obj.optString("title", ""),
                        obj.optString("description", ""),
                        obj.optString("snippet", "")
                );
                }
            }
        }

        return classifyMarketauxTypeByEntityAndText(
                "",
                obj.optString("title", ""),
                obj.optString("description", ""),
                obj.optString("snippet", "")
        );
    }

    private static int classifyMarketauxTypeByEntityAndText(String entityType, String title, String description, String snippet) {
        if ("cryptocurrency".equals(entityType)) return TYPE_CRYPTO;
        if ("currency".equals(entityType)) return TYPE_EXCHANGE;
        if ("equity".equals(entityType) || "index".equals(entityType)
                || "etf".equals(entityType) || "mutualfund".equals(entityType)) {
            return TYPE_STOCK;
        }

        String text = (title + " " + description + " " + snippet).toLowerCase();
        if (containsAny(text, "crypto", "cryptocurrency", "bitcoin", "btc", "ethereum", "eth", "blockchain", "token")) {
            return TYPE_CRYPTO;
        }
        if (containsAny(text, "forex", "fx", "currency", "usd", "eur", "jpy", "gbp", "aud", "cad", "chf", "nzd")) {
            return TYPE_EXCHANGE;
        }
        return TYPE_STOCK;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
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
