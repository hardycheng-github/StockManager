package com.msi.stockmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockApi;
import com.msi.stockmanager.data.transaction.ITransApi;
import com.msi.stockmanager.data.transaction.TransApi;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ITransApi transApi = new TransApi(this);
    IStockApi stockApi = new StockApi(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}