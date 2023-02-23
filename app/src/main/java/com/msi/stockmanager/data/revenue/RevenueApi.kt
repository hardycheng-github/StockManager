package com.msi.stockmanager.data.revenue

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.msi.stockmanager.data.DateUtil
import com.msi.stockmanager.data.stock.StockApi
import com.msi.stockmanager.data.stock.StockHistory
import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RevenueApi(val context: Context): IRevenueApi {
    companion object{
        val TAG = RevenueApi::class.java.simpleName
    }
    val infoList = mutableListOf<RevenueInfo>()
    var initCallback: IRevenueApi.SyncCallback? = null
    var syncStatus = IRevenueApi.SYNC_STATUS_NONE

    init {
        sync(false, null)
    }

    override fun sync(blocking: Boolean, cb: IRevenueApi.SyncCallback?): Int {
        if(syncStatus == IRevenueApi.SYNC_STATUS_ING){
            cb?.onFail("last sync task still running")
            IRevenueApi.SYNC_STATUS_FAIL
        }
        initCallback = cb
        //data from 103-1
        val curDate = Date()
        val year = curDate.year + 1900
        val month = curDate.month + 1
        val twYear = year - 1911
        val result = GlobalScope.async(Dispatchers.IO) {
            syncStatus = IRevenueApi.SYNC_STATUS_ING
            initCallback?.onStart()
            Log.d(TAG, "[sync] start!")
            val t1 = SystemClock.uptimeMillis()
            try {
                for (y in 103..twYear) {
                    val requestList = mutableListOf<Deferred<MutableList<RevenueInfo>>>()
                    val lastMonth = if (y == twYear) month else 12
                    val t3 = SystemClock.uptimeMillis()
                    for (m in 1..lastMonth) {
                        requestList += GlobalScope.async(Dispatchers.IO){
                            val tmpList = mutableListOf<RevenueInfo>()
                            var tmpStr = ""
                            Log.v(TAG, "[sync] fetching ${y}-${m} data")
                            try {
                                val filename = "t21sc03_${y}_${m}.csv"
                                val file = getFileFromInternalStorage(filename)
                                if(!file.exists()){
                                    Log.d(TAG, "[sync] local data [${y}-${m}] not exists, start fetching...")
                                    val httRequestUrl = "https://mops.twse.com.tw/nas/t21/sii/$filename"
                                    val url = URL(httRequestUrl)
                                    val connection = url.openConnection() as HttpURLConnection
                                    connection.requestMethod = "GET"
                                    connection.doInput = true
                                    val inputStream: InputStream = connection.inputStream
                                    val status: Int = connection.responseCode
                                    if (status != 200 || inputStream == null) {
                                        throw java.lang.Exception("HTTP error fetching URL (status=$status, URL=$httRequestUrl)")
                                    } else {
                                        val reader = InputStreamReader(inputStream, "UTF-8")
                                        val br = BufferedReader(reader)
                                        file.parentFile.mkdirs()
                                        file.createNewFile()
                                        val writer = FileWriter(file)
                                        br.forEachLine {
                                            writer.write(it + "\n")
                                        }
                                        writer.flush()
                                        writer.close()
                                        br.close()
                                        Log.d(TAG, "[sync] fetching and save [${y}-${m}] complete")
                                    }
                                }
                                val reader = InputStreamReader(file.inputStream(), "UTF-8")
                                val br = BufferedReader(reader)
                                val cr = CSVReaderBuilder(br).withSkipLines(1).build()
                                for(rowData in cr.readAll()){
                                    if(rowData.size >= 14){
                                        tmpStr = rowData.joinToString(", ", "[", "]")
                                        val yearMonth = rowData[1].split("/")
                                        tmpList += RevenueInfo(
                                            yearMonth[0].toInt()+1911,
                                            yearMonth[1].toInt(),
                                            rowData[2],
                                            rowData[3],
                                            rowData[4],
                                            rowData[5].toLongOrNull()?:0,
                                            rowData[6].toLongOrNull()?:0,
                                            rowData[7].toLongOrNull()?:0,
                                            rowData[8].toFloatOrNull()?:0f,
                                            rowData[9].toFloatOrNull()?:0f,
                                            rowData[10].toLongOrNull()?:0,
                                            rowData[11].toLongOrNull()?:0,
                                            rowData[12].toFloatOrNull()?:0f,
                                            rowData[13],
                                        )
                                    }
                                }
                                cr.close()
                            } catch (e: Exception){
                                Log.w(TAG, "[sync] parse ${y}-${m} fail: " + e.message + ", detail: $tmpStr")
                            }
                            tmpList
                        }
                    }
                    runBlocking {
                        val yearList = requestList.awaitAll().flatten()
                        val t4 = SystemClock.uptimeMillis()
                        infoList.addAll(yearList)
                        Log.i(TAG, "[sync] fetching year[$y] with %d data, spend %.2fs.".format(yearList.size, (t4-t3)/1000f))
                    }
                }
                initCallback?.onSuccess()
                syncStatus = IRevenueApi.SYNC_STATUS_SUCCESS
            } catch (e: Exception){
                Log.e(TAG, "[sync] err: " + e.message)
                initCallback?.onFail(e?.message?:"unknown")
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

    private fun getFileFromInternalStorage(filename: String): File {
        val rootPath = context.filesDir.path + File.separator + "revenue" + File.separator
        val filePath = rootPath + filename
        return File(filePath)
    }

    override fun hasSync(): Boolean {
        return syncStatus == IRevenueApi.SYNC_STATUS_SUCCESS
    }

    override fun getRevenueInfo(stockId: String, year: Int, month: Int): RevenueInfo {
        TODO("Not yet implemented")
    }
}