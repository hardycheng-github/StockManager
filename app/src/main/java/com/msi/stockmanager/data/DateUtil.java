package com.msi.stockmanager.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private final static SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

    public static String toDateTimeString(long time){
        return formatDateTime.format(new Date(time));
    }

    public static String toDateString(long time){
        return formatDate.format(new Date(time));
    }
}
