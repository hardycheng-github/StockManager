package com.msi.stockmanager.data.revenue

import android.os.SystemClock
import android.util.Log
import com.msi.stockmanager.data.DateUtil
import com.msi.stockmanager.data.stock.StockApi
import com.msi.stockmanager.data.stock.StockHistory
import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RevenueApi: IRevenueApi {
    companion object{
        val TAG = RevenueApi::class.java.simpleName
    }
    val infoList = mutableListOf<RevenueInfo>()
    var isInit = false
    var initCallback: IRevenueApi.InitCallback? = null

    override fun init(cb: IRevenueApi.InitCallback?) {
        initCallback = cb
        syncRevenueInfoFromCloud()
    }

    private fun syncRevenueInfoFromCloud(){
        //data from 103-1
        val curDate = Date()
        val year = curDate.year + 1900
        val month = curDate.month + 1
        val twYear = year - 1911
        val syncRevenue = Thread {
            Log.d(TAG, "[syncRevenueInfoFromCloud] start")
            val t1 = SystemClock.uptimeMillis()
            try {
                for (y in 103..twYear) {
                    val t3 = SystemClock.uptimeMillis()
                    val tmpListSize = infoList.size
                    val lastMonth = if (y == twYear) month else 12
                    for (m in 1..lastMonth) {
                        try {
                            val httRequestUrl = "https://mops.twse.com.tw/nas/t21/sii/t21sc03_${y}_${m}.csv"
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
                                val cr = CSVReaderBuilder(br).withSkipLines(1).build()

                                for(rowData in cr.readAll()){
                                    if(rowData.size >= 14){
                                        val yearMonth = rowData[1].split("/")
                                        infoList += RevenueInfo(
                                            yearMonth[0].toInt()+1911,
                                            yearMonth[1].toInt(),
                                            rowData[2],
                                            rowData[3],
                                            rowData[4],
                                            rowData[5].toLong(),
                                            rowData[6].toLong(),
                                            rowData[7].toLong(),
                                            rowData[8].toFloat(),
                                            rowData[9].toFloat(),
                                            rowData[10].toLong(),
                                            rowData[11].toLong(),
                                            rowData[12].toFloat(),
                                            rowData[13],
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception){
                            Log.w(TAG, "[syncRevenueInfoFromCloud] parse ${y}-${m} fail: " + e.message)
                        }
                    }
                    val t4 = SystemClock.uptimeMillis()
                    Log.d(TAG, "[syncRevenueInfoFromCloud] year ${y} finish with %d data, spend %d ms.".format(infoList.size-tmpListSize, (t4-t3)))
                }
                isInit = true
                initCallback?.onSuccess()
            } catch (e: Exception){
                Log.e(TAG, "[syncRevenueInfoFromCloud] err: " + e.message)
                initCallback?.onFail(e?.message?:"unknown")
            }
            val t2 = SystemClock.uptimeMillis()
            Log.i(TAG, "[syncRevenueInfoFromCloud] sync finish with %d data, spend %.1fs.".format(infoList.size, (t2-t1)/1000f))
        }
        syncRevenue.name = "syncRevenue"
        syncRevenue.start()
    }

    override fun getRevenueInfo(stockId: String, year: Int, month: Int): RevenueInfo {
        TODO("Not yet implemented")
    }
}