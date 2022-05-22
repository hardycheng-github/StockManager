package com.msi.stockmanager.data.transaction;

import com.msi.stockmanager.data.Param;

import java.util.ArrayList;
import java.util.List;

public class TransApi implements ITransApi{

    private List<Transaction> history_trans_list = new ArrayList<>();

    @Override
    public List<String> getHoldingStockList() {
        List<String> holding_stock_list = new ArrayList<>();
        for(Transaction trans: getHistoryTransList()){
            if(!holding_stock_list.contains(trans.stock_id)){
                holding_stock_list.add(trans.stock_id);
            }
        }
        return holding_stock_list;
    }

    @Override
    public List<Transaction> getHistoryTransList() {
        return history_trans_list;
    }

    @Override
    public long addTrans(Transaction trans) {
        trans = FakeDataGenerator.getRandomTransaction();
        history_trans_list.add(trans);
        return trans.trans_id;
    }

    @Override
    public boolean updateTrans(long trans_id, Transaction trans) {
        trans_id = (long) Math.random() * history_trans_list.size();
        for(int i = 0; i < history_trans_list.size(); i++){
            Transaction t = history_trans_list.get(i);
            if(t.trans_id == trans_id){
                trans = FakeDataGenerator.getRandomTransaction();
                trans.trans_id = trans_id;
                history_trans_list.set(i, trans);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeTrans(long trans_id) {
        trans_id = (long) Math.random() * history_trans_list.size();
        for(int i = 0; i < history_trans_list.size(); i++){
            Transaction t = history_trans_list.get(i);
            if(t.trans_id == trans_id){
                history_trans_list.remove(i);
                return true;
            }
        }
        return false;
    }

    //TODO 開始實作API時請將FakeDataGenerator移除
    static class FakeDataGenerator {

        static final String[] sample_stock_id_array = new String[]{"1101", "1102", "2330", "0050", "0056"};
        static long trans_id = 0;

        static double getRandomPrice(){
            return Math.random() * 100;
        }

        static long getRandomDateTime(){
            return System.currentTimeMillis() - (long)(Math.random()*30*24*60*60*1000);
        }

        static List<Transaction> getRandomTransList(){
            List<Transaction> trans_list = new ArrayList<>();
            int size = (int)(Math.random()*100);
            for(int i = 0; i < size; i++){
                trans_list.add(getRandomTransaction());
            }
            return trans_list;
        }

        static String getRandomStockId(){
            return sample_stock_id_array[(int)(Math.random()*sample_stock_id_array.length)];
        }

        static Transaction getRandomTransaction(){
            Transaction trans = new Transaction();
            trans.trans_id = getRandomTransId();
            trans.trans_time = getRandomDateTime();
            trans.trans_type = (int)(Math.random()*9)-1;
            switch (trans.trans_type){
                case TransType.TRANS_TYPE_STOCK_BUY:
                    trans.stock_id = getRandomStockId();
                    trans.stock_amount = (int) (Math.random()*10000);
                    trans.cash_amount = -getRandomPrice()*trans.stock_amount;
                    trans.fee = Math.abs(trans.cash_amount) * Param.fee_rate * Param.fee_discount;
                    trans.fee = Double.max(trans.fee, Param.fee_minimum);
                    break;
                case TransType.TRANS_TYPE_STOCK_SELL:
                    trans.stock_id = getRandomStockId();
                    trans.stock_amount = -(int) (Math.random()*10000);
                    trans.cash_amount = -getRandomPrice()*trans.stock_amount;
                    trans.fee = Math.abs(trans.cash_amount) * Param.fee_rate * Param.fee_discount;
                    trans.fee = Double.max(trans.fee, Param.fee_minimum);
                    trans.tax = Math.abs(trans.cash_amount) * Param.tax_rate;
                    break;
                case TransType.TRANS_TYPE_CASH_IN:
                    trans.cash_amount = Math.random()*100000;
                    break;
                case TransType.TRANS_TYPE_CASH_OUT:
                    trans.cash_amount = -Math.random()*100000;
                    break;
                case TransType.TRANS_TYPE_CASH_DIVIDEND:
                    trans.stock_id = getRandomStockId();
                    trans.cash_amount = Math.random()*1000;
                    break;
                case TransType.TRANS_TYPE_STOCK_DIVIDEND:
                    trans.stock_id = getRandomStockId();
                    trans.stock_amount = (int)Math.random()*100;
                    break;
                case TransType.TRANS_TYPE_CASH_REDUCTION:
                    trans.stock_id = getRandomStockId();
                    trans.stock_amount = -(int) (Math.random()*100);
                    trans.cash_amount = -getRandomPrice()*trans.stock_amount;
                    break;
                case TransType.TRANS_TYPE_STOCK_REDUCTION:
                    trans.stock_id = getRandomStockId();
                    trans.stock_amount = -(int) (Math.random()*100);
                    break;
            }
            return trans;
        }

        static long getRandomTransId(){
            return trans_id++;
        }
    }
}
