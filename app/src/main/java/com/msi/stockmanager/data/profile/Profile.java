package com.msi.stockmanager.data.profile;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Profile {
    public static double fee_discount = 0.6;
    public static double fee_rate = 0.001425;
    public static int fee_minimum = 20;
    public static double tax_rate = 0.003;
    public static boolean profit_color_reverse = false;
    public static void load(Context context){
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            fee_discount = Double.parseDouble(preferences.getString("setting_fee_discount", fee_discount+""));
            fee_minimum = Integer.parseInt(preferences.getString("setting_fee_minimum", fee_minimum + ""));
            profit_color_reverse = preferences.getBoolean("profit_color_reverse", profit_color_reverse);
        } catch (Exception e){}
    }
}
