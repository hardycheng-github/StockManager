package com.msi.stockmanager.data.news;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    // 驗證 CNYES 來源配置包含公告類別
    @Test
    public void CrawlerNewsUrlTest() throws Exception {
        Field field = this.newsApi.getClass().getDeclaredField("SOURCE_INFO");
        field.setAccessible(true);
        String[][] sourceInfo = (String[][]) field.get(this.newsApi);

        boolean hasCnyesBulletin = false;
        for (int x = 0; x < sourceInfo.length; x++) {
            if ("CNYES".equals(sourceInfo[x][0])
                    && String.valueOf(INewsApi.TYPE_BULLETIN).equals(sourceInfo[x][1])
                    && sourceInfo[x][2].contains("announcement")) {
                hasCnyesBulletin = true;
                break;
            }
        }
        assertTrue("Source config should include CNYES announcement for TYPE_BULLETIN.", hasCnyesBulletin);
    }

    @Test
    public void classifyMarketauxTypeTest() throws Exception {
        Method classifyMethod = this.newsApi.getClass().getDeclaredMethod(
                "classifyMarketauxTypeByEntityAndText",
                String.class, String.class, String.class, String.class
        );
        classifyMethod.setAccessible(true);

        int cryptoType = (int) classifyMethod.invoke(this.newsApi, "cryptocurrency", "BTC jumps", "", "");
        assertEquals(INewsApi.TYPE_CRYPTO, cryptoType);

        int exchangeType = (int) classifyMethod.invoke(this.newsApi, "currency", "USD weakens", "", "");
        assertEquals(INewsApi.TYPE_EXCHANGE, exchangeType);

        int stockType = (int) classifyMethod.invoke(this.newsApi, "equity", "AAPL earnings", "", "");
        assertEquals(INewsApi.TYPE_STOCK, stockType);
    }

    @Test
    public void marketauxShouldNotBeIncludedInBulletinTest() throws Exception {
        Method includeMethod = this.newsApi.getClass().getDeclaredMethod("shouldQueryMarketaux", int.class);
        includeMethod.setAccessible(true);

        boolean includeBulletin = (boolean) includeMethod.invoke(this.newsApi, INewsApi.TYPE_BULLETIN);
        boolean includeStock = (boolean) includeMethod.invoke(this.newsApi, INewsApi.TYPE_STOCK);
        boolean includeExchange = (boolean) includeMethod.invoke(this.newsApi, INewsApi.TYPE_EXCHANGE);
        boolean includeCrypto = (boolean) includeMethod.invoke(this.newsApi, INewsApi.TYPE_CRYPTO);
        boolean includeAll = (boolean) includeMethod.invoke(this.newsApi, INewsApi.TYPE_ALL);

        assertFalse(includeBulletin);
        assertTrue(includeStock);
        assertTrue(includeExchange);
        assertTrue(includeCrypto);
        assertTrue(includeAll);
    }
}
