package com.msi.stockmanager.data.transaction;

public interface TransType {
    int TRANS_TYPE_STOCK_BUY = 0; //股票買入
    int TRANS_TYPE_STOCK_SELL = 1; //股票賣出
    int TRANS_TYPE_CASH_IN = 2; //帳戶現金轉入
    int TRANS_TYPE_CASH_OUT = 3; //帳戶現金轉出
    int TRANS_TYPE_CASH_DIVIDEND = 4; //現金股利 (股息)
    int TRANS_TYPE_STOCK_DIVIDEND = 5; //股票股利
    int TRANS_TYPE_CASH_REDUCTION = 6; //現金減資 (股數減少，股價調高，發放現金)
    int TRANS_TYPE_STOCK_REDUCTION = 7; //股票減資 (股數減少，股價調高)
    int TRANS_TYPE_OTHER = -1;
}