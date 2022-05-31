package com.msi.stockmanager.data.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.database.DBDefine;
import com.msi.stockmanager.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class TransApi implements ITransApi{
    private final static String TAG = TransApi.class.getSimpleName();
    private List<Transaction> history_trans_list = new ArrayList<>();
    private DBHelper dbHelper = null;
    public TransApi(Context context){
        dbHelper = new DBHelper(context);
    }

    @Override
    public List<String> getHoldingStockList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        String[] projection = {
                DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE
        };
        String groupByRows = DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE;
        String orderBy = DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE;

        Cursor cursor = db.query(
                DBDefine.TB_TransactionRecord.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                groupByRows,                   // group the rows
                null,                   // don't filter by row groups
                orderBy               // The sort order
        );

        List holdingStockList = new ArrayList<String>();
        while(cursor.moveToNext()) {
            holdingStockList.add(cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE)));
        }

        return holdingStockList;
    }

    @Override
    public List<Transaction> getHistoryTransList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DBDefine.TB_TransactionRecord._ID + " ASC";

        Cursor cursor = db.query(
                DBDefine.TB_TransactionRecord.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List transList = new ArrayList<Transaction>();
        while(cursor.moveToNext()) {
            Transaction trans = new Transaction();
            trans.trans_id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord._ID));
            trans.stock_name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_NAME));
            trans.stock_id = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE));
            trans.trans_type = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE));
            trans.trans_type_other_desc = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE_OTHER_DESCRIPTION));
            trans.trans_time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME));
            trans.stock_amount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_AMOUNT));
            trans.cash_amount = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT));
            trans.fee = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE));
            trans.tax = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX));
            trans.create_time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME));
            trans.remark = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK));

            transList.add(trans);
        }
        cursor.close();

        return transList;
    }

    @Override
    public long addTrans(Transaction trans) {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_NAME, trans.stock_name);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE, trans.stock_id);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE, trans.trans_type);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE_OTHER_DESCRIPTION,
                trans.trans_type_other_desc);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME, trans.trans_time);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_AMOUNT, trans.stock_amount);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT, trans.cash_amount);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE, trans.fee);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX, trans.tax);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK, trans.remark);

        // Insert the new row, returning the primary key value of the new row
        long trans_id = db.insert(DBDefine.TB_TransactionRecord.TABLE_NAME, null, values);
        // if insert failed, will return -1
        return trans_id;
    }

    @Override
    public boolean updateTrans(long trans_id, Transaction trans) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_NAME, trans.stock_name);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE, trans.stock_id);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE, trans.trans_type);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE_OTHER_DESCRIPTION,
                trans.trans_type_other_desc);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME, trans.trans_time);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_AMOUNT, trans.stock_amount);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT, trans.cash_amount);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE, trans.fee);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX, trans.tax);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK, trans.remark);

        // Which row to update, based on the id
        String selection = DBDefine.TB_TransactionRecord._ID + " = ?";
        String[] selectionArgs = { String.valueOf(trans_id) };

        int result = db.update(
                        DBDefine.TB_TransactionRecord.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
        if (result == 0){
            return false;
        }
        return true;
    }

    @Override
    public boolean removeTrans(long trans_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = DBDefine.TB_TransactionRecord._ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(trans_id) };
        // Issue SQL statement.
        int deletedRows = db.delete(DBDefine.TB_TransactionRecord.TABLE_NAME, selection, selectionArgs);
        if (deletedRows <= 0){
            return false;
        }
        return true;
    }
}
