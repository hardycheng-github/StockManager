package com.msi.stockmanager.ui.main.pager.cash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.data.transaction.TransType;
import com.msi.stockmanager.data.transaction.Transaction;

import java.util.ArrayList;

public class CashAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private ArrayList<Transaction> list = new ArrayList<>();

    interface ItemLongClickListener {
        void onLongClick(View view, int position, Transaction trans);
    }
    private ItemLongClickListener longClickListener;

    public CashAdapter(Context context, ItemLongClickListener listener) {
        longClickListener = listener;
        myInflater = LayoutInflater.from(context);
    }

    public void setItems(ArrayList<Transaction> items){
        list.clear();
        list.addAll(items);
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

        Transaction trans = list.get(position);
        if(convertView == null) {

            convertView = myInflater.inflate(R.layout.fragment_cash_item, null);


            viewTag = new ViewTag(
                    convertView.findViewById(R.id.cardView),
                    convertView.findViewById(R.id.textTranType),
                    convertView.findViewById(R.id.textCash),
                    convertView.findViewById(R.id.textTranDate)
            );
            viewTag.cardView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(viewTag.cardView, position, trans);
                return true;
            });
            convertView.setTag(viewTag);

        } else {
            viewTag = (ViewTag) convertView.getTag();
        }
        if (trans.trans_type == TransType.TRANS_TYPE_CASH_OUT){
            viewTag.text1.setText(R.string.TRANS_TYPE_CASH_OUT);
        }else {
            viewTag.text1.setText(R.string.TRANS_TYPE_CASH_IN);
        }

        viewTag.text2.setText(FormatUtil.number(Math.abs(trans.cash_amount)));
        viewTag.text3.setText(DateUtil.toDateString(trans.trans_time));

        return convertView;
    }

    public class ViewTag{
        TextView text1, text2,text3;
        CardView cardView;

        public ViewTag(CardView cardView, TextView textview1, TextView textview2, TextView textview3){
            this.cardView = cardView;
            this.text1 = textview1;
            this.text2 = textview2;
            this.text3 = textview3;
        }
    }
}
