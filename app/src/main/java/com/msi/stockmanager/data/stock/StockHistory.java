package com.msi.stockmanager.data.stock;

public class StockHistory {
    public String stock_id;
    public long date_timestamp;
    public double price_open;
    public double price_close;
    public double price_high;
    public double price_low;
    /** 成交量，單位：張（1 張 = 1,000 股）。 */
    public double price_volume;
}
