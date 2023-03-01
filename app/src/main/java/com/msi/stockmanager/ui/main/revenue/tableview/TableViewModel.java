/*
 * MIT License
 *
 * Copyright (c) 2021 Evren Coşkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.msi.stockmanager.ui.main.revenue.tableview;

import android.content.Context;

import androidx.annotation.NonNull;

import com.evrencoskun.tableview.listener.ITableViewListener;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.revenue.RevenueInfo;
import com.msi.stockmanager.data.stock.MyStockUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;
import com.msi.stockmanager.ui.main.revenue.RevenueFilterUtil;
import com.msi.stockmanager.ui.main.revenue.tableview.model.Cell;
import com.msi.stockmanager.ui.main.revenue.tableview.model.ColumnHeader;
import com.msi.stockmanager.ui.main.revenue.tableview.model.RowHeader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by evrencoskun on 4.02.2018.
 */

public class TableViewModel {

    private static int[] headerResidList = new int[]{
//            R.string.revenue_table_header_stock_id,
//            R.string.revenue_table_header_company_name,
            R.string.revenue_table_header_company_type,
            R.string.revenue_table_header_revenue_this_month,
            R.string.revenue_table_header_revenue_last_month,
            R.string.revenue_table_header_revenue_mom,
            R.string.revenue_table_header_revenue_last_year,
            R.string.revenue_table_header_revenue_yoy,
            R.string.revenue_table_header_revenue_this_ytd,
            R.string.revenue_table_header_revenue_last_ytd,
            R.string.revenue_table_header_revenue_yoy_ytd,
    };

    private Context mContext;

    public IRevenueTableListener listener;

    public RevenueFilterUtil filterUtil;

    public boolean isHiddenColumn(int col){
        List<ColumnHeader> ch = getColumnHeaderList();
        if(ch.size() > col){
            return filterUtil.getHiddenItems().contains(ch.get(col).getId());
        }
        return false;
    }

    public TableViewModel(Context context, RevenueFilterUtil util) {
        mContext = context;
        filterUtil = util;
    }

    public void setListener(IRevenueTableListener listener) {
        this.listener = listener;
    }

    @NonNull
    public List<List<Cell>> getCellList(int year, int month) {
        List<ColumnHeader> cols = getColumnHeaderList();
        List<RowHeader> rows = getRowHeaderList();
        List<List<Cell>> cells = new ArrayList<>();
        for(RowHeader rh: rows){
            String stockId = rh.getId();
            RevenueInfo stockInfo = ApiUtil.revenueApi.getRevenueInfo(stockId, year, month);
            StockInfo stockInfo1 = StockUtilKt.getStockInfoOrNull(stockId);
            List<Cell> row = new ArrayList<>();
            for(ColumnHeader ch: cols){
                int headerResId = Integer.parseInt(ch.getId());
                Object data = "";
                switch (headerResId){
                    case R.string.revenue_table_header_stock_id:{
                        data = stockId;
                        break;
                    }
                    case R.string.revenue_table_header_company_name:{
                        data = stockInfo != null ? stockInfo.getCompanyName()
                                : stockInfo1 != null ? stockInfo1.getStockName() : "";
                        break;
                    }
                    case R.string.revenue_table_header_company_type:{
                        data = stockInfo != null ? stockInfo.getCompanyType()
                                : stockInfo1 != null ? stockInfo1.getCompanyType() : "";
                        break;
                    }
                    case R.string.revenue_table_header_revenue_this_month:{
                        data = stockInfo != null ? stockInfo.getRevenueThisMonth() : 0L;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_last_month:{
                        data = stockInfo != null ? stockInfo.getRevenueLastMonth() : 0L;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_last_year:{
                        data = stockInfo != null ? stockInfo.getRevenueLastYearSameMonth() : 0L;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_mom:{
                        data = stockInfo != null ? stockInfo.getRevenueMoM() : 0f;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_yoy:{
                        data = stockInfo != null ? stockInfo.getRevenueYoY() : 0f;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_this_ytd:{
                        data = stockInfo != null ? stockInfo.getRevenueYtdThisYear() : 0L;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_last_ytd:{
                        data = stockInfo != null ? stockInfo.getRevenueYtdLastYear() : 0L;
                        break;
                    }
                    case R.string.revenue_table_header_revenue_yoy_ytd:{
                        data = stockInfo != null ? stockInfo.getRevenueYtdYoY() : 0f;
                        break;
                    }
                }
                row.add(new Cell(rh.getId()+"___"+ch.getId(), data));
            }
            cells.add(row);
        }
        return cells;
    }

    @NonNull
    public List<RowHeader> getRowHeaderList() {
        List<RowHeader> list = new ArrayList<>();
        for(String stockId: ApiUtil.revenueApi.getWatchingList()){
            StockInfo info = StockUtilKt.getStockInfoOrNull(stockId);
            String stockIdWithName = info != null ? stockId + "\n" + info.getStockName() : stockId;
            list.add(new RowHeader(stockId, stockIdWithName));
        }
        Comparator<RowHeader> comp = Comparator.comparing(RowHeader::getId);
        list.sort(filterUtil.getSortingAscending() ? comp : comp.reversed());
        return list;
    }

    @NonNull
    public List<ColumnHeader> getColumnHeaderList() {
        List<ColumnHeader> list = new ArrayList<>();
        for(int rid: headerResidList){
            list.add(new ColumnHeader(String.valueOf(rid), mContext.getString(rid)));
        }
        return list;
    }
}
