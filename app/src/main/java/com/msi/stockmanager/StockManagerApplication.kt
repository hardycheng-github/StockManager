package com.msi.stockmanager

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.msi.stockmanager.util.SystemBarUtils

class StockManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(SystemBarLifecycleCallbacks())
    }

    private class SystemBarLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            SystemBarUtils.applySystemBarInsets(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            SystemBarUtils.applySystemBarInsets(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
