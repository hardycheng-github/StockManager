package com.msi.stockmanager.data;

import java.text.NumberFormat;

public class FormatUtil {

    private static NumberFormat number = NumberFormat.getNumberInstance();
    private static NumberFormat percent = NumberFormat.getPercentInstance();

    public static String number(int val){
        return number.format(val);
    }

    public static String number(double val){
        return number.format(val);
    }

    public static String percent(double val){
        return percent.format(val);
    }

    static {
        percent.setMinimumFractionDigits(2);
    }
}
