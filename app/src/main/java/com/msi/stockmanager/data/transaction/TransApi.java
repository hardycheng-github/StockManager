package com.msi.stockmanager.data.transaction;

import android.content.Context;

import com.msi.stockmanager.data.profile.Profile;

import java.util.ArrayList;
import java.util.List;

public class TransApi implements ITransApi{
    private final static String TAG = TransApi.class.getSimpleName();
    private List<Transaction> history_trans_list = new ArrayList<>();

    public TransApi(Context context){

    }

    @Override
    public List<String> getHoldingStockList() {
        return null;
    }

    @Override
    public List<Transaction> getHistoryTransList() {
        return null;
    }

    @Override
    public long addTrans(Transaction trans) {
        return 0;
    }

    @Override
    public boolean updateTrans(long trans_id, Transaction trans) {
        return false;
    }

    @Override
    public boolean removeTrans(long trans_id) {
        return false;
    }
}
