package com.msi.stockmanager.data;

import android.content.Context;

import com.msi.stockmanager.R;

public class FinMindApiDisabledException extends Exception {
    public FinMindApiDisabledException(String message) {
        super(message);
    }

    public static FinMindApiDisabledException fromContext(Context context) {
        if (context == null) {
            return new FinMindApiDisabledException("FinMind API is disabled");
        }
        return new FinMindApiDisabledException(
                context.getString(R.string.finmind_api_disabled_message)
        );
    }
}
