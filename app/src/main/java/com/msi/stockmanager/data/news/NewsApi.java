package com.msi.stockmanager.data.news;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.ui.main.pager.PagerActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NewsApi implements INewsApi {
    private static final String TAG = NewsApi.class.getSimpleName();
    private Context parentsContext;
    private Map<Integer, List<NewsItem>> newsRecord;

    //0:全部, 1:股票, 2:股市公告, 3:外匯, 4:加密貨幣
    //{ source, type, url }
    private static final String[][] SOURCE_INFO = {
            {"CNYES","1","https://news.cnyes.com/news/cat/tw_quo"},                //鉅亨 - 台股
            {"CNYES","2","https://news.cnyes.com/news/cat/announcement"},          //鉅亨 - 公告
            {"CNYES","3","https://news.cnyes.com/news/cat/forex",},                //鉅亨 - 外匯
            {"CNYES","4","https://news.cnyes.com/news/cat/bc_crypto"},             //鉅亨 - 虛擬貨幣
            {"YAHOO","1","https://tw.stock.yahoo.com/tw-market"},                  //奇摩 - 台股
            {"CHINATIMES","1","https://wantrich.chinatimes.com/newslist/420101/1"},   //中時新聞網 - 台股(上市櫃)
            {"CHINATIMES","3","https://www.chinatimes.com/Search/%E5%A4%96%E5%8C%AF?chdtv"},   //中時新聞網 - 搜尋(外匯)
            {"CHINATIMES","4","https://www.chinatimes.com/search/%E5%8A%A0%E5%AF%86%E8%B2%A8%E5%B9%A3?chdtv"}   //中時新聞網 - 搜尋(加密貨幣)

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
                        Log.d(TAG, "NewsApi Info : News data not found, crawler data!");
                        newsItemList = NewsCrawler(type);
                        newsRecord.put(type, newsItemList);
                    }
                }else{
                    Log.d(TAG, "NewsApi Info : Force refresh news data, crawler data!");
                    newsItemList = NewsCrawler(type);
                    newsRecord.put(type, newsItemList);
                }

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
            //Log.e(TAG, "err: " + e.getMessage());
        }
        return null;
    }

    /**
     * 爬蟲取得各新聞網站的各個種類新聞
     * @param type 新聞種類
     * @return List<NewsItem>
     */
    private static List<NewsItem> NewsCrawler(int type){

        List<NewsItem> newsItemList = new ArrayList<>();
        for(int x=0; x<SOURCE_INFO.length; x++) {
            // 挑選出type相符的 or type:0 全部
            if (Integer.parseInt(SOURCE_INFO[x][1]) == type || 0 == type){
                List<NewsItem> itemList = new ArrayList<>();
                switch (SOURCE_INFO[x][0]) {
                    case "CNYES":
                        itemList = CrawlerWithCNYES(SOURCE_INFO[x][2]);
                        break;
                    case "YAHOO":
                        itemList = CrawlerWithYahoo(SOURCE_INFO[x][2]);
                        break;
                    case "CHINATIMES":
                        itemList = CrawlerWithChinatimes(SOURCE_INFO[x][2]);
                        break;
                }

                for (NewsItem item : itemList) {
                    item.type = type;

                    switch (SOURCE_INFO[x][0]) {
                        case "CNYES":
                            item.source = SOURCE_CNYES;
                            break;
                        case "YAHOO":
                            item.source = SOURCE_YAHOO;
                            break;
                        case "CHINATIMES":
                            item.source = SOURCE_CHINATIMES;
                            break;
                    }

                    newsItemList.add(item);
                }
            }
        }

        return newsItemList;
    }

    /**
     * 爬蟲取得鉅亨網的新聞
     * @param URL 爬蟲網址
     * @return List<NewsItem>
     */
    private static List<NewsItem> CrawlerWithCNYES(String URL){
        List<NewsItem> newsItemList = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(URL).get();

            Elements newsListByCrawler = doc.select("div.theme-list").first()
                    .select(":root > div > a");

            for (Element el : newsListByCrawler) {
                NewsItem item;
                item = new NewsItem();
                item.source = SOURCE_CNYES;
                item.title = el.attr("title");
                item.link = el.absUrl("href");

                try {
                    String imageUrl = el.select(":root > div > figure > img").attr("src").toString();
                    item.image = getImageFromUrl(imageUrl);
                } catch (NullPointerException e){
                    System.out.println("DOM not found.");
                }

                try {
                    String time = el.select(":root > div.theme-meta > time").attr("datetime").toString();
                    SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

                    item.timestamp = datetime.parse(time).getTime();
                } catch (NullPointerException e) {
                    System.out.println("DOM not found.");
                }
                newsItemList.add(item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return newsItemList;
    }

    /**
     * 爬蟲取得奇摩股市的新聞
     * @param URL 爬蟲網址
     * @return List<NewsItem>
     */
    private static List<NewsItem> CrawlerWithYahoo(String URL){
        List<NewsItem> newsItemList = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(URL).get();
            Elements newsListByCrawler = doc.select("div#YDC-Stream").select(":root > ul > li");
            for (Element el : newsListByCrawler) {
                NewsItem item;
                item = new NewsItem();

                //獲取標題及連結，若獲取失敗 - 跳出
                try {
                    item.link = el.select(":root > div > div")
                            .select("a.mega-item-header-link").first().absUrl("href");
                    item.title = el.select(":root > div > div")
                            .select("a.mega-item-header-link").first().text();
                } catch (NullPointerException e){
                    System.out.println("DOM not found.");
                    continue;
                }

                //有可能沒有圖片，若獲取失敗 - 繼續
                try {
                    String imageUrl = el.select(":root > div > div > div").get(0)
                            .select("img").first().attr("src").toString();
                    item.image = getImageFromUrl(imageUrl);

                } catch (NullPointerException e){
                    System.out.println("DOM not found.");
                }

                newsItemList.add(item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return newsItemList;
    }

    /**
     * 爬蟲取得中時新聞網的新聞
     * @param URL 爬蟲網址
     * @return List<NewsItem>
     */
    private static List<NewsItem> CrawlerWithChinatimes(String URL){
        List<NewsItem> newsItemList = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(URL).get();
            Elements newsListByCrawler = doc.select("ul.vertical-list").select(":root > li");
            for (Element el : newsListByCrawler) {
                NewsItem item;
                item = new NewsItem();

                //獲取標題及連結，若獲取失敗 - 跳出
                try {
                    item.link = el.select(":root > div > div > div")
                            .select(".title").first().select("a").first().absUrl("href");
                    item.title = el.select(":root > div > div > div")
                            .select(".title").first().select("a").text();
                } catch (Exception e){
                    System.out.println("DOM not found.");
                    continue;
                }

                //獲取時間，若獲取失敗 - 繼續
                try {
                    String time = el.select(":root > div > div > div > div.meta-info > time")
                            .attr("datetime");
                    if(time != null){
                        if (time.matches("\\d+")) { //1658217101
                            item.timestamp = Long.parseLong(time) * 1000;
                        } else if (time.matches("\\d{4}[\\-]\\d{2}[\\-]\\d{2}\\s\\d{2}[:]\\d{2}")){ //2022-07-19 11:58
                            SimpleDateFormat datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            item.timestamp = datetime.parse(time).getTime();
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println("DOM not found.");
                }

                //有可能沒有圖片，若獲取失敗 - 繼續
                try {
                    String imageUrl = el.select(":root > div > div > div > div.thumb-photo")
                            .select("img").attr("src");
                    item.image = getImageFromUrl(imageUrl);

                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                newsItemList.add(item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return newsItemList;
    }
}
