package com.msi.stockmanager.ui.main.trans_history;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransHistoryUtil {

    public static String keyword = "";
    public static Set<Integer> targetTypes = new HashSet<>();
    public static long startTime = 0;
    public static long endTime = Long.MAX_VALUE;
    public static final long ONE_MINUTE_MS = 60*1000;
    public static final long ONE_DAY_MS = 24*60*ONE_MINUTE_MS;

    public static void resetFilter(){
        targetTypes.clear();
        startTime = 0;
        endTime = Long.MAX_VALUE;
    }

    public static List<Transaction> getListWithFilter() {
        List<Transaction> list = new ArrayList<>();
        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
            StockInfo info = StockUtilKt.getStockInfoOrNull(trans.stock_id);
            String stockName = info == null ? "" : info.getStockNameWithId();
            if((keyword != null && !keyword.isEmpty() && !stockName.contains(keyword)) ||
                    (!targetTypes.isEmpty() && !targetTypes.contains(trans.trans_type)) ||
                    !isInTimeRange(trans.trans_time)){
                continue;
            }
            list.add(trans);
        }
        return list;
    }

    private static boolean isInTimeRange(long time){
        String timeStr = DateUtil.toDateString(time);
        String startStr = DateUtil.toDateString(startTime);
        String endStr = DateUtil.toDateString(endTime);
        return timeStr.compareTo(startStr) >= 0 && timeStr.compareTo(endStr) <= 0;
//        long timeUnit = time / ONE_DAY_MS;
//        long startUnit = startTime / ONE_DAY_MS;
//        long endUnit = endTime / ONE_DAY_MS;
//        return timeUnit >= startUnit && timeUnit <= endUnit;
    }

    public static boolean isFilterActive(){
        return (targetTypes.size() > 0 || startTime > 0 || endTime < Long.MAX_VALUE);
    }
}
