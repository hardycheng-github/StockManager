package com.msi.stockmanager.data.demo;

import com.msi.stockmanager.data.transaction.TransType;

public enum SimulatedTransKind {
    CASH_IN(TransType.TRANS_TYPE_CASH_IN),
    CASH_OUT(TransType.TRANS_TYPE_CASH_OUT),
    STOCK_BUY(TransType.TRANS_TYPE_STOCK_BUY),
    STOCK_SELL(TransType.TRANS_TYPE_STOCK_SELL),
    CASH_DIVIDEND(TransType.TRANS_TYPE_CASH_DIVIDEND);

    public final int transType;

    SimulatedTransKind(int transType) {
        this.transType = transType;
    }
}
