package com.msi.stockmanager.util;

import android.app.Activity;
import android.content.Context;

/**
 * 結束應用程式行程，讓使用者重新開啟後由 {@link com.msi.stockmanager.ui.main.LaunchActivity}
 * 重新執行 {@link com.msi.stockmanager.data.ApiUtil}、{@link com.msi.stockmanager.data.AccountUtil} 等初始化。
 */
public final class AppExitUtil {

    private AppExitUtil() {}

    public static void exitApp(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }
        System.exit(0);
    }
}
