package com.msi.stockmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InputCashInOut extends AppCompatActivity {
    private final static String TAG = "InputCashInOut";
    private RadioButton radioInCash, radioOutcash;
    private EditText editTextDate,editTextNumber;
    private Button transfer_btn;
    private RadioGroup radioGroup1;
    private Date date;
    private Intent intent;
    private int transId = -1;
    ITransApi transApi = new TransApi(this);
    private Transaction trans = new Transaction();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_out_cash);
        Log.d(TAG,"onCreate");

        intent = this.getIntent();
        transId = intent.getIntExtra("transId", -1);
        trans = transApi.getTransaction(transId);
        date = new Date();
        initView();
        setListener();
        initData();
    }

    void initView(){
        radioInCash  = findViewById(R.id.radioInCash);
        radioOutcash =  findViewById(R.id.radioOutcash);
        editTextDate =  findViewById(R.id.editTextDate);
        editTextNumber =  findViewById(R.id.editTextNumber);
        transfer_btn =  findViewById(R.id.transfer_btn);
        radioGroup1 =  findViewById(R.id.radioGroup1);
        radioInCash.toggle();
    }

    void initData(){
        if(trans != null && trans.trans_id >= 0){
            try {
                if (trans.trans_type == TransType.TRANS_TYPE_CASH_IN) {
                    radioInCash.toggle();
                } else {
                    radioOutcash.toggle();
                }
                String dateStr = DateUtil.toDateString(trans.trans_time);
                editTextDate.setText(dateStr);
                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                editTextNumber.setText(Math.abs(trans.cash_amount) + "");
            } catch (Exception e){
                Log.e(TAG, "initData fail: " + e.getMessage());
            }
        }
    }

    void setListener(){
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mYear, mMonth, mDay;
                final Calendar calendar = Calendar.getInstance ();
                mYear = calendar.get ( Calendar.YEAR );
                mMonth = calendar.get ( Calendar.MONTH );
                mDay = calendar.get ( Calendar.DAY_OF_MONTH );

                //show dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog (InputCashInOut.this, new DatePickerDialog.OnDateSetListener () {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextDate.setText ( year + "-" + (month+1<10?"0":"") +(month + 1) + "-" + (dayOfMonth<10?"0":"") + dayOfMonth );

                        try {
                             date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(editTextDate.getText().toString() +" 00:00:00");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                }, mYear, mMonth, mDay );
                datePickerDialog.show();
            }
        });

        transfer_btn.setOnClickListener(v -> {
            if (transId < 0) {
                Log.d(TAG,"Add_Data");
                addTrans();
            } else {
                Log.d(TAG,"Edit_Data");
                editData(transId);
            }
        });
    }

    private void editData(int id){
        int selected = radioGroup1.getCheckedRadioButtonId();
        trans.trans_type=(selected== R.id.radioInCash)?TransType.TRANS_TYPE_CASH_IN:TransType.TRANS_TYPE_CASH_OUT;
        trans.trans_time = date.getTime();
        trans.trans_type_other_desc = "";
        trans.stock_id = "";
        trans.stock_name = "";
        trans.stock_amount = 0;
        trans.cash_amount = Integer.parseInt(editTextNumber.getText().toString());
        trans.fee = 0;
        trans.tax = 0;
        trans.remark = "";
        transApi.updateTrans(id, trans);
        InputCashInOut.this.finish();
    }


    private void addTrans(){
        int selected = radioGroup1.getCheckedRadioButtonId();
        trans.trans_type=(selected== R.id.radioInCash)?TransType.TRANS_TYPE_CASH_IN:TransType.TRANS_TYPE_CASH_OUT;
        trans.trans_time = date.getTime();
        trans.trans_type_other_desc = "";
        trans.stock_id = "";
        trans.stock_name = "";
        trans.stock_amount = 0;
        trans.cash_amount = Integer.parseInt(editTextNumber.getText().toString());
        trans.fee = 0;
        trans.tax = 0;
        trans.remark = "";

        transApi.addTrans(trans);
        InputCashInOut.this.finish();
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }


}
