package com.msi.stockmanager.data.news;


import android.graphics.Bitmap;

import com.msi.stockmanager.data.DateUtil;

import java.util.List;

public interface INewsApi {
    int TYPE_ALL = 0; //全部
    int TYPE_STOCK = 1; //股票
    int TYPE_BULLETIN = 2; //股市公告
    int TYPE_EXCHANGE = 3; //外匯
    int TYPE_CRYPTO = 4; //加密貨幣

    String SOURCE_CNYES = "鉅亨新聞";
    String SOURCE_YAHOO = "Yahoo財經";
    String SOURCE_CHINATIMES = "中時新聞網";

    class NewsItem {
        public int type; //news type, ex: TYPE_STOCK,TYPE_BULLETIN,TYPE_EXCHANGE,TYPE_CRYPTO
        public String source; //source web name, ex: Yahoo財經, 鉅亨新聞
        public Bitmap image; //image or null
        public long timestamp; //news posted date
        public String title; //news title
        public String link; //news link

        public String getSubtitle(){
            long diffTime = System.currentTimeMillis() - timestamp;
            String timeStr = DateUtil.toDateString(timestamp);
            if(diffTime < 1000*60) {
                timeStr = "現在"; // 1分內
            } else if(diffTime < 1000*60*60) {
                timeStr = (diffTime / (1000*60)) + "分鐘前"; //1小時內
            } else if(diffTime < 1000*60*60*24){
                timeStr = (diffTime / (1000*60*60)) + "小時前"; //1天內
            }
            return String.format("%s / %s", source, timeStr);
        }
    }

    interface ResultCallback {
        void onResult(List<NewsItem> newsItemList);
        void onException(Exception e);
    }

    /**
     * 取得新聞列表
     * @param type 新聞類型, ex: TYPE_STOCK,TYPE_BULLETIN,TYPE_EXCHANGE,TYPE_CRYPTO
     * @param force 強制要求刷新
     * @param callback 回調
     */
    void getNewsList(int type, boolean force, ResultCallback callback);
}
