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

    public static SimulatedTransRecord cashIn(String date, int amount, @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_IN, null, 0, 0, amount, remarkSuffix);
    }

    public static SimulatedTransRecord cashOut(String date, int amount) {
        return cashOut(date, amount, null);
    }

    public static SimulatedTransRecord cashOut(String date, int amount, @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(date, SimulatedTransKind.CASH_OUT, null, 0, 0, amount, remarkSuffix);
    }

    /** {@code netCashOut} 為含稅費的現金支出絕對值；傳 0 則由 factory 依費率計算。 */
    public static SimulatedTransRecord stockBuy(
            String date, String stockId, int shares, double price, int netCashOut) {
        return stockBuy(date, stockId, shares, price, netCashOut, null);
    }

    public static SimulatedTransRecord stockBuy(
            String date, String stockId, int shares, double price, int netCashOut,
            @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_BUY, stockId, shares, price, netCashOut, remarkSuffix);
    }

    /** {@code netCashIn} 為含稅費的現金收入絕對值；傳 0 則由 factory 依費率計算。 */
    public static SimulatedTransRecord stockSell(
            String date, String stockId, int shares, double price, int netCashIn) {
        return stockSell(date, stockId, shares, price, netCashIn, null);
    }

    public static SimulatedTransRecord stockSell(
            String date, String stockId, int shares, double price, int netCashIn,
            @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_SELL, stockId, shares, price, netCashIn, remarkSuffix);
    }

    public static SimulatedTransRecord cashDividend(String date, String stockId, int amount) {
        return cashDividend(date, stockId, amount, null);
    }

    public static SimulatedTransRecord cashDividend(
            String date, String stockId, int amount, @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.CASH_DIVIDEND, stockId, 0, 0, amount, remarkSuffix);
    }

    public static SimulatedTransRecord stockDividend(String date, String stockId, int shares) {
        return stockDividend(date, stockId, shares, null);
    }

    public static SimulatedTransRecord stockDividend(
            String date, String stockId, int shares, @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_DIVIDEND, stockId, shares, 0, 0, remarkSuffix);
    }

    public static SimulatedTransRecord stockReduction(String date, String stockId, int sharesLost) {
        return stockReduction(date, stockId, sharesLost, null);
    }

    public static SimulatedTransRecord stockReduction(
            String date, String stockId, int sharesLost, @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.STOCK_REDUCTION, stockId, sharesLost, 0, 0, remarkSuffix);
    }

    public static SimulatedTransRecord cashReduction(
            String date, String stockId, int sharesLost, double pricePerShare, int cashReceived) {
        return cashReduction(date, stockId, sharesLost, pricePerShare, cashReceived, null);
    }

    public static SimulatedTransRecord cashReduction(
            String date, String stockId, int sharesLost, double pricePerShare, int cashReceived,
            @Nullable String remarkSuffix) {
        return new SimulatedTransRecord(
                date, SimulatedTransKind.CASH_REDUCTION, stockId, sharesLost, pricePerShare,
                cashReceived, remarkSuffix);
    }
}
