package com.msi.stockmanager.ui.main.notify;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.DateUtil;
import com.msi.stockmanager.data.notify.MacdSignalConfig;
import com.msi.stockmanager.data.notify.NotifyEntity;
import com.msi.stockmanager.databinding.ItemNotifyBinding;

import java.util.ArrayList;
import java.util.List;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.ViewHolder> {
    
    private List<NotifyEntity> mItems = new ArrayList<>();
    private OnItemClickListener mListener;
    
    public interface OnItemClickListener {
        void onItemClick(NotifyEntity item);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    
    public void setItems(List<NotifyEntity> items) {
        mItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void removeItem(int position) {
        if (position >= 0 && position < mItems.size()) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * 獲取指定位置的 item
     * @param position 位置索引
     * @return NotifyEntity，如果位置無效則返回 null
     */
    public NotifyEntity getItem(int position) {
        if (position >= 0 && position < mItems.size()) {
            return mItems.get(position);
        }
        return null;
    }
    
    /**
     * 檢查位置是否有效
     * @param position 位置索引
     * @return true 如果位置有效，否則 false
     */
    public boolean isValidPosition(int position) {
        return position >= 0 && position < mItems.size();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotifyBinding binding = ItemNotifyBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotifyEntity item = mItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return mItems.size();
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemNotifyBinding binding;
        
        ViewHolder(ItemNotifyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(NotifyEntity item) {
            binding.textTitle.setText(item.getTitle());
            bindTrendIcon(item.getType());

            String body = item.getBody();
            if (body != null && !body.isEmpty()) {
                binding.textBody.setText(body);
                binding.textBody.setVisibility(View.VISIBLE);
            } else {
                binding.textBody.setVisibility(View.GONE);
            }
            
            // 格式化時間
            String timeStr = DateUtil.toDateTimeString(item.getCreatedAt());
            binding.textTime.setText(timeStr);
            
            // 未讀指示器和字體樣式
            if (!item.getRead()) {
                binding.indicatorUnread.setVisibility(View.VISIBLE);
                binding.textTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            } else {
                binding.indicatorUnread.setVisibility(View.GONE);
                binding.textTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            }
            
            // 點擊事件
            binding.getRoot().setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(item);
                }
            });
        }

        private void bindTrendIcon(String type) {
            if (MacdSignalConfig.isBullishType(type)) {
                binding.iconTrend.setImageResource(R.drawable.ic_baseline_trending_up_24);
                binding.iconTrend.setColorFilter(ColorUtil.getProfitEarn(), PorterDuff.Mode.SRC_IN);
                binding.iconTrend.setVisibility(View.VISIBLE);
            } else if (MacdSignalConfig.isBearishType(type)) {
                binding.iconTrend.setImageResource(R.drawable.ic_baseline_trending_down_24);
                binding.iconTrend.setColorFilter(ColorUtil.getProfitLose(), PorterDuff.Mode.SRC_IN);
                binding.iconTrend.setVisibility(View.VISIBLE);
            } else {
                binding.iconTrend.setVisibility(View.GONE);
            }
        }
    }
}
