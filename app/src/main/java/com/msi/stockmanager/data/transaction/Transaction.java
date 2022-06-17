package com.msi.stockmanager.data.transaction;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.profile.Profile;

import java.io.Serializable;

public class Transaction implements Serializable {
    public long trans_id = -1;
    public long trans_time = System.currentTimeMillis(); //timestamp
    public int trans_type = TransType.TRANS_TYPE_OTHER; //interface TransactionType
    public String trans_type_other_desc = "";
    public String stock_id = "";
    public String stock_name = "";
    public int stock_amount = 0; //正負值, unit: 零股, 正數表示股數增加, 負數表示股數減少
    public double stock_price = 0; //正值, unit: 新台幣, 股票價格
    public int cash_amount = 0; //正負值, unit: 新台幣, 正數表示現金轉入, 負數表示現金轉出
    public int fee = 0; //手續費
    public int tax = 0; //證交稅
    public String remark = "";
    public long create_time = 0; //timestamp

    public Transaction(){
        super();
    }

    public Transaction(int type){
        super();
        trans_type = type;
        switch (type){
            case TransType.TRANS_TYPE_STOCK_BUY:
            case TransType.TRANS_TYPE_STOCK_SELL:
                stock_amount = 1000;
                fee = Profile.fee_minimum;
                break;
        }
    }

    @Override
    public String toString(){
        return String.format("=== trans_id: %d ===\ntrans_time: %s\ntrans_type: %d\ntrans_type_other_desc: %s\n"
                + "stock_id: %s\nstock_name: %s\nstock_amount: %d\nstock_price: %.2f\ncash_amount: %d\nfee: %d\ntax: %d\nremark: %s\n"
        , trans_id, DateUtil.toDateTimeString(trans_time), trans_type, trans_type_other_desc
        , stock_id, stock_name, stock_amount, stock_price, cash_amount, fee, tax, remark);
    }

    public boolean isIdValid(){
        return trans_id >= 0;
    }

}
