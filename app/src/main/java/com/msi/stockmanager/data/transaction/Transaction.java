package com.msi.stockmanager.data.transaction;

import com.msi.stockmanager.data.DateUtil;

public class Transaction {
    long trans_id = -1;
    long trans_time = 0; //timestamp
    int trans_type = TransType.TRANS_TYPE_OTHER; //interface TransactionType
    String trans_type_other_desc = "";
    String stock_id = "";
    int stock_amount = 0; //正負值, unit: 零股, 正數表示股數增加, 負數表示股數減少
    double cash_amount = 0; //正負值, unit: 新台幣, 正數表示現金轉入, 負數表示現金轉出
    double fee = 0; //手續費
    double tax = 0; //證交稅
    String remark = "";

    @Override
    public String toString(){
        return String.format("trans_id %d, trans_time %s, trans_type %d, trans_type_other_desc %s, "
                + "stock_id %s, stock_amount %d, cash_amount %.2f, fee %.2f, tax %.2f, remark %s"
        , trans_id, DateUtil.toDateTimeString(trans_time), trans_type, trans_type_other_desc
        , stock_id, stock_amount, cash_amount, fee, tax, remark);
    }
}
