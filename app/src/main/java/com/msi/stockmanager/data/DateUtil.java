package com.msi.stockmanager.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public final static SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

    public static String toDateTimeString(long time){
        return formatDateTime.format(new Date(time));
    }

    public static String toDateString(long time){
        return formatDate.format(new Date(time));
    }

    public static long parseDateTime(String str){
        try {
            return formatDateTime.parse(str).getTime();
        } catch (Exception e){
            return 0;
        }
    }

    public static long parseDate(String str){
        try {
            return formatDate.parse(str).getTime();
        } catch (Exception e){
            return 0;
        }
    }
}
