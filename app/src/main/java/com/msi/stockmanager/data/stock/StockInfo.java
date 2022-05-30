package com.msi.stockmanager.data.stock;

import androidx.annotation.NonNull;

import com.msi.stockmanager.components.autocomplete.AutoCompleteEntity;
import com.msi.stockmanager.data.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class StockInfo implements AutoCompleteEntity {
    public String stock_id = "";
    public String stock_name = "";
    public double last_price = 0;
    public long last_update_time = 0;

    public StockInfo(){}

    public StockInfo(String id, String name){
        stock_id = id;
        stock_name = name;
    }

    public String getStockNameWithId(){
        return String.format("%s - %s", stock_id, stock_name);
    }

    @Override
    public String toString(){
        return String.format("stock_id %s, stock_name %s, last_price %.2f, last_update_time %s"
                , stock_id, stock_name, last_price, DateUtil.toDateTimeString(last_update_time));
    }

    @Override
    public boolean filter(@NonNull String query) {
        return stock_id.contains(query) || stock_name.contains(query);
    }
}
