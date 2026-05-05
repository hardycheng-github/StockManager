package com.msi.stockmanager.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public final class ExternalApiPrefs {
    public static final String KEY_ENABLE_FINMIND_API = "enable_finmind_api";
    public static final String KEY_ENABLE_MARKETAUX_API = "enable_marketaux_api";
    public static final boolean DEFAULT_ENABLE_FINMIND_API = true;
    public static final boolean DEFAULT_ENABLE_MARKETAUX_API = true;

    private ExternalApiPrefs() {}

    public static boolean isFinMindApiEnabled(Context context) {
        if (context == null) return DEFAULT_ENABLE_FINMIND_API;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_ENABLE_FINMIND_API, DEFAULT_ENABLE_FINMIND_API);
    }

    public static boolean isMarketAuxApiEnabled(Context context) {
        if (context == null) return DEFAULT_ENABLE_MARKETAUX_API;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(KEY_ENABLE_MARKETAUX_API, DEFAULT_ENABLE_MARKETAUX_API);
    }
}
