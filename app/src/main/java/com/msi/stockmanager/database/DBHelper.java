package com.msi.stockmanager.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.msi.stockmanager.data.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();
    private final static int _DBVersion = 2;
    private final static String _DBName = "stockAccounting.db";

    public DBHelper(@Nullable Context context) {
        super(context, _DBName, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "+++ onCreate +++");
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
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        Log.d(TAG, "!!! onUpgrade: " + oldVersion + " -> " + newVersion + " !!!");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DBDefine.TB_TransactionRecord.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
