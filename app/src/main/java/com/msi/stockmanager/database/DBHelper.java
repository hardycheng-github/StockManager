package com.msi.stockmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1;
    private final static String _DBName = "stockAccounting.db";

    public DBHelper(@Nullable Context context) {
        super(context, _DBName, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + DBDefine.TB_TransactionRecord.TABLE_NAME + "( " +
                DBDefine.TB_TransactionRecord._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_NAME + " TEXT , " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE + " TEXT, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE + " TEXT, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE_OTHER_DESCRIPTION + " TEXT, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME + " INTEGER, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_AMOUNT + " INTEGER, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT + " REAL, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE + " REAL, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX + " REAL, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME + " INTEGER, " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK + " TEXT " +
                ");";
        sqLiteDatabase.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //For version 2
        if (newVersion > 1){
            //do something for version 2 and later.
        }
    }
}
