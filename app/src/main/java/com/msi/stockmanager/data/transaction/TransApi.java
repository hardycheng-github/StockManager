package com.msi.stockmanager.data.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.profile.Profile;
import com.msi.stockmanager.database.DBDefine;
import com.msi.stockmanager.database.DBHelper;
import com.msi.stockmanager.ui.main.pager.PagerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransApi implements ITransApi{
    private final static String TAG = TransApi.class.getSimpleName();
    private List<Transaction> history_trans_list = new ArrayList<>();
    private DBHelper dbHelper = null;
    public TransApi(Context context){
        dbHelper = new DBHelper(context);
    }
    private List<TransUpdateListener> listenerList = new ArrayList<>();

    @Override
    public boolean addTransUpdateListener(TransUpdateListener listener) {
        return listenerList.add(listener);
    }

    @Override
    public boolean removeTransUpdateListener(TransUpdateListener listener) {
        return listenerList.remove(listener);
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
            String stockId = cursor.getString(cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_CODE));
            if(stockId != null && !stockId.isEmpty()) holdingStockList.add(stockId);
        }

        return holdingStockList;
    }

    @Override
    public Map<String, Integer> getHoldingStockAmount() {
        Map<String, Integer> holdingStockAmount = new HashMap<>();
        for(Transaction trans: ApiUtil.transApi.getHistoryTransList()){
            if(!trans.stock_id.isEmpty()) {
                int amount = holdingStockAmount.getOrDefault(trans.stock_id, 0) + trans.stock_amount;
                if(amount != 0) holdingStockAmount.put(trans.stock_id, amount);
                else holdingStockAmount.remove(trans.stock_id);
            }
        }
        return holdingStockAmount;
    }

    @Override
    public List<Transaction> getHistoryTransList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME + ", " +
                DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE + " ASC";

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
            trans.stock_price = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_PRICE));
            trans.cash_amount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT));
            trans.fee = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE));
            trans.tax = cursor.getInt(
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
    public Transaction getTransaction(int trans_id) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    DBDefine.TB_TransactionRecord._ID + " ASC";
            String selection = DBDefine.TB_TransactionRecord._ID + " = ?";
            String[] selectionArgs = {String.valueOf(trans_id)};

            Cursor cursor = db.query(
                    DBDefine.TB_TransactionRecord.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            cursor.moveToFirst();

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
            trans.stock_price = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_PRICE));
            trans.cash_amount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT));
            trans.fee = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE));
            trans.tax = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX));
            trans.create_time = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME));
            trans.remark = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK));

            return trans;
        } catch (Exception e) {
            Log.e(TAG, "getTransaction fail: " + e.getMessage());
        }
        return new Transaction();
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
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_PRICE, trans.stock_price);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT, trans.cash_amount);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_FEE, trans.fee);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TAX, trans.tax);
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_REMARK, trans.remark);

        // Insert the new row, returning the primary key value of the new row
        long trans_id = db.insert(DBDefine.TB_TransactionRecord.TABLE_NAME, null, values);
        // if insert failed, will return -1
        if(trans_id < 0) return -1;
        for(TransUpdateListener listener: listenerList){
            listener.onAdd(trans);
        }
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
        values.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_STOCK_PRICE, trans.stock_price);
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
        for(TransUpdateListener listener: listenerList){
            listener.onEdit(trans_id, trans);
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
        for(TransUpdateListener listener: listenerList){
            listener.onRemove(trans_id);
        }
        return true;
    }
}
