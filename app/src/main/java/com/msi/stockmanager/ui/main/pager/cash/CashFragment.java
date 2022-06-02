package com.msi.stockmanager.ui.main.pager.cash;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.msi.stockmanager.InputCashInOut;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.database.DBDefine;
import com.msi.stockmanager.database.DBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class CashFragment extends Fragment {
    private static final String TAG = CashFragment.class.getSimpleName();
    private TextView CashIn,CashOut;
    private ListView listview;
    private double cashin_int=0,cashout_int=0;
    private ArrayList<HashMap> listAdapter;
    private CashAdapter adapter;
    ITransApi transApi = null;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CashFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CashFragment newInstance(int columnCount) {
        CashFragment fragment = new CashFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        transApi = new TransApi(getContext());
        initView();
        registerForContextMenu(listview);
    }

    void initView(){
        CashIn = getView().findViewById(R.id.CashIn);
        CashOut = getView().findViewById(R.id.CashOut);
        listview = getView().findViewById(R.id.listView1);
        adapter = new CashAdapter(getContext());
        listview.setAdapter(adapter);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return false;
            }
        });
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

        adapter.setItems(listAdapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
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
                Intent intent = new Intent(getContext(), InputCashInOut.class);
                intent.putExtra("transId", transId);
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        showInList();
    }
}