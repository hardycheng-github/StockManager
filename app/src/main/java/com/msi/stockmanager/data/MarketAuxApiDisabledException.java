package com.msi.stockmanager.data;

import android.content.Context;

import com.msi.stockmanager.R;

public class MarketAuxApiDisabledException extends Exception {
    public MarketAuxApiDisabledException(String message) {
        super(message);
    }

    public static MarketAuxApiDisabledException fromContext(Context context) {
        if (context == null) {
            return new MarketAuxApiDisabledException("MarketAux API is disabled");
        }
        return new MarketAuxApiDisabledException(
                context.getString(R.string.marketaux_api_disabled_message)
        );
    }
}
