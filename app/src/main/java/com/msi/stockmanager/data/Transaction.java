package com.msi.stockmanager.data;

public class Transaction {
    long trans_id = -1;
    long trans_time = 0; //timestamp
    int trans_type = TransType.TRANS_TYPE_OTHER; //interface TransactionType
    String trans_type_other_desc = "";
    String stock_id = "";
    int stock_amount = 0; //正負值, unit: 零股, 正數表示股數增加, 負數表示股數減少
    double cash_amount = 0; //正負值, unit: 新台幣, 正數表示現金轉入, 負數表示現金轉出
    double fee = 0; //手續費
    double discount = 0; //折扣
    String remark = "";
}
