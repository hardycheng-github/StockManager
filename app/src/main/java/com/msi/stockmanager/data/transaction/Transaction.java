package com.msi.stockmanager.data.transaction;

import com.msi.stockmanager.data.DateUtil;

public class Transaction {
    public long trans_id = -1;
    public long trans_time = 0; //timestamp
    public int trans_type = TransType.TRANS_TYPE_OTHER; //interface TransactionType
    public String trans_type_other_desc = "";
    public String stock_id = "";
    public String stock_name = "";
    public int stock_amount = 0; //正負值, unit: 零股, 正數表示股數增加, 負數表示股數減少
    public double cash_amount = 0; //正負值, unit: 新台幣, 正數表示現金轉入, 負數表示現金轉出
    public double fee = 0; //手續費
    public double tax = 0; //證交稅
    public String remark = "";
    public long create_time = 0; //timestamp

    @Override
    public String toString(){
        return String.format("trans_id %d, trans_time %s, trans_type %d, trans_type_other_desc %s, "
                + "stock_id %s, stock_name %s, stock_amount %d, cash_amount %.2f, fee %.2f, tax %.2f, remark %s"
        , trans_id, DateUtil.toDateTimeString(trans_time), trans_type, trans_type_other_desc
        , stock_id, stock_name, stock_amount, cash_amount, fee, tax, remark);
    }
}
