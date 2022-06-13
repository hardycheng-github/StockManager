package com.msi.stockmanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.stetho.Stetho;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DatabaseDemoActivity extends AppCompatActivity {
    ITransApi transApi = ApiUtil.transApi;

    private Button addTransBtn = null;
    private Button loadTransBtn = null;
    private Button updateTransBtn = null;
    private Button removeTransBtn = null;
    private Button loadHodingBtn = null;
    private Button clearBtn = null;

    private EditText transIdInput = null;
    private EditText stockCodeInput = null;
    private EditText cashMountInput = null;
    private EditText transDateInput = null;

    private TextView showPanel = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_demo);

        Stetho.initializeWithDefaults(this);

        initView();
        setListener();
        setCloseKeyboardListener();
    }
    void initView(){
        addTransBtn = (Button) findViewById(R.id.add_trans);
        loadTransBtn = (Button) findViewById(R.id.load_trans);
        updateTransBtn = (Button) findViewById(R.id.update_trans);
        removeTransBtn = (Button) findViewById(R.id.remove_trans);
        loadHodingBtn = (Button) findViewById(R.id.load_holding);
        clearBtn = (Button) findViewById(R.id.clear);

        transIdInput = (EditText) findViewById(R.id.trans_id);
        transIdInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        stockCodeInput = (EditText) findViewById(R.id.stock_code);
        stockCodeInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        cashMountInput = (EditText) findViewById(R.id.cash_mount);
        cashMountInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        transDateInput = (EditText) findViewById(R.id.transation_time);

        showPanel = (TextView) findViewById(R.id.show_panel);
        showPanel.setMovementMethod(ScrollingMovementMethod.getInstance());

    }
    void setListener(){
        transDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;
                final Calendar calendar = Calendar.getInstance ();
                mYear = calendar.get ( Calendar.YEAR );
                mMonth = calendar.get ( Calendar.MONTH );
                mDay = calendar.get ( Calendar.DAY_OF_MONTH );

                //show dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog (DatabaseDemoActivity.this, new DatePickerDialog.OnDateSetListener () {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        transDateInput.setText ( year + "-" + (month+1<10?"0":"") +(month + 1) + "-" + (dayOfMonth<10?"0":"") + dayOfMonth );
                    }
                }, mYear, mMonth, mDay );
                datePickerDialog.show();
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPanel.setText("");
                showPanel.scrollTo(0 ,0);
            }
        });
        addTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrans();
            }
        });
        loadTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = transIdInput.getText().toString();
                int transId;
                try {
                    transId = Integer.parseInt(id);
                    loadTrans(transId);
                }catch (NumberFormatException e) {
                    showPanel.append("交易紀錄ID輸入錯誤");
                    return;
                }
            }
        });
        updateTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = transIdInput.getText().toString();
                int transId;
                try {
                    transId = Integer.parseInt(id);
                    updateTrans(transId);
                }catch (NumberFormatException e) {
                    showPanel.append("交易紀錄ID輸入錯誤");
                    return;
                }
            }
        });
        removeTransBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = transIdInput.getText().toString();
                int transId;
                try {
                    transId = Integer.parseInt(id);
                    removeTrans(transId);
                }catch (NumberFormatException e) {
                    showPanel.append("交易紀錄ID輸入錯誤");
                    return;
                }
            }
        });
        loadHodingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadHoding();
            }
        });
    }
    void setCloseKeyboardListener(){
        transIdInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(transIdInput.getWindowToken(), 0);

                return false;
            }
        });
        stockCodeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(stockCodeInput.getWindowToken(), 0);

                return false;
            }
        });
        cashMountInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cashMountInput.getWindowToken(), 0);

                return false;
            }
        });

    }

    // ============= demo api testing ================
    void addTrans(){
        String code = stockCodeInput.getText().toString();
        String cash = cashMountInput.getText().toString();
        String transDate = transDateInput.getText().toString();

        Transaction trans = new Transaction();
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transDate +" 00:00:00");
            trans.trans_time = date.getTime();

            trans.trans_type = TransType.TRANS_TYPE_CASH_IN;
            trans.trans_type_other_desc = "";
            trans.stock_id = code;
            trans.stock_name = "unknown";
            trans.stock_amount = 1000;
            trans.cash_amount = Integer.parseInt(cash);
            trans.fee = 20;
            trans.tax = 400;
            trans.remark = "";

            transApi.addTrans(trans);
            showPanel.append("交易紀錄新增成功");
        } catch (ParseException e) {
            e.printStackTrace();
            showPanel.append("交易紀錄新增失敗");
        }
    }
    void loadTrans(int id){
        List records = transApi.getHistoryTransList();
        for (int i=0;i<records.size();i++) {
            Transaction record = (Transaction) records.get(i);
            if(record.trans_id == id) {
                stockCodeInput.setText(record.stock_id);
                cashMountInput.setText(String.valueOf(record.cash_amount));

                String dateStr = DateUtil.toDateString(record.trans_time);
                transDateInput.setText(dateStr);

                break;
            }

            if(i == records.size()-1){
                showPanel.append("交易紀錄ID不存在\n\n");
            }
        }

        for (int i=0;i<records.size();i++){
            Transaction item = (Transaction) records.get(i);

            showPanel.append(item.toString());
        }
    }
    void updateTrans(int id){
        String code = stockCodeInput.getText().toString();
        String cash = cashMountInput.getText().toString();
        String transDate = transDateInput.getText().toString();

        List records = transApi.getHistoryTransList();
        Transaction record = null;
        for (int i=0;i<records.size();i++) {
            record = (Transaction) records.get(i);
            if(record.trans_id == id) {
                try {
                    record.stock_id = code;
                    record.cash_amount = Integer.parseInt(cash);
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transDate +" 00:00:00");
                    record.trans_time = date.getTime();

                    transApi.updateTrans(record.trans_id, record);
                    showPanel.append("交易紀錄更新成功\n\n");
                } catch (ParseException e) {
                    showPanel.append("交易紀錄更新失敗，請檢查輸入資料格式是否完整正確\n\n");
                    e.printStackTrace();
                }
                break;
            }

            if(i == records.size()-1){
                showPanel.append("交易紀錄ID不存在\n\n");
            }
        }
    }
    void removeTrans(int id){
        List records = transApi.getHistoryTransList();
        Transaction record = null;
        for (int i=0;i<records.size();i++) {
            record = (Transaction) records.get(i);
            if(record.trans_id == id) {
                transApi.removeTrans(record.trans_id);
                showPanel.append("交易紀錄刪除成功");
                break;
            }

            if(i == records.size()-1){
                showPanel.append("交易紀錄ID不存在\n\n");
            }
        }

    }
    void loadHoding(){
        List holdings = transApi.getHoldingStockList();
        for (int i=0;i<holdings.size();i++){
            String holdStr = (String) holdings.get(i);
            showPanel.append(String.format("%d : %s\n", i+1, holdStr));
        }
    }
    // ============= end of demo api testing ================
}
