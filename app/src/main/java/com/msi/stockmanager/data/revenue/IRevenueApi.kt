package com.msi.stockmanager.data.revenue

import android.content.Context

data class RevenueInfo(
    val year: Int, //資料年月-年
    val month: Int, //資料年月-月
    val stockId: String, //公司代號
    val companyName: String, //公司名稱
    val companyType: String, //產業別
    val revenueThisMonth: Long, //營業收入-當月營收
    val revenueLastMonth: Long, //營業收入-上月營收
    val revenueLastYearSameMonth: Long, //營業收入-去年當月營收
    val revenueMoM: Float, //營業收入-上月比較增減(%)
    val revenueYoY: Float, //營業收入-去年同月增減(%)
    val revenueYtdThisYear: Long, //累計營業收入-當月累計營收
    val revenueYtdLastYear: Long, //累計營業收入-去年累計營收
    val revenueYtdYoY: Float, //累計營業收入-前期比較增減(%)
    val note: String, //備註
)

interface IRevenueApi {

    companion object {
        const val SYNC_STATUS_NONE = 0
        const val SYNC_STATUS_ING = 1
        const val SYNC_STATUS_SUCCESS = 2
        const val SYNC_STATUS_FAIL = 3
    }

    interface SyncCallback {
        fun onStart()
        fun onSuccess()
        fun onFail(msg: String)
    }

    /**
     * fetching data from open data cloud
     */
    fun sync(blocking:Boolean = false, cb: SyncCallback? = null): Int

    /**
     * check if sync success before
     */
    fun hasSync(): Boolean

    fun getRevenueInfo(stockId: String, year: Int = -1, month: Int = -1): RevenueInfo?

    fun inWatchingList(stockId: String): Boolean

    fun getWatchingList(): List<String>

    fun addWatchingList(stockId: String): List<String>

    fun addWatchingList(stockIdList: List<String>): List<String>

    fun removeWatchingList(stockId: String): List<String>

    fun clearWatchingList()

}