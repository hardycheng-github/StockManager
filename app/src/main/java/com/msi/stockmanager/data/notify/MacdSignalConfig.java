package com.msi.stockmanager.data.notify;

/**
 * MACD 事件通知配置管理類
 */
public class MacdSignalConfig {

    public static final int EVENT_SCAN_DAYS = 31;

    public static class MacdParams {
        public final int fastPeriod;
        public final int slowPeriod;
        public final int signalPeriod;

        public MacdParams(int fastPeriod, int slowPeriod, int signalPeriod) {
            this.fastPeriod = fastPeriod;
            this.slowPeriod = slowPeriod;
            this.signalPeriod = signalPeriod;
        }
    }

    /**
     * 根據關注等級返回對應的 MACD 參數
     */
    public static MacdParams getMacdParams(MaAlertLevel level) {
        switch (level) {
            case LOW:
                return new MacdParams(24, 52, 18);
            case HIGH:
                return new MacdParams(6, 13, 5);
            case DEFAULT:
            default:
                return new MacdParams(12, 26, 9);
        }
    }

    public static String getMacdLabel(MacdParams params) {
        return String.format("MACD(%d,%d,%d)", params.fastPeriod, params.slowPeriod, params.signalPeriod);
    }

    public static int getMinWarmupBars(MacdParams params) {
        return params.slowPeriod + params.signalPeriod;
    }

    public static int getEventScanDays() {
        return EVENT_SCAN_DAYS;
    }

    /**
     * 生成通知類型字串，如 "MACD_12_26_9_GOLDEN" 或 "MACD_12_26_9_DEATH"
     */
    public static String getNotifyType(MacdParams params, boolean isGoldenCross) {
        String prefix = String.format("MACD_%d_%d_%d", params.fastPeriod, params.slowPeriod, params.signalPeriod);
        return isGoldenCross ? prefix + "_GOLDEN" : prefix + "_DEATH";
    }

    public static String getActionLabel(boolean isGoldenCross) {
        return isGoldenCross ? "MACD 突破" : "MACD 跌落";
    }

    public static String getLevelLabel(MaAlertLevel level) {
        if (level == null) {
            level = MaAlertLevel.DEFAULT;
        }
        switch (level) {
            case LOW:
                return "保守";
            case HIGH:
                return "積極";
            case DEFAULT:
            default:
                return "穩健";
        }
    }

    public static String getCrossLabel(boolean isGoldenCross) {
        return isGoldenCross ? "黃金交叉" : "死亡交叉";
    }

    public static String getSignalLabel(MaAlertLevel level, boolean isGoldenCross) {
        return getLevelLabel(level) + "型" + getCrossLabel(isGoldenCross);
    }

    public static String getNotifyBody(MaAlertLevel level, boolean isGoldenCross, double price) {
        return String.format("%s，收盤價：%.2f", getSignalLabel(level, isGoldenCross), price);
    }

    public static boolean isBullishType(String type) {
        return type != null && (type.endsWith("_GOLDEN") || type.endsWith("_BREAKTHROUGH"));
    }

    public static boolean isBearishType(String type) {
        return type != null && (type.endsWith("_DEATH") || type.endsWith("_BREAKDOWN"));
    }
}
