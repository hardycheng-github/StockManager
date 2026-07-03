package com.msi.stockmanager.data.notify;

/**
 * 布林通道突破/跌落事件配置
 */
public class BollSignalConfig {

    public static final int BOLL_PERIOD = 22;
    public static final int BOLL_K = 2;

    public static int getMinWarmupBars() {
        return BOLL_PERIOD;
    }

    public static int getEventScanDays() {
        return MacdSignalConfig.getEventScanDays();
    }

    public static String getNotifyType(boolean isBreakthrough) {
        return isBreakthrough ? "BOLL_22_2_BREAKTHROUGH" : "BOLL_22_2_BREAKDOWN";
    }

    public static String getActionLabel(boolean isBreakthrough) {
        return isBreakthrough ? "突破上行通道" : "跌落下行通道";
    }

    public static String getNotifyBody(double price, double bandValue, boolean isUpper) {
        String bandLabel = isUpper ? "上軌" : "下軌";
        return String.format("收盤價：%.2f，%s：%.2f", price, bandLabel, bandValue);
    }
}
