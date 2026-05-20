package com.msi.stockmanager.data.notify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 平均線突破配置管理類
 */
public class MaBreakthroughConfig {

    /** 同日多均線觸發時的保留優先順序：週期最長者優先 */
    public static final int[] MA_PRIORITY_ORDER = {30, 10, 5};

    /**
     * 比較兩條均線的合併優先級，回傳應保留者（30 > 10 > 5）
     */
    public static int compareMaPriority(int a, int b) {
        return maPriority(a) >= maPriority(b) ? a : b;
    }

    /**
     * @return true 表示 candidate 優先級高於 current（應取代 current）
     */
    public static boolean isHigherMaPriority(int candidate, int current) {
        return maPriority(candidate) > maPriority(current);
    }

    private static int maPriority(int days) {
        switch (days) {
            case 30:
                return 3;
            case 10:
                return 2;
            case 5:
                return 1;
            default:
                return 0;
        }
    }
    
    /**
     * 根據關注等級返回對應的平均線天數列表
     * @param level 關注等級
     * @return 平均線天數列表
     */
    public static List<Integer> getMaDays(MaAlertLevel level) {
        switch (level) {
            case LOW:
                // 保守：只關注30日平均線
                return Arrays.asList(30);
            case DEFAULT:
                // 穩健：關注10日、30日平均線
                return Arrays.asList(10, 30);
            case HIGH:
                // 積極：關注5日、10日、30日平均線
                return Arrays.asList(5, 10, 30);
            default:
                return Arrays.asList(10, 30);
        }
    }
    
    /**
     * 根據天數獲取平均線名稱
     * @param days 天數
     * @return 平均線名稱
     */
    public static String getMaName(int days) {
        switch (days) {
            case 5:
                return "五日平均線";
            case 10:
                return "十日平均線";
            case 30:
                return "三十日平均線";
            default:
                return days + "日平均線";
        }
    }
    
    /**
     * 根據天數獲取對應的 MA 值（從 KData）
     * @param kData K線數據
     * @param days 天數
     * @return MA 值，如果無效則返回 -1
     */
    public static double getMaValue(com.msi.stockmanager.kline.KData kData, int days) {
        if (kData == null) {
            return -1;
        }
        switch (days) {
            case 5:
                return kData.getPriceMa5();
            case 10:
                return kData.getPriceMa10();
            case 30:
                return kData.getPriceMa30();
            default:
                return -1;
        }
    }
    
    /**
     * 生成通知類型字串
     * @param days 天數
     * @param isBreakthrough true 為突破，false 為跌破
     * @return 通知類型字串，如 "MA5_BREAKTHROUGH" 或 "MA5_BREAKDOWN"
     */
    public static String getNotifyType(int days, boolean isBreakthrough) {
        String maPrefix = "MA" + days;
        return isBreakthrough ? maPrefix + "_BREAKTHROUGH" : maPrefix + "_BREAKDOWN";
    }
}
