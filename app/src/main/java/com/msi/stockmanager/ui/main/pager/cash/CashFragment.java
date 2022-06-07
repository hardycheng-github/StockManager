package com.msi.stockmanager.ui.main.pager.cash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.msi.stockmanager.InputCashInOut;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;
import com.msi.stockmanager.database.DBDefine;

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
    private int cashin_int=0,cashout_int=0;
    private ArrayList<Transaction> listAdapter = new ArrayList<>();;
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
//        registerForContextMenu(listview);
    }

    void initView(){
        CashIn = getView().findViewById(R.id.CashIn);
        CashOut = getView().findViewById(R.id.CashOut);
        listview = getView().findViewById(R.id.listView1);
        adapter = new CashAdapter(getContext(), new CashAdapter.ItemLongClickListener() {
            @Override
            public void onLongClick(View view, int position, Transaction trans) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.item_edit, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                        switch(item.getItemId())
                        {
                            case R.id.item1:
                                Log.d("ContextItem","Edit"+info.position);
                                Intent intent = new Intent(getContext(), InputCashInOut.class);
                                intent.putExtra("transId", listAdapter.get(info.position).trans_id);
                                startActivity(intent);


                                return true;
                            case R.id.item2:
                                Log.d("ContextItem","Del"+info.position);
                                transApi.removeTrans(listAdapter.get(info.position).trans_id);
                                showInList();

                                return true;

                        }
                        return onOptionsItemSelected(item);
                    }
                });
                // Showing the popup menu
                popupMenu.show();
            }
        });
        listview.setAdapter(adapter);
    }


    private void showInList(){
        listAdapter.clear();
        List<Transaction> records = transApi.getHistoryTransList();
        cashin_int=0;
        cashout_int=0;

        for (Transaction item_tran: records) {
            if(item_tran.trans_type != TransType.TRANS_TYPE_CASH_IN &&
                item_tran.trans_type != TransType.TRANS_TYPE_CASH_OUT){
                continue;
            }
            if (item_tran.stock_id.equals("") && item_tran.cash_amount != 0) {
                listAdapter.add(item_tran);
                if (item_tran.trans_type == TransType.TRANS_TYPE_CASH_IN)
                    cashin_int += item_tran.cash_amount;
                else
                    cashout_int += item_tran.cash_amount;
            }
        }
        CashIn.setText(cashin_int+"");
        CashOut.setText(-cashout_int+"");

        adapter.setItems(listAdapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        showInList();
    }
}