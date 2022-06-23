package com.msi.stockmanager.data;

import android.content.Context;

import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.R;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class FormatUtil {

    private static NumberFormat currency = NumberFormat.getCurrencyInstance();
    private static NumberFormat number = NumberFormat.getNumberInstance();
    private static NumberFormat percent = NumberFormat.getPercentInstance();

    public static String currency(int val){
        return currency.format(val);
    }

    public static String number(int val){
        return number.format(val);
    }

    public static String number(double val){
        return number.format(val);
    }

    public static String percent(double val){
        return percent.format(val);
    }

    public static String transType(Context context, int type){
        switch (type){
            case TransType.TRANS_TYPE_STOCK_BUY:
                return context.getString(R.string.TRANS_TYPE_STOCK_BUY);
            case TransType.TRANS_TYPE_STOCK_SELL:
                return context.getString(R.string.TRANS_TYPE_STOCK_SELL);
            case TransType.TRANS_TYPE_CASH_IN:
                return context.getString(R.string.TRANS_TYPE_CASH_IN);
            case TransType.TRANS_TYPE_CASH_OUT:
                return context.getString(R.string.TRANS_TYPE_CASH_OUT);
            case TransType.TRANS_TYPE_CASH_DIVIDEND:
                return context.getString(R.string.TRANS_TYPE_CASH_DIVIDEND);
            case TransType.TRANS_TYPE_STOCK_DIVIDEND:
                return context.getString(R.string.TRANS_TYPE_STOCK_DIVIDEND);
            case TransType.TRANS_TYPE_CASH_REDUCTION:
                return context.getString(R.string.TRANS_TYPE_CASH_REDUCTION);
            case TransType.TRANS_TYPE_STOCK_REDUCTION:
                return context.getString(R.string.TRANS_TYPE_STOCK_REDUCTION);
        }
        return "";
    }

    static {
        percent.setMinimumFractionDigits(2);
        currency.setMinimumFractionDigits(0);
        number.setMaximumFractionDigits(2);
    }
}
