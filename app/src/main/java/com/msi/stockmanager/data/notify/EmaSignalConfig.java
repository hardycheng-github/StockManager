package com.msi.stockmanager.data.notify;

import com.msi.stockmanager.kline.KData;

/**
 * EMA 突破/跌落事件配置
 */
public class EmaSignalConfig {

    public static final int EMA_PERIOD = 20;

    public static int getMinWarmupBars() {
        return EMA_PERIOD;
    }

    public static int getEventScanDays() {
        return MacdSignalConfig.getEventScanDays();
    }

    public static double getEmaValue(KData kData) {
        if (kData == null) {
            return -1;
        }
        return kData.getEma20();
    }

    public static String getNotifyType(boolean isBreakthrough) {
        return isBreakthrough ? "EMA20_BREAKTHROUGH" : "EMA20_BREAKDOWN";
    }

    public static String getActionLabel(boolean isBreakthrough) {
        return isBreakthrough ? "EMA 突破" : "EMA 跌落";
    }

    public static String getNotifyBody(double price, double emaValue) {
        return String.format("收盤價：%.2f，EMA20：%.2f", price, emaValue);
    }
}
