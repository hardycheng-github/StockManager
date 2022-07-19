package com.msi.stockmanager.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountUtil {
    public static final String TAG = AccountUtil.class.getSimpleName();
    public static boolean hasValue = false;
    private static DataChangedTask onDataChangedTask;
    private static Context mContext;
    private static List<AccountUpdateListener> mListenerList = new ArrayList<>();
    private static AccountValue account = new AccountValue();
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

        private int lockCount = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            account.reset();
            for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
                account.cashBalance += trans.cash_amount;
                switch (trans.trans_type){
                    case TransType.TRANS_TYPE_CASH_IN:
                        account.cashInTotal += Math.abs(trans.cash_amount);
                        break;
                    case TransType.TRANS_TYPE_CASH_OUT:
                        account.cashOutTotal += Math.abs(trans.cash_amount);
                        break;

                }

                StockInfo info = StockUtilKt.getStockInfoOrNull(trans.stock_id);
                if(info != null){
                    StockValue stockValue = account.stockValueMap.getOrDefault(trans.stock_id, new StockValue());
                    stockValue.info = info;
                    if(stockValue.lastTransTime < trans.trans_time){
                        stockValue.lastTransTime = trans.trans_time;
                    }
                    account.stockValueMap.put(trans.stock_id, stockValue);
                    if(trans.stock_amount < 0){
                        stockValue.sellAmount += Math.abs(trans.stock_amount);
                        stockValue.sellCost += Math.abs(trans.cash_amount);
                    } else {
                        stockValue.buyAmount += Math.abs(trans.stock_amount);
                        stockValue.buyCost += Math.abs(trans.cash_amount);
                    }

                    switch (trans.trans_type){
                        case TransType.TRANS_TYPE_CASH_DIVIDEND:
                            stockValue.dividendCash += Math.abs(trans.cash_amount);
                            account.dividendCashTotal += stockValue.dividendCash;
                            break;
                        case TransType.TRANS_TYPE_STOCK_DIVIDEND:
                            stockValue.dividendStock += Math.abs(trans.stock_amount);
                            account.dividendStockTotal += stockValue.dividendStock;
                            break;
                        case TransType.TRANS_TYPE_CASH_REDUCTION:
                            stockValue.reductionCash += Math.abs(trans.cash_amount);
                            stockValue.reductionStock += Math.abs(trans.stock_amount);
                            account.reductionCashTotal += stockValue.reductionCash;
                            account.reductionStockTotal += stockValue.reductionStock;
                            break;
                        case TransType.TRANS_TYPE_STOCK_REDUCTION:
                            stockValue.reductionStock += Math.abs(trans.stock_amount);
                            account.reductionStockTotal += stockValue.reductionStock;
                            break;

                    }
                }
            }
            Lock lock = new ReentrantLock();
            lockCount = account.stockValueMap.size();
            for(Map.Entry<String, StockValue> entry: account.stockValueMap.entrySet()) {
                String stockId = entry.getKey();
                StockValue stockValue = entry.getValue();
                stockValue.avgSellPrice = stockValue.sellAmount == 0 ? 0 : 1. * stockValue.sellCost / stockValue.sellAmount;
                stockValue.avgBuyPrice = stockValue.buyAmount == 0 ? 0 : 1. * stockValue.buyCost / stockValue.buyAmount;
                stockValue.holdingAmount = stockValue.buyAmount - stockValue.sellAmount;
                ApiUtil.stockApi.getRegularStockPrice(stockId, info -> {
                    if(info != null){
                        try {
                            stockValue.holdingCalc = (int) Math.floor(stockValue.holdingAmount * info.getLastPrice());
                            stockValue.holdingCost = stockValue.buyCost - stockValue.sellCost;
                            stockValue.holdingProfit = stockValue.holdingCalc - stockValue.holdingCost;
                            stockValue.holdingProfitRate = stockValue.holdingCost == 0 ? 0 : 1. * stockValue.holdingProfit / stockValue.holdingCost;
                            stockValue.historyCost = (int) Math.floor(stockValue.avgBuyPrice * stockValue.sellAmount);
                            stockValue.historyProfit = stockValue.sellCost - stockValue.historyCost;
                            stockValue.historyProfitRate = stockValue.historyCost == 0 ? 0 : 1. * stockValue.historyProfit / stockValue.historyCost;
                            account.stockCostTotal += stockValue.holdingCost;
                            account.stockProfitTotal += stockValue.holdingProfit;
                            account.historyCostTotal += stockValue.historyCost;
                            account.historyProfitTotal += stockValue.historyProfit;
                        } catch (Exception e){
                            Log.e(TAG, info.getStockId() + " get price err: " + e.getMessage());
                        }
                    }
                    if(--lockCount <= 0){
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
            }
            if(lockCount > 0) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            account.stockProfitRate = account.stockCostTotal == 0 ? 0 : 1. * account.stockProfitTotal / account.stockCostTotal;
            account.stockCalcTotal = account.stockCostTotal + account.stockProfitTotal;
            account.accountCalcTotal = account.cashBalance + account.stockCalcTotal;
            account.accountProfitRate = account.cashInTotal == 0 ? 0 : 1. * account.stockProfitTotal / account.cashInTotal;
            account.accountTotal = account.cashBalance + account.stockCostTotal;
            account.historyProfitRate = account.historyCostTotal == 0 ? 0 : 1. * account.historyProfitTotal / account.historyCostTotal;
            hasValue = true;
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            for(AccountUpdateListener listener: mListenerList){
                listener.onUpdate(account);
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
        onDataChanged();
    }

    public static void close(){
        mContext = null;
        ApiUtil.transApi.removeTransUpdateListener(transUpdateListener);
        if(onDataChangedTask != null && onDataChangedTask.getStatus() == AsyncTask.Status.RUNNING){
            onDataChangedTask.cancel(true);
        }
    }

    public static AccountValue getAccount(){
        return hasValue ? account : null;
    }

    public static boolean addListener(AccountUpdateListener listener){
        if(hasValue) listener.onUpdate(account);
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
        public double accountProfitRate;
        public int stockCostTotal;
        public int stockCalcTotal;
        public int stockProfitTotal;
        public double stockProfitRate;
        public int historyCostTotal;
        public int historyProfitTotal;
        public double historyProfitRate;
        public int cashBalance;
        public int cashInTotal;
        public int cashOutTotal;
        public int dividendCashTotal;
        public int dividendStockTotal;
        public int reductionCashTotal;
        public int reductionStockTotal;

        public Map<String, StockValue> stockValueMap = new HashMap<>();
        public void reset(){
            accountCalcTotal = 0;
            accountTotal = 0;
            accountProfitRate = 0.;
            stockCostTotal = 0;
            stockCalcTotal = 0;
            stockProfitTotal = 0;
            stockProfitRate = 0.;
            historyCostTotal = 0;
            historyProfitTotal = 0;
            historyProfitRate = 0.;
            cashBalance = 0;
            cashInTotal = 0;
            cashOutTotal = 0;
            dividendCashTotal = 0;
            dividendStockTotal = 0;
            reductionCashTotal = 0;
            reductionStockTotal = 0;
            stockValueMap.clear();
        }
    }

    public static class StockValue{
        public StockInfo info;
        public int holdingAmount;
        public int holdingCost;
        public int holdingCalc;
        public int holdingProfit;
        public double holdingProfitRate;
        public int historyCost;
        public int historyProfit;
        public double historyProfitRate;
        public int buyAmount;
        public int buyCost;
        public double avgBuyPrice;
        public int sellAmount;
        public int sellCost;
        public double avgSellPrice;
        public long lastTransTime;
        public int dividendCash;
        public int dividendStock;
        public int reductionCash;
        public int reductionStock;
    }
}
