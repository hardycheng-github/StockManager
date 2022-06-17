package com.msi.stockmanager.data;

import android.content.Context;
import android.os.AsyncTask;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.stock.MyStockUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.ui.main.overview.OverviewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountUtil {
    private static boolean hasValue = false;
    private static DataChangedTask onDataChangedTask;
    private static Context mContext;
    private static List<AccountUpdateListener> mListenerList = new ArrayList<>();
    private static AccountValue accountValue = new AccountValue();
    private static ITransApi.TransUpdateListener transUpdateListener = new ITransApi.TransUpdateListener() {
        @Override
        public void onAdd(Transaction trans) {
            onDataChanged();
        }

        @Override
        public void onEdit(long transId, Transaction trans) {
            onDataChanged();
        }

        @Override
        public void onRemove(long transId) {
            onDataChanged();
        }
    };
    private static class DataChangedTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            accountValue.stockValueMap.clear();
            for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
                StockInfo info = StockUtilKt.getStockInfoOrNull(trans.stock_id);
                if(info != null){
                    StockValue stockValue = accountValue.stockValueMap.getOrDefault(trans.stock_id, new StockValue());
                    stockValue.holdingAmount += trans.stock_amount;
                    if(trans.stock_amount < 0){
                        stockValue.historyAmount += Math.abs(trans.stock_amount);
                        stockValue.historyCost += Math.abs(trans.cash_amount);
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            for(AccountUpdateListener listener: mListenerList){
                listener.onUpdate(accountValue);
            }
            onDataChangedTask = null;
        }
    }

    private synchronized static void onDataChanged(){
        if(onDataChangedTask != null && onDataChangedTask.getStatus() == AsyncTask.Status.RUNNING){
            onDataChangedTask.cancel(true);
        }
        onDataChangedTask = new DataChangedTask();
        onDataChangedTask.execute();
    }

    public static void init(Context context){
        mContext = context;
        ApiUtil.transApi.addTransUpdateListener(transUpdateListener);
    }

    public static void close(){
        mContext = null;
        ApiUtil.transApi.removeTransUpdateListener(transUpdateListener);
        if(onDataChangedTask != null && onDataChangedTask.getStatus() == AsyncTask.Status.RUNNING){
            onDataChangedTask.cancel(true);
        }
    }

    public static AccountValue getAccountValue(){
        return hasValue ? accountValue : null;
    }

    public static boolean addListener(AccountUpdateListener listener){
        if(hasValue) listener.onUpdate(accountValue);
        return mListenerList.add(listener);
    }

    public static boolean removeListener(AccountUpdateListener listener){
        return mListenerList.remove(listener);
    }

    public interface AccountUpdateListener{
        void onUpdate(AccountValue accountValue);
    }

    public static class AccountValue {
        public int accountCalcTotal;
        public int accountTotal;
        public int accountProfit;
        public double accountProfitPercent;
        public int stockCostTotal;
        public int stockCalcTotal;
        public int cashBalance;
        public int cashInTotal;
        public int cashOutTotal;
        public Map<String, StockValue> stockValueMap = new HashMap<>();
    }

    public static class StockValue{
        public StockInfo info;
        public int holdingAmount;
        public int holdingCalc;
        public int holdingCost;
        public int holdingProfit;
        public int historyAmount;
        public int historyCost;
        public int historyProfit;
    }
}
