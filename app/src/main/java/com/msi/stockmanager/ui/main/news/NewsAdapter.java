package com.msi.stockmanager.ui.main.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.news.INewsApi;
import com.msi.stockmanager.databinding.FragmentNewsItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private static final String TAG = NewsAdapter.class.getSimpleName();
    private Context mContext;

    public final List<INewsApi.NewsItem> mItems = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(FragmentNewsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    public void reloadList(List<INewsApi.NewsItem> newsItemList){
        try {
            mItems.clear();
            mItems.addAll(newsItemList);
            notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAG, "reloadList err: " + e.getMessage());
        }
    }

    private void openUrl(String link){
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(link));
            mContext.startActivity(i);
        } catch (Exception e){
            Log.e(TAG, "openUrl err: " + e.getMessage());
            Snackbar snackbar = Snackbar.make(((Activity)mContext).findViewById(android.R.id.content)
                    , mContext.getString(R.string.cannot_open_url)
                    , Snackbar.LENGTH_SHORT);
            snackbar.setAction(mContext.getString(R.string.confirm), v->snackbar.dismiss());
            snackbar.show();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Context context = holder.binding.getRoot().getContext();
        Activity activity = (Activity) context;
        INewsApi.NewsItem item = mItems.get(position);
        holder.mValue = item;
        if(item.image != null){
//            holder.binding.img.setVisibility(View.VISIBLE);
            holder.binding.img.setImageBitmap(item.image);
            holder.binding.imgText.setVisibility(View.INVISIBLE);
        } else {
//            holder.binding.img.setVisibility(View.GONE);
            holder.binding.img.setImageResource(R.drawable.gradient_s);
            switch (item.type){
                case INewsApi.TYPE_ALL:
                    holder.binding.imgText.setText(R.string.news_type_all);
                    break;
                case INewsApi.TYPE_STOCK:
                    holder.binding.imgText.setText(R.string.news_type_stock);
                    break;
                case INewsApi.TYPE_BULLETIN:
                    holder.binding.imgText.setText(R.string.news_type_bulletin);
                    break;
                case INewsApi.TYPE_EXCHANGE:
                    holder.binding.imgText.setText(R.string.news_type_exchange);
                    break;
                case INewsApi.TYPE_CRYPTO:
                    holder.binding.imgText.setText(R.string.news_type_crypto);
                    break;
            }
            holder.binding.imgText.setVisibility(View.VISIBLE);
        }
        holder.binding.title.setText(item.title);
        holder.binding.cardView.setOnClickListener(v->openUrl(item.link));
        holder.binding.subtitle.setText(item.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final FragmentNewsItemBinding binding;
        public INewsApi.NewsItem mValue;

        public ViewHolder(FragmentNewsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}