package com.msi.stockmanager.data.news;

import static org.junit.Assert.*;

import android.content.Context;
import android.view.View;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.DateUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
public class NewsApiTest {
    private INewsApi newsApi;
    @Before
    public void setUp() throws Exception {
        this.newsApi = new NewsApi();
    }

    //測試NewsApi - NewsCrawler(改成thread方式之後不可用)
    /*
    @Test
    public void NewsCrawlerApiTest() throws Exception {
        Method method = this.newsApi.getClass().getDeclaredMethod("NewsCrawler", int.class);
        method.setAccessible(true);

        Object[] parameters = new Object[1];
        parameters[0] = INewsApi.TYPE_ALL;

        List<INewsApi.NewsItem> newsItemList = (List<INewsApi.NewsItem>) method.invoke(this.newsApi, parameters);
        assertTrue("newsItemList size should be greater than one.", newsItemList.size() > 0);

        //Debug message, check data getting is correct.
        for (INewsApi.NewsItem item : newsItemList) {
            System.out.println(item.link);
            System.out.println(item.source);
            System.out.println(item.timestamp);
            System.out.println(item.title);
            System.out.println(item.type);
        }
    }
     */

    // 針對 SOURCE_INFO 各筆呼叫 CrawlerWithCNYES（僅 CNYES 來源）測試
    @Test
    public void CrawlerNewsUrlTest() throws Exception {
        Method crawlerWithCNYES = this.newsApi.getClass().getDeclaredMethod("CrawlerWithCNYES", String.class);
        crawlerWithCNYES.setAccessible(true);

        Field field = this.newsApi.getClass().getDeclaredField("SOURCE_INFO");
        field.setAccessible(true);
        String[][] sourceInfo = (String[][]) field.get(this.newsApi);

        for (int x = 0; x < sourceInfo.length; x++) {
            List<INewsApi.NewsItem> newsItemList = (List<INewsApi.NewsItem>) crawlerWithCNYES.invoke(this.newsApi, sourceInfo[x][2]);
            assertTrue("newsItemList size should be greater than one.\n"
                    + "Source : " + sourceInfo[x][0] + "\n"
                    + "Url : " + sourceInfo[x][2] + "\n",
                    newsItemList.size() > 0);
        }
    }

}