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
        return cashIn(date, amount, null);
    }

    public static SimulatedTransRecord cashIn(String date, int amount, String remarkSuffix) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_IN, null, 0, 0, amount, remarkSuffix);
    }

    public static SimulatedTransRecord cashOut(String date, int amount) {
        return cashOut(date, amount, null);
    }

    public static SimulatedTransRecord cashOut(String date, int amount, String remarkSuffix) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_OUT, null, 0, 0, amount, remarkSuffix);
    }

    public static SimulatedTransRecord stockBuy(String date, String stockId, int shares, double price) {
        return stockBuy(date, stockId, shares, price, null);
    }

    public static SimulatedTransRecord stockBuy(
            String date, String stockId, int shares, double price, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_BUY, stockId, shares, price, 0, remarkSuffix);
    }

    public static SimulatedTransRecord stockSell(String date, String stockId, int shares, double price) {
        return stockSell(date, stockId, shares, price, null);
    }

    public static SimulatedTransRecord stockSell(
            String date, String stockId, int shares, double price, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_SELL, stockId, shares, price, 0, remarkSuffix);
    }

    public static SimulatedTransRecord cashDividend(String date, String stockId, int amount) {
        return cashDividend(date, stockId, amount, null);
    }

    public static SimulatedTransRecord cashDividend(
            String date, String stockId, int amount, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.CASH_DIVIDEND, stockId, 0, 0, amount, remarkSuffix);
    }

    public static SimulatedTransRecord stockDividend(String date, String stockId, int shares) {
        return stockDividend(date, stockId, shares, null);
    }

    public static SimulatedTransRecord stockDividend(
            String date, String stockId, int shares, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_DIVIDEND, stockId, shares, 0, 0, remarkSuffix);
    }

    public static SimulatedTransRecord stockReduction(String date, String stockId, int sharesReduced) {
        return stockReduction(date, stockId, sharesReduced, null);
    }

    public static SimulatedTransRecord stockReduction(
            String date, String stockId, int sharesReduced, String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_REDUCTION, stockId, sharesReduced, 0, 0, remarkSuffix);
    }

    /** @param refundPerShare 每股退還現金（元） */
    public static SimulatedTransRecord cashReduction(
            String date, String stockId, int sharesReduced, double refundPerShare, String remarkSuffix) {
        int cash = (int) Math.floor(refundPerShare * sharesReduced);
        return new SimulatedTransRecord(
                date,
                SimulatedTransKind.CASH_REDUCTION,
                stockId,
                sharesReduced,
                refundPerShare,
                cash,
                remarkSuffix);
    }
}
