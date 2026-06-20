package com.msi.stockmanager.data.notify;

/**
 * MACD 關注等級（對應不同 MACD 參數）
 */
public enum MaAlertLevel {
    LOW,      // 保守：MACD(24,52,18)
    DEFAULT,  // 穩健（預設）：MACD(12,26,9)
    HIGH;     // 積極：MACD(6,13,5)
    
    /**
     * 從字串轉換為枚舉值
     * @param value 字串值
     * @return 對應的枚舉值，如果無效則返回預設值 DEFAULT
     */
    public static MaAlertLevel fromString(String value) {
        if (value == null) {
            return DEFAULT;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
    
    /**
     * 轉換為字串（用於 SharedPreferences 存儲）
     * @return 枚舉名稱字串
     */
    @Override
    public String toString() {
        return name();
    }
}
