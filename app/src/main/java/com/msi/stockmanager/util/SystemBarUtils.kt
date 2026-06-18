package com.msi.stockmanager.util

import android.app.Activity
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.msi.stockmanager.R

object SystemBarUtils {

    private const val INSETS_APPLIED_TAG = "system_bar_insets_applied"

    /**
     * Status bar overlaps content, auto-hides, and reveals transiently on a top-edge swipe.
     * Navigation bar keeps reserved bottom space and stays visible.
     */
    fun applySystemBarInsets(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (content.tag == INSETS_APPLIED_TAG) {
            applyStatusBarBehavior(activity, content)
            ViewCompat.requestApplyInsets(content)
            return
        }
        content.tag = INSETS_APPLIED_TAG

        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = activity.getColor(R.color.transparent)
        @Suppress("DEPRECATION")
        window.navigationBarColor = activity.getColor(R.color.bg_1)

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
            val navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(
                navigationBars.left,
                0,
                navigationBars.right,
                navigationBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(content)
        applyStatusBarBehavior(activity, content)
    }

    private fun applyStatusBarBehavior(activity: Activity, content: ViewGroup) {
        val insetsController = WindowCompat.getInsetsController(activity.window, content)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = true
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        insetsController.show(WindowInsetsCompat.Type.navigationBars())
    }
}
