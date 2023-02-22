package com.msi.stockmanager.data.analytics

import com.msi.stockmanager.data.stock.StockHistory
import io.reactivex.rxjava3.core.SingleObserver

interface ITaApi {

    companion object {
        const val SCORE_MIN: Int = 0
        const val SCORE_MAX: Int = 100
        const val SCORE_ERR: Int = -1
        const val KEY_RSI = "KEY_RSI" //動能
        const val KEY_PPO = "KEY_PPO" //趨勢
        const val KEY_WILLIAMS_R = "KEY_WILLIAMS_R" //Williams %R, 買賣壓力
        const val KEY_TOTAL = "KEY_TOTAL" //綜合分析
    }

    interface Callback: SingleObserver<Map<String, Int>>

    fun getAllIndicatorLastScores(data: List<StockHistory>, callback: Callback)
}