package com.msi.stockmanager.data.revenue

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import androidx.preference.PreferenceManager
import com.msi.stockmanager.BuildConfig
import com.msi.stockmanager.data.stock.getStockInfoOrNull
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth

class RevenueApi(val context: Context): IRevenueApi {
    companion object{
        private val TAG = RevenueApi::class.java.simpleName
        private const val FINMIND_BASE_URL = "https://api.finmindtrade.com/api/v4/data"
        private const val FINMIND_DATASET_MONTH_REVENUE = "TaiwanStockMonthRevenue"
        private const val DEFAULT_BACKFILL_MONTHS = 24L
        private const val PREF_KEY_WATCHING_LIST = "revenueWatchingList"
        private const val PREF_KEY_LAST_SYNC_PREFIX = "finmind_revenue_last_sync_"
    }

    val infoList = mutableListOf<RevenueInfo>()
    var initCallback: IRevenueApi.SyncCallback? = null
    var syncStatus = IRevenueApi.SYNC_STATUS_NONE
    val mWatchingList = mutableListOf<String>()
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        loadWatchingList()
        sync(false, null)
    }

    override fun sync(blocking: Boolean, cb: IRevenueApi.SyncCallback?): Int {
        if(syncStatus == IRevenueApi.SYNC_STATUS_ING){
            if(blocking){
                runBlocking {
                    while(syncStatus == IRevenueApi.SYNC_STATUS_ING) delay(100)
                }
                when(syncStatus){
                    IRevenueApi.SYNC_STATUS_SUCCESS ->{
                        cb?.onSuccess()
                        return IRevenueApi.SYNC_STATUS_SUCCESS
                    }
                    else -> {
                        cb?.onFail("last sync task fail")
                        return IRevenueApi.SYNC_STATUS_FAIL
                    }
                }
            } else {
                cb?.onFail("last sync task still running")
                return IRevenueApi.SYNC_STATUS_FAIL
            }
        }

        val token = BuildConfig.FINMIND_API_TOKEN.trim()
        if (token.isEmpty()) {
            cb?.onFail("FINMIND_API_TOKEN is empty")
            syncStatus = IRevenueApi.SYNC_STATUS_FAIL
            return IRevenueApi.SYNC_STATUS_FAIL
        }

        initCallback = cb

        val result = GlobalScope.async(Dispatchers.IO) {
            syncStatus = IRevenueApi.SYNC_STATUS_ING
            initCallback?.onStart()
            Log.d(TAG, "[sync] start!")
            val t1 = SystemClock.uptimeMillis()
            try {
                loadWatchingList()
                val targetWatchingSet = mWatchingList.toSet()
                val endMonth = getLatestPublishedYearMonth()
                if (targetWatchingSet.isEmpty()) {
                    syncStatus = IRevenueApi.SYNC_STATUS_SUCCESS
                    initCallback?.onSuccess()
                    Log.i(TAG, "[sync] no watching stock, skip fetching")
                    return@async syncStatus
                }

                val mergedMap = linkedMapOf<String, RevenueInfo>()
                infoList.forEach { old ->
                    if (targetWatchingSet.contains(old.stockId)) {
                        mergedMap[buildRevenueKey(old.stockId, old.year, old.month)] = old
                    }
                }

                var okCount = 0
                var skippedCount = 0
                val failList = mutableListOf<String>()
                for (stockId in targetWatchingSet) {
                    val startMonth = getSyncStartMonth(stockId, endMonth)
                    if (startMonth.isAfter(endMonth)) {
                        skippedCount += 1
                        continue
                    }
                    try {
                        val rows = fetchStockRevenueRows(stockId, startMonth, endMonth, token)
                        val mapped = mapRowsToRevenueInfo(stockId, rows)
                        mapped.forEach { info ->
                            mergedMap[buildRevenueKey(stockId, info.year, info.month)] = info
                        }
                        saveLastSyncedMonth(stockId, endMonth)
                        okCount += 1
                        Log.i(TAG, "[sync] fetched $stockId rows=${mapped.size}, range=$startMonth~$endMonth")
                    } catch (e: Exception) {
                        failList += "$stockId:${e.message}"
                        Log.e(TAG, "[sync] fetch $stockId fail: ${e.message}")
                    }
                }

                infoList.clear()
                infoList.addAll(
                    mergedMap.values.sortedWith(
                        compareBy<RevenueInfo> { it.stockId }
                            .thenByDescending { it.year }
                            .thenByDescending { it.month }
                    )
                )

                if (okCount == 0 && targetWatchingSet.isNotEmpty()) {
                    if (skippedCount == targetWatchingSet.size) {
                        Log.i(TAG, "[sync] all watching stocks are up-to-date, skip network fetch")
                    } else {
                        throw IllegalStateException("sync fail: ${failList.joinToString("; ")}")
                    }
                }

                initCallback?.onSuccess()
                syncStatus = IRevenueApi.SYNC_STATUS_SUCCESS
            } catch (e: Exception){
                Log.e(TAG, "[sync] err: " + e.message)
                initCallback?.onFail(e.message ?: "unknown")
                syncStatus = IRevenueApi.SYNC_STATUS_FAIL
            }
            val t2 = SystemClock.uptimeMillis()
            Log.i(TAG, "[sync] fetching complete with %d data, spend %.2fs.".format(infoList.size, (t2-t1)/1000f))
            syncStatus
        }
        if(blocking){
            runBlocking {
                result.await()
            }
        } else {
            GlobalScope.launch {
                result.await()
            }
        }
        return syncStatus
    }

    private fun getLatestPublishedYearMonth(): YearMonth {
        val now = LocalDate.now()
        return YearMonth.now().minusMonths(if (now.dayOfMonth < 10) 2 else 1)
    }

    private fun getSyncStartMonth(stockId: String, endMonth: YearMonth): YearMonth {
        // infoList 僅存在記憶體，app 重啟後會遺失；若本輪尚無該股票資料，需強制回補區間避免畫面全 0。
        val hasInMemoryData = infoList.any { it.stockId == stockId }
        if (!hasInMemoryData) {
            return endMonth.minusMonths(DEFAULT_BACKFILL_MONTHS)
        }
        val checkpoint = getLastSyncedMonth(stockId)
        return checkpoint?.plusMonths(1) ?: endMonth.minusMonths(DEFAULT_BACKFILL_MONTHS)
    }

    private fun getLastSyncedMonth(stockId: String): YearMonth? {
        val value = sharedPref.getString(PREF_KEY_LAST_SYNC_PREFIX + stockId, null) ?: return null
        return try {
            YearMonth.parse(value)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveLastSyncedMonth(stockId: String, ym: YearMonth) {
        sharedPref.edit()
            .putString(PREF_KEY_LAST_SYNC_PREFIX + stockId, ym.toString())
            .apply()
    }

    private fun buildRevenueKey(stockId: String, year: Int, month: Int): String {
        return "$stockId-$year-${month.toString().padStart(2, '0')}"
    }

    private data class RevenueRow(
        val year: Int,
        val month: Int,
        val revenue: Long
    )

    private fun fetchStockRevenueRows(
        stockId: String,
        startMonth: YearMonth,
        endMonth: YearMonth,
        token: String
    ): List<RevenueRow> {
        val requestUrl = StringBuilder(FINMIND_BASE_URL)
            .append("?dataset=").append(encode(FINMIND_DATASET_MONTH_REVENUE))
            .append("&data_id=").append(encode(stockId))
            .append("&start_date=").append(encode(startMonth.atDay(1).toString()))
            .append("&end_date=").append(encode(endMonth.atEndOfMonth().toString()))
            .toString()
        val body = httpGet(requestUrl, token)
        val root = JSONObject(body)
        if (root.optInt("status", 0) != 200) {
            throw IllegalStateException("FinMind status=${root.optInt("status")} msg=${root.optString("msg")}")
        }
        val data = root.optJSONArray("data") ?: JSONArray()
        val rows = mutableListOf<RevenueRow>()
        for (idx in 0 until data.length()) {
            val row = data.optJSONObject(idx) ?: continue
            val parsed = parseRevenueRow(row) ?: continue
            rows += parsed
        }
        return rows.sortedWith(compareBy<RevenueRow> { it.year }.thenBy { it.month })
    }

    @SuppressLint("DefaultLocale")
    private fun parseRevenueRow(row: JSONObject): RevenueRow? {
        var year = row.optInt("revenue_year", -1)
        var month = row.optInt("revenue_month", -1)
        val date = row.optString("date", "")
        if ((year <= 0 || month <= 0) && date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            year = date.substring(0, 4).toIntOrNull() ?: year
            month = date.substring(5, 7).toIntOrNull() ?: month
        }
        val revenue = firstLong(row, "revenue", "monthly_revenue", "month_revenue")
        if (year <= 0 || month !in 1..12) return null
        return RevenueRow(year = year, month = month, revenue = revenue)
    }

    private fun mapRowsToRevenueInfo(stockId: String, rows: List<RevenueRow>): List<RevenueInfo> {
        if (rows.isEmpty()) return emptyList()
        val sorted = rows.sortedWith(compareBy<RevenueRow> { it.year }.thenBy { it.month })
        // FinMind TaiwanStockMonthRevenue 的 revenue 為元；既有 UI 以 K(千元) 顯示，這裡先轉成千元再進後續計算。
        val revenueMap = sorted.associateBy(
            keySelector = { YearMonth.of(it.year, it.month) },
            valueTransform = { it.revenue / 1000L }
        )
        val stockInfo = getStockInfoOrNull(stockId)
        val companyName = stockInfo?.stockName ?: stockId
        val companyType = stockInfo?.companyType ?: ""
        val result = mutableListOf<RevenueInfo>()

        sorted.forEach { row ->
            val ym = YearMonth.of(row.year, row.month)
            val prevYm = ym.minusMonths(1)
            val prevYearYm = ym.minusYears(1)

            val revenueThisMonth = revenueMap[ym] ?: 0L
            val revenueLastMonth = revenueMap[prevYm] ?: 0L
            val revenueLastYearSameMonth = revenueMap[prevYearYm] ?: 0L
            val revenueMoM = percentChange(revenueThisMonth, revenueLastMonth)
            val revenueYoY = percentChange(revenueThisMonth, revenueLastYearSameMonth)

            val ytdThisYear = revenueMap
                .filterKeys { it.year == ym.year && it.monthValue <= ym.monthValue }
                .values.sum()
            val ytdLastYear = revenueMap
                .filterKeys { it.year == ym.year - 1 && it.monthValue <= ym.monthValue }
                .values.sum()
            val ytdYoY = percentChange(ytdThisYear, ytdLastYear)

            result += RevenueInfo(
                year = ym.year,
                month = ym.monthValue,
                stockId = stockId,
                companyName = companyName,
                companyType = companyType,
                revenueThisMonth = revenueThisMonth,
                revenueLastMonth = revenueLastMonth,
                revenueLastYearSameMonth = revenueLastYearSameMonth,
                revenueMoM = revenueMoM,
                revenueYoY = revenueYoY,
                revenueYtdThisYear = ytdThisYear,
                revenueYtdLastYear = ytdLastYear,
                revenueYtdYoY = ytdYoY,
                note = "FinMind"
            )
        }
        return result
    }

    private fun percentChange(current: Long, base: Long): Float {
        if (base == 0L) return 0f
        // 回傳比值（例如 0.12 代表 12%），搭配 FormatUtil.percent() 顯示。
        return ((current - base).toDouble() / base.toDouble()).toFloat()
    }

    private fun firstLong(row: JSONObject, vararg keys: String): Long {
        keys.forEach { key ->
            if (row.has(key) && !row.isNull(key)) {
                val value = row.optString(key, "").replace(",", "").trim()
                value.toLongOrNull()?.let { return it }
                row.optLong(key, Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE }?.let { return it }
            }
        }
        return 0L
    }

    private fun encode(source: String): String = URLEncoder.encode(source, "UTF-8")

    private fun httpGet(requestUrl: String, token: String): String {
        val url = URL(requestUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.doInput = true
        try {
            val status: Int = connection.responseCode
            val stream: InputStream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (status !in 200..299) {
                throw IllegalStateException("HTTP error status=$status body=$body")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    override fun hasSync(): Boolean {
        return syncStatus == IRevenueApi.SYNC_STATUS_SUCCESS
    }

    override fun getRevenueInfo(stockId: String, year: Int, month: Int): RevenueInfo? {
        if (year <= 0 || month <= 0) {
            return infoList
                .filter { it.stockId == stockId }
                .maxWithOrNull(compareBy<RevenueInfo> { it.year }.thenBy { it.month })
        }
        return infoList.find {
            it.stockId == stockId && it.year == year && it.month == month
        }
    }

    private fun loadWatchingList(){
        mWatchingList.clear()
        sharedPref.getString(PREF_KEY_WATCHING_LIST, "")?.let{ stockIdList ->
            stockIdList.split(",").forEach { stockId ->
                val target = stockId.trim()
                if (target.isNotEmpty()) {
                    mWatchingList += target
                }
            }
        }
    }

    private fun saveWatchingList(){
        sharedPref.edit()
            .putString(PREF_KEY_WATCHING_LIST, mWatchingList.joinToString(","))
            .apply()
    }

    override fun inWatchingList(stockId: String) = mWatchingList.contains(stockId)

    override fun getWatchingList() = mWatchingList

    override fun addWatchingList(stockId: String): List<String> {
        if(!inWatchingList(stockId)){
            mWatchingList += stockId
            saveWatchingList()
        }
        return mWatchingList
    }

    override fun addWatchingList(stockIdList: List<String>): List<String> {
        var change = false
        stockIdList.forEach { stockId ->
            if(!inWatchingList(stockId)){
                mWatchingList += stockId
                change = true
            }
        }
        if(change) saveWatchingList()
        return mWatchingList
    }

    override fun removeWatchingList(stockId: String): List<String> {
        if(inWatchingList(stockId)){
            mWatchingList.remove(stockId)
            saveWatchingList()
        }
        return mWatchingList
    }

    override fun clearWatchingList() {
        if(mWatchingList.isNotEmpty()) {
            mWatchingList.clear()
            saveWatchingList()
        }
    }
}