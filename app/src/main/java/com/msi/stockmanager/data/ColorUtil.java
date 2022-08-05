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

    public static int getProfitEarnLight(){
        return mContext.getColor(Profile.profit_color_reverse ? R.color.stock_lose_light : R.color.stock_earn_light);
    }

    public static int getProfitEarnSoft(){
        return mContext.getColor(Profile.profit_color_reverse ? R.color.stock_lose_soft : R.color.stock_earn_soft);
    }

    public static int getProfitEarn(int percent){
        if(percent < 0) percent = 0;
        else if(percent > 100) percent = 100;
        double rate = percent / 100.0;
        int color1 = getProfitEarn();
        int color2 = getProfitNone();
        int a1 = color1 & 0xFF000000;
        int r1 = color1 & 0x00FF0000;
        int g1 = color1 & 0x0000FF00;
        int b1 = color1 & 0x000000FF;
        int a2 = color2 & 0xFF000000;
        int r2 = color2 & 0x00FF0000;
        int g2 = color2 & 0x0000FF00;
        int b2 = color2 & 0x000000FF;
        int a = (int)(a1 * rate + a2 * (1.-rate)) & 0xFF000000;
        int r = (int)(r1 * rate + r2 * (1.-rate)) & 0x00FF0000;
        int g = (int)(g1 * rate + g2 * (1.-rate)) & 0x0000FF00;
        int b = (int)(b1 * rate + b2 * (1.-rate)) & 0x000000FF;
        int color = a + r + g + b;
        return color;
    }

    public static int getProfitLose(){
        return mContext.getColor(!Profile.profit_color_reverse ? R.color.stock_lose : R.color.stock_earn);
    }

    public static int getProfitLoseLight(){
        return mContext.getColor(!Profile.profit_color_reverse ? R.color.stock_lose_light : R.color.stock_earn_light);
    }

    public static int getProfitLoseSoft(){
        return mContext.getColor(!Profile.profit_color_reverse ? R.color.stock_lose_soft : R.color.stock_earn_soft);
    }

    public static int getProfitLose(int percent){
        if(percent < 0) percent = 0;
        else if(percent > 100) percent = 100;
        double rate = percent / 100.0;
        int color1 = getProfitLose();
        int color2 = getProfitNone();
        int a1 = color1 & 0xFF000000;
        int r1 = color1 & 0x00FF0000;
        int g1 = color1 & 0x0000FF00;
        int b1 = color1 & 0x000000FF;
        int a2 = color2 & 0xFF000000;
        int r2 = color2 & 0x00FF0000;
        int g2 = color2 & 0x0000FF00;
        int b2 = color2 & 0x000000FF;
        int a = (int)(a1 * rate + a2 * (1.-rate)) & 0xFF000000;
        int r = (int)(r1 * rate + r2 * (1.-rate)) & 0x00FF0000;
        int g = (int)(g1 * rate + g2 * (1.-rate)) & 0x0000FF00;
        int b = (int)(b1 * rate + b2 * (1.-rate)) & 0x000000FF;
        int color = a + r + g + b;
        return color;
    }

    public static int getProfitNone(){
        return Color.BLACK;
    }

    public static int getProfitColor(int val){
        if(val > 0){
            return getProfitEarn();
        } else if(val < 0){
            return getProfitLose();
        }
        return getProfitNone();
    }

    public static int getProfitColorSoft(int val){
        if(val > 0){
            return getProfitEarnSoft();
        } else if(val < 0){
            return getProfitLoseSoft();
        }
        return getProfitNone();
    }

    public static int getProfitColorLight(int val){
        if(val > 0){
            return getProfitEarnLight();
        } else if(val < 0){
            return getProfitLoseLight();
        }
        return getProfitNone();
    }

    public static int getProfitScore(int score){
        if(score == 50){
            return getProfitNone();
        } else if(score < 50){
            return getProfitLose((50-score)*2);
//            return getProfitLose();
        }
        return getProfitEarn((score-50)*2);
//        return getProfitEarn();
    }

    public static int getProfitColor(double val){
        if(val > 0){
            return getProfitEarn();
        } else if(val < 0){
            return getProfitLose();
        }
        return getProfitNone();
    }

    public static int getColorWithAlpha(int color, double rate){
        if(rate < 0f) rate = 0f;
        else if(rate > 1f) rate = 1f;
        int a = color >> 24 & 0xFF;
        int newA = ((int)(a * rate) & 0xFF) << 24;
        int newColor = color & 0x00FFFFFF + newA;
        return newColor;
    }
}
