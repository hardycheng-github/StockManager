package com.msi.stockmanager.data.profile;

import com.msi.stockmanager.kline.KLineView;

/**
 * 智慧分析 K 線主圖指標類型
 */
public enum ChartIndicatorType {
    MA,
    EMA,
    BBAND;

    public static ChartIndicatorType fromString(String value) {
        if (value == null) {
            return EMA;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EMA;
        }
    }

    public int toKLineMainImgType() {
        switch (this) {
            case EMA:
                return KLineView.MAIN_IMG_EMA;
            case BBAND:
                return KLineView.MAIN_IMG_BOLL;
            case MA:
            default:
                return KLineView.MAIN_IMG_MA;
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
