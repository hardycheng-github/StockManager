package com.msi.stockmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.msi.stockmanager.database.DBDefine;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBaseAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    ArrayList<HashMap> list;

    public MyBaseAdapter(Context context, ArrayList<HashMap> list) {
        myInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewTag viewTag;

        if(convertView == null) {

            convertView = myInflater.inflate(R.layout.transfer_adaper, null);


            viewTag = new ViewTag(
                     convertView.findViewById(R.id.textTranType),
                     convertView.findViewById(R.id.textCash),
                     convertView.findViewById(R.id.textTranDate)
            );
            convertView.setTag(viewTag);

        } else {
            viewTag = (ViewTag) convertView.getTag();
        }

        int  i = (int) list.get(position).get(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TYPE);
        if ( i == 3){
            viewTag.text1.setText("提領金額");
        }else {
            viewTag.text1.setText("存入金額");
        }

        viewTag.text2.setText(list.get(position).get(DBDefine.TB_TransactionRecord.COLUMN_NAME_CASH_AMOUNT).toString());
        viewTag.text3.setText(list.get(position).get(DBDefine.TB_TransactionRecord.COLUMN_NAME_TRANSACTION_TIME).toString());

        return convertView;
    }

    public class ViewTag{
        TextView text1, text2,text3;

        public ViewTag(TextView textview1, TextView textview2, TextView textview3){
            this.text1 = textview1;
            this.text2 = textview2;
            this.text3 = textview3;
        }
    }
}
