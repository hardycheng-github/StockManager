package com.msi.stockmanager.data;

import android.content.Context;
import android.graphics.Color;

import com.msi.stockmanager.R;
import com.msi.stockmanager.data.profile.Profile;

public class ColorUtil {
    private static Context mContext;

    public static void init(Context context){
        mContext = context;
    }

    public static int getProfitEarn(){
        return mContext.getColor(Profile.profit_color_reverse ? R.color.stock_lose : R.color.stock_earn);
    }

    public static int getProfitLose(){
        return mContext.getColor(!Profile.profit_color_reverse ? R.color.stock_lose : R.color.stock_earn);
    }

    public static int getProfitNone(){
        return Color.BLACK;
    }
}
