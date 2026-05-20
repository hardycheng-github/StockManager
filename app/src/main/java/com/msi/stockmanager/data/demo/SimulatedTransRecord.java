package com.msi.stockmanager.data.demo;

import androidx.annotation.Nullable;

public final class SimulatedTransRecord {

    public final String date;
    public final SimulatedTransKind kind;
    @Nullable public final String stockId;
    public final int stockAmount;
    public final double stockPrice;
    public final int cashAmount;
    @Nullable public final String remarkSuffix;

    private SimulatedTransRecord(
            String date,
            SimulatedTransKind kind,
            @Nullable String stockId,
            int stockAmount,
            double stockPrice,
            int cashAmount,
            @Nullable String remarkSuffix
    ) {
        this.date = date;
        this.kind = kind;
        this.stockId = stockId;
        this.stockAmount = stockAmount;
        this.stockPrice = stockPrice;
        this.cashAmount = cashAmount;
        this.remarkSuffix = remarkSuffix;
    }

    public static SimulatedTransRecord cashIn(String date, int amount) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_IN, null, 0, 0, amount, null);
    }

    public static SimulatedTransRecord cashOut(String date, int amount) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_OUT, null, 0, 0, amount, null);
    }

    public static SimulatedTransRecord stockBuy(String date, String stockId, int shares, double price) {
        return new SimulatedTransRecord(date, SimulatedTransKind.STOCK_BUY, stockId, shares, price, 0, null);
    }

    public static SimulatedTransRecord stockSell(String date, String stockId, int shares, double price) {
        return new SimulatedTransRecord(date, SimulatedTransKind.STOCK_SELL, stockId, shares, price, 0, null);
    }

    public static SimulatedTransRecord cashDividend(String date, String stockId, int amount) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.CASH_DIVIDEND, stockId, 0, 0, amount, null);
    }

    public static SimulatedTransRecord cashDividend(
            String date, String stockId, int amount, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.CASH_DIVIDEND, stockId, 0, 0, amount, remarkSuffix);
    }
}
