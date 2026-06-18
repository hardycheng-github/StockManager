package com.msi.stockmanager.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String toDateTimeString(long time){
        return DATE_TIME.format(java.time.Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()));
    }

    public static String toDateString(long time){
        return LocalDate.ofInstant(java.time.Instant.ofEpochMilli(time), ZoneId.systemDefault()).toString();
    }

    public static long parseDateTime(String str){
        if (str == null || str.isEmpty()) return 0;
        try {
            String value = str.trim();
            if (value.length() >= 19) {
                return LocalDateTime.parse(value.substring(0, 19), DATE_TIME_SECONDS)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            if (value.length() >= 16) {
                return LocalDateTime.parse(value.substring(0, 16), DATE_TIME)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            return parseDate(value);
        } catch (DateTimeParseException e){
            return 0;
        }
    }

    public static long parseDate(String str){
        if (str == null || str.isEmpty()) return 0;
        try {
            String value = str.trim();
            if (value.length() >= 10) {
                value = value.substring(0, 10);
            }
            return LocalDate.parse(value)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (DateTimeParseException e){
            return 0;
        }
    }
}
