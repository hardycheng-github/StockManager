package com.msi.stockmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ITransApi transApi = new TransApi();

    TextView tv_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_msg = findViewById(R.id.tv_msg);
        findViewById(R.id.getHoldingStockList).setOnClickListener(v -> {
            List<String> list = transApi.getHoldingStockList();
            String msg = "[getHoldingStockList] ";
            for(String s: list){
                msg += s+", ";
            }
            addMsg(msg);
        });
        findViewById(R.id.getHistoryTransList).setOnClickListener(v -> {
            List<Transaction> list = transApi.getHistoryTransList();
            String msg = "[getHistoryTransList] ";
            for(Transaction t: list){
                msg += "\n"+t;
            }
            addMsg(msg);
        });
        findViewById(R.id.addTrans).setOnClickListener(v -> {
            long id = transApi.addTrans(null);
            String msg = "[addTrans] " + id;
            addMsg(msg);
        });
        findViewById(R.id.updateTrans).setOnClickListener(v -> {
            boolean success = transApi.updateTrans(0, null);
            String msg = "[updateTrans] " + success;
            addMsg(msg);
        });
        findViewById(R.id.removeTrans).setOnClickListener(v -> {
            boolean success = transApi.removeTrans(0);
            String msg = "[removeTrans] " + success;
            addMsg(msg);
        });
    }

    private void addMsg(String msg){
        String all = tv_msg.getText().toString();
        all = msg + "\n" + all;
        if(all.length() > 10000) all = all.substring(0, 10000);
        tv_msg.setText(all);
    }
}