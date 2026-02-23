package com.msi.stockmanager.data.notify;

/**
 * 平均線關注等級
 */
public enum MaAlertLevel {
    LOW,      // 保守：只關注30日平均線
    DEFAULT,  // 穩健（預設）：關注10日、30日平均線
    HIGH;     // 積極：關注5日、10日、30日平均線
    
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
