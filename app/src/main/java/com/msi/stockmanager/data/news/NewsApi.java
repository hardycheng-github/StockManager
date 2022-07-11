package com.msi.stockmanager.data.news;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.ui.main.pager.PagerActivity;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NewsApi implements INewsApi {
    private static final String TAG = NewsApi.class.getSimpleName();
    private Context parentsContext;
    public NewsApi(Context context) {
        parentsContext = context;
    }

    @Override
    public void getNewsList(int type, ResultCallback callback) {
        Thread task = new Thread(()->{
            try {
                List<NewsItem> newsItemList = new ArrayList<>();
                // TODO implement

                // *****************
                // Sample Test Start
                // *****************
                NewsItem item;
                item = new NewsItem();
                item.type = INewsApi.TYPE_CRYPTO;
                item.source = SOURCE_CNYES;
                item.link = "https://news.cnyes.com/news/id/4909621?exp=a";
                item.image = getImageFromUrl("https://cimg.cnyes.cool/prod/news/4908443/s/f20fb0c6fbcefdaf008a03cdcaf2a362.jpg");
                item.title = "專家：市場去槓桿程度已達90% 但加密貨幣短期內走勢仍震盪";
                item.timestamp = DateUtil.parseDateTime("2022-07-08 15:16");
                newsItemList.add(item);

                item = new NewsItem();
                item.type = INewsApi.TYPE_CRYPTO;
                item.source = SOURCE_CNYES;
                item.link = "https://news.cnyes.com/news/id/4909630?exp=a";
                item.image = getImageFromUrl("https://cimg.cnyes.cool/prod/news/4909630/l/3e5562f127e8c63486e3b50c0c970448.jpg");
                item.title = "風險偏好回歸 比特幣短線走強 升破22000美元";
                item.timestamp = DateUtil.parseDateTime("2022-07-08 14:07");
                newsItemList.add(item);
                // ***************
                // Sample Test End
                // ***************

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
            Log.e(TAG, "err: " + e.getMessage());
        }
        return null;
    }
}
