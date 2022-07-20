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

    //測試NewsApi - NewsCrawler
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

    //調用NewsApi各個網站爬蟲的函式，針對SOURCE_INFO的Url各別測試
    @Test
    public void CrawlerNewsUrlTest() throws Exception {
        Method fackCrawlerWithCNYES = this.newsApi.getClass().getDeclaredMethod("CrawlerWithCNYES", String.class);
        fackCrawlerWithCNYES.setAccessible(true);

        Method fackCrawlerWithYahoo = this.newsApi.getClass().getDeclaredMethod("CrawlerWithYahoo", String.class);
        fackCrawlerWithYahoo.setAccessible(true);

        Method fackCrawlerWithChinatimes = this.newsApi.getClass().getDeclaredMethod("CrawlerWithChinatimes", String.class);
        fackCrawlerWithChinatimes.setAccessible(true);

        Field field = this.newsApi.getClass().getDeclaredField("SOURCE_INFO");
        field.setAccessible(true);
        String[][] fackSourceInfo = (String[][]) field.get(this.newsApi);

        for(int x=0; x<fackSourceInfo.length; x++) {
            List<INewsApi.NewsItem> newsItemList = new ArrayList<>();
            switch (fackSourceInfo[x][0]) {
                case "CNYES":
                    newsItemList = (List<INewsApi.NewsItem>) fackCrawlerWithCNYES.invoke(this.newsApi, fackSourceInfo[x][2]);
                    break;
                case "YAHOO":
                    newsItemList = (List<INewsApi.NewsItem>) fackCrawlerWithYahoo.invoke(this.newsApi, fackSourceInfo[x][2]);
                    break;
                case "CHINATIMES":
                    newsItemList = (List<INewsApi.NewsItem>) fackCrawlerWithChinatimes.invoke(this.newsApi, fackSourceInfo[x][2]);
                    break;
                default:
                    fail("Find a source without defined crawler method");
            }
            assertTrue("newsItemList size should be greater than one.\n"
                    + "Source : " + fackSourceInfo[x][0] + "\n"
                    + "Url : " + fackSourceInfo[x][2] + "\n"
                    , newsItemList.size() > 0);
        }

    }

}