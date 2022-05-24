package com.msi.stockmanager.database;

import android.provider.BaseColumns;
public final class DBDefine {
    public static class TB_TransactionRecord implements BaseColumns {
        public static final String TABLE_NAME = "transaction_record";

        public static final String COLUMN_NAME_STOCK_NAME = "tr_stock_name"; //股票名稱
        public static final String COLUMN_NAME_STOCK_CODE = "tr_stock_code"; //股票代號
        public static final String COLUMN_NAME_TRANSACTION_TYPE = "tr_transaction_type"; //交易類別
        public static final String COLUMN_NAME_TRANSACTION_TYPE_OTHER_DESCRIPTION = "tr_transaction_type_other_desc";
        public static final String COLUMN_NAME_TRANSACTION_TIME = "tr_transaction_time"; //交易時間
        public static final String COLUMN_NAME_STOCK_AMOUNT = "stock_amount"; //股數
        public static final String COLUMN_NAME_CASH_AMOUNT = "cash_amount"; //現金轉入(正值), 轉出(負值)
        public static final String COLUMN_NAME_FEE = "tr_fee"; //手續費
        public static final String COLUMN_NAME_TAX = "tr_tax"; //證交稅
        public static final String COLUMN_NAME_CREATE_TIME = "create_time"; //此筆資料創建時間
        public static final String COLUMN_NAME_REMARK = "tr_remark"; //備註
    }

}
