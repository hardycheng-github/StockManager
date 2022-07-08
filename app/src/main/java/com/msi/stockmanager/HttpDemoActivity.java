package com.msi.stockmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.msi.stockmanager.data.stock.IStockApi;
import com.msi.stockmanager.data.stock.StockApi;
import com.msi.stockmanager.data.stock.StockInfo;

public class HttpDemoActivity extends AppCompatActivity {

    public static final int MESSAGE_SHOW_RESULT = 0;
    public static final int MESSAGE_SHOW_PRICE  = 1;

    private EditText stockCodeInput = null;
    private Button btnTest = null;
    private TextView showPrice = null;
    private TextView showResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_demo);

        stockCodeInput = (EditText) findViewById(R.id.input_stock_code);
        stockCodeInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        btnTest = (Button) findViewById(R.id.btn_http_test);
        showPrice = (TextView) findViewById(R.id.show_price);
        showResult = (TextView) findViewById(R.id.show_result);
        showResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        setListener();
        setCloseKeyboardListener();
    }

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch(message.what) {
                case MESSAGE_SHOW_RESULT:
                    showResult.setText((String) message.obj);
                    break;
                case MESSAGE_SHOW_PRICE:
                    showPrice.setText((String) message.obj);
                    break;
            }
            return false;
        }
    });

    void setListener() {
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = stockCodeInput.getText().toString();
                showResult.setText(code);
                showResult.scrollTo(0 ,0);
                StockApi stock = new StockApi(getApplicationContext());
                stock.getRegularStockPrice(code, new IStockApi.ResultCallback() {
                    @Override
                    public void onResult(StockInfo info) {
                        if(info == null){
                            handler.sendMessage(handler.obtainMessage(0, "ERROR"));
                            handler.sendMessage(handler.obtainMessage(1, String.format("ERROR")));
                        } else {
                            handler.sendMessage(handler.obtainMessage(0, info.toString()));
                            handler.sendMessage(handler.obtainMessage(1, String.valueOf(info.getLastPrice())));
                        }
                    }
                });
            }
        });
    }

    void setCloseKeyboardListener() {
        stockCodeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(stockCodeInput.getWindowToken(), 0);
                return false;
            }
        });
    }

}