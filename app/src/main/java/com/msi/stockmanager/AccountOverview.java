package com.msi.stockmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.database.DBDefine;
import com.msi.stockmanager.database.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AccountOverview extends AppCompatActivity {
    private final static String TAG = "AccountOverview";
    private TextView CashIn,CashOut;
    private ListView listview;
    private FloatingActionButton fab;
    private Intent intent;
    private Bundle bundle;
    private DBHelper dbhelper = null;
    private double cashin_int=0,cashout_int=0;
    private ArrayList<HashMap> listAdapter;
    private MyBaseAdapter adapter;
    ITransApi transApi = new TransApi(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_main);
        Log.d(TAG,"onCreate");

        bundle = new Bundle();
        intent = new Intent(this,InputCashInOut.class);

        initView();
        setListener();
        registerForContextMenu(listview);


    }

    void setListener(){
        fab.setOnClickListener(view -> {
            bundle.putString("title", "Add_Data");
            intent.putExtras(bundle);
            startActivity(intent);
        });

    }


    void initView(){
        CashIn = findViewById(R.id.CashIn);
        CashOut = findViewById(R.id.CashOut);
        listview = findViewById(R.id.listView1);
        fab = findViewById(R.id.floatingActionButton1);

    }


    private void showInList(){
        listAdapter = new ArrayList<HashMap>();
        List records = transApi.getHistoryTransList();
        cashin_int=0;
        cashout_int=0;

            for (int i=0;i<records.size();i++) {
                Transaction item_tran = (Transaction) records.get(i);

                if (item_tran.stock_id.equals("") && item_tran.cash_amount != 0) {
                    HashMap item = new HashMap();
                    item.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE, item_tran.trans_type);
                    item.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT, item_tran.cash_amount);
                    item.put(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME, DateUtil.toDateString(item_tran.trans_time));
                    item.put("trans_id",item_tran.trans_id);
                    Log.d("showInList", "trans_id="+item_tran.trans_id+" "+item_tran.stock_id+" "+item_tran.cash_amount+" "+item_tran.trans_type+" "+DateUtil.toDateString(item_tran.trans_time));
                     listAdapter.add(item);

                    if (item_tran.trans_type == 2)
                        cashin_int = cashin_int + item_tran.cash_amount;
                    else
                        cashout_int = cashout_int + item_tran.cash_amount;
                }
            }
                 CashIn.setText(cashin_int+"");
                 CashOut.setText(cashout_int+"");

                listview.setAdapter(new MyBaseAdapter(this, listAdapter));

   }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

    }

    private int transId = 0;
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId())
        {
            case R.id.item1:
                Log.d("ContextItem","Edit"+info.position);
                transId = Integer.parseInt(listAdapter.get(info.position).get("trans_id").toString());
                Log.d("ContextItem",transId+"");
                bundle.putString("title", "Edit_Data");
                bundle.putInt("transId", transId);
                intent.putExtras(bundle);
                startActivity(intent);


                return true;
            case R.id.item2:
                Log.d("ContextItem","Del"+info.position);

                transId = Integer.parseInt(listAdapter.get(info.position).get("trans_id").toString());

                transApi.removeTrans(transId);
                showInList();

                return true;

        }
        return onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        showInList();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


}