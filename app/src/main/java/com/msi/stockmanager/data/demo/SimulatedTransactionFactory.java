package com.msi.stockmanager.data.demo;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.data.transaction.Transaction;

public final class SimulatedTransactionFactory {

    private static final String REMARK_PREFIX = "【模擬】";

    private SimulatedTransactionFactory() {}

    public static Transaction build(SimulatedTransRecord record) {
        Transaction trans = new Transaction(record.kind.transType);
        trans.trans_time = DateUtil.parseDate(record.date);
        trans.trans_type_other_desc = "";
        trans.stock_id = record.stockId != null ? record.stockId : "";
        trans.stock_name = resolveStockName(trans.stock_id);
        trans.remark = buildRemark(record);

        switch (record.kind) {
            case CASH_IN:
                trans.stock_amount = 0;
                trans.stock_price = 0;
                trans.cash_amount = Math.abs(record.cashAmount);
                trans.fee = 0;
                trans.tax = 0;
                break;
            case CASH_OUT:
                trans.stock_amount = 0;
                trans.stock_price = 0;
                trans.cash_amount = -Math.abs(record.cashAmount);
                trans.fee = 0;
                trans.tax = 0;
                break;
            case STOCK_BUY:
                trans.stock_amount = Math.abs(record.stockAmount);
                trans.stock_price = record.stockPrice;
                trans.fee = calcFee(record.stockPrice, record.stockAmount);
                trans.tax = 0;
                trans.cash_amount = -(
                        (int) Math.floor(record.stockPrice * record.stockAmount) + trans.fee);
                break;
            case STOCK_SELL:
                trans.stock_amount = -Math.abs(record.stockAmount);
                trans.stock_price = record.stockPrice;
                trans.fee = calcFee(record.stockPrice, record.stockAmount);
                trans.tax = calcTax(record.stockPrice, record.stockAmount);
                int sellProceeds = (int) Math.floor(record.stockPrice * record.stockAmount);
                trans.cash_amount = sellProceeds - trans.fee - trans.tax;
                break;
            case CASH_DIVIDEND:
                trans.stock_amount = 0;
                trans.stock_price = 0;
                trans.cash_amount = Math.abs(record.cashAmount);
                trans.fee = 0;
                trans.tax = 0;
                break;
            case STOCK_DIVIDEND:
                trans.stock_amount = Math.abs(record.stockAmount);
                trans.stock_price = 0;
                trans.cash_amount = 0;
                trans.fee = 0;
                trans.tax = 0;
                break;
            case STOCK_REDUCTION:
                trans.stock_amount = -Math.abs(record.stockAmount);
                trans.stock_price = 0;
                trans.cash_amount = 0;
                trans.fee = 0;
                trans.tax = 0;
                break;
            case CASH_REDUCTION:
                trans.stock_amount = -Math.abs(record.stockAmount);
                trans.stock_price = record.stockPrice;
                trans.cash_amount = Math.abs(record.cashAmount);
                trans.fee = 0;
                trans.tax = 0;
                break;
            default:
                break;
        }
        return trans;
    }

    private static String resolveStockName(String stockId) {
        if (stockId == null || stockId.isEmpty()) {
            return "";
        }
        StockInfo info = StockUtilKt.getStockInfoOrNull(stockId);
        return info != null ? info.getStockName() : "";
    }

    private static String buildRemark(SimulatedTransRecord record) {
        if (record.remarkSuffix != null && !record.remarkSuffix.isEmpty()) {
            return REMARK_PREFIX + record.remarkSuffix;
        }
        return REMARK_PREFIX;
    }

    private static int calcFee(double price, int shares) {
        int fee = (int) Math.floor(
                price * Profile.fee_rate * Profile.fee_discount * shares);
        return Math.max(fee, Profile.fee_minimum);
    }

    private static int calcTax(double price, int shares) {
        return (int) Math.floor(price * Profile.tax_rate * shares);
    }
}
