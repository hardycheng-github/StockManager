package com.msi.stockmanager.ui.main.analysis;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ApiUtil;
import com.msi.stockmanager.data.stock.StockInfo;
import com.msi.stockmanager.data.stock.StockUtilKt;

public final class WatchingListUiHelper {
    private WatchingListUiHelper() {}

    public static void bindFavorite(View container, ImageView icon, Activity activity, String stockId) {
        bindFavorite(container, icon, activity, stockId, null);
    }

    public static void bindFavorite(View container, ImageView icon, Activity activity, String stockId, Runnable onChanged) {
        updateFavoriteIcon(icon, stockId);
        container.setOnClickListener(v -> onFavoriteClick(container, icon, activity, stockId, onChanged));
    }

    public static void updateFavoriteIcon(ImageView icon, String stockId) {
        if (ApiUtil.revenueApi.inWatchingList(stockId)) {
            icon.setImageResource(R.drawable.ic_baseline_favorite_24);
            icon.setImageTintList(ContextCompat.getColorStateList(icon.getContext(), R.color.stock_earn_soft));
        } else {
            icon.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            icon.setImageTintList(ContextCompat.getColorStateList(icon.getContext(), R.color.black));
        }
    }

    private static void onFavoriteClick(View container, ImageView icon, Activity activity, String stockId, Runnable onChanged) {
        if (ApiUtil.revenueApi.inWatchingList(stockId)) {
            StockInfo info = StockUtilKt.getStockInfoOrNull(stockId);
            String stockName = info != null ? info.getStockNameWithId() : stockId;
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.revenue_watching_list_remove_title))
                    .setMessage(activity.getString(R.string.revenue_watching_list_remove_msg).replace("${stock_name}", stockName))
                    .setNegativeButton(R.string.revenue_watching_list_remove_no, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.revenue_watching_list_remove_yes, (dialog, which) -> {
                        ApiUtil.revenueApi.removeWatchingList(stockId);
                        updateFavoriteIcon(icon, stockId);
                        showSnackbar(container, activity.getString(R.string.watching_list_removed));
                        if (onChanged != null) {
                            onChanged.run();
                        }
                        dialog.dismiss();
                    })
                    .show();
        } else {
            ApiUtil.revenueApi.addWatchingList(stockId);
            updateFavoriteIcon(icon, stockId);
            showSnackbar(container, activity.getString(R.string.watching_list_added));
            if (onChanged != null) {
                onChanged.run();
            }
        }
    }

    private static void showSnackbar(View anchor, String message) {
        Activity activity = (Activity) anchor.getContext();
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}