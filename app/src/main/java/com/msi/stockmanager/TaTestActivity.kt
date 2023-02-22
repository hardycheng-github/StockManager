package com.msi.stockmanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.msi.stockmanager.data.ApiUtil
import com.msi.stockmanager.data.analytics.ITaApi
import com.msi.stockmanager.data.stock.IStockApi
import com.msi.stockmanager.data.stock.StockHistory
import com.msi.stockmanager.data.stock.getStockInfoOrNull
import com.msi.stockmanager.databinding.ActivityTaTestBinding
import io.reactivex.rxjava3.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.*

class TaTestActivity : AppCompatActivity() {

    companion object{
        var TAG = TaTestActivity::class.java.simpleName
    }

    lateinit var binding: ActivityTaTestBinding
    private val msgBuilder: StringBuilder = StringBuilder()
    private val msgLimits = 9999

    private val stockList = listOf(
        "1101",
        "1234",
        "2371",
        "2317",
        "2891",
        "2727",
        "1227",
        "8478",
        "2377",
        "2330",
    )

    fun draw(shortValidList: List<Int>, longValidList: List<Int>, totalValidList: List<Int>){
        try {
            val canvas = binding.texture.lockCanvas()
            if(canvas != null){
                binding.texture.unlockCanvasAndPost(canvas)
            }
        } catch (e: Exception){
            Log.e(TAG, "draw err " + e.message)
        }
    }

    private fun stockAnalysis(stockId: String){
        if(stockId.isNotEmpty()){
            ApiUtil.stockApi.getHistoryStockData(stockId, "1d", "1y", object:IStockApi.HistoryCallback{
                override fun onResult(data: MutableList<StockHistory>?) {
                    appendMsg("$stockId get history success: data size " + data?.size)
                    if (data != null) {
                        data.sortBy { it.date_timestamp }
                        val stockName = getStockInfoOrNull(stockId)?.getStockNameWithId()
                        var shortValidList = mutableListOf<Int>()
                        var longValidList = mutableListOf<Int>()
                        var totalValidList = mutableListOf<Int>()
                        val total = data.size
                        val quarter = data.size / 4
                        val shortValidDays = 5
                        val longValidDays = 20
                        val totalValidDays = 10
                        val firstDay = 30
                        val lastDay = total - max(max(shortValidDays,longValidDays), totalValidDays) - 1
                        val lock = Object()
                        data.forEachIndexed { idx, item ->
                            if(idx % quarter == 0){
                                appendMsg("$stockId get ta progress: ($idx/$total)")
                            }
                            if(idx in firstDay..lastDay) {
                                ApiUtil.taApi.getAllIndicatorLastScores(
                                    data.subList(0, idx),
                                    object : ITaApi.Callback {
                                        override fun onSubscribe(d: Disposable) {}

                                        override fun onSuccess(taMap: Map<String, Int>) {
                                            val rsi: Int = taMap.getOrDefault(
                                                ITaApi.KEY_RSI,
                                                ITaApi.SCORE_ERR
                                            )
                                            val ppo: Int = taMap.getOrDefault(
                                                ITaApi.KEY_PPO,
                                                ITaApi.SCORE_ERR
                                            )
                                            val wr: Int = taMap.getOrDefault(
                                                ITaApi.KEY_WILLIAMS_R,
                                                ITaApi.SCORE_ERR
                                            )
                                            val totalScore: Int = taMap.getOrDefault(
                                                ITaApi.KEY_TOTAL,
                                                ITaApi.SCORE_ERR
                                            )
                                            val shortScore = (rsi + wr) / 2
                                            val longScore = ppo
                                            val totalLevel = min(totalScore / 20, 4)

                                            val shortValidItem = data[idx + shortValidDays]
                                            val longValidItem = data[idx + longValidDays]
                                            val totalValidItem = data[idx + totalValidDays]
                                            val shortPriceDiff =
                                                shortValidItem.price_close - item.price_close
                                            val shortPriceRate =
                                                1f * shortPriceDiff / item.price_close
                                            val shortPricePercentage = shortPriceRate * 100
                                            val longPriceDiff =
                                                longValidItem.price_close - item.price_close
                                            val longPriceRate =
                                                1f * longPriceDiff / item.price_close
                                            val longPricePercentage = longPriceRate * 100
                                            val totalPriceDiff =
                                                totalValidItem.price_close - item.price_close
                                            val totalPriceRate =
                                                1f * totalPriceDiff / item.price_close
                                            val totalPricePercentage = totalPriceRate * 100


                                            val dateStr =
                                                SimpleDateFormat("yyyy-MM-dd").format(
                                                    Date(item.date_timestamp)
                                                )
                                            Log.i(
                                                TAG, String.format(
                                                    "%s\t%s\t%d\t%d\t%d\t%.2f\t%.2f\t%.2f\t%s\t%s\t%s",
                                                    stockName,
                                                    dateStr,
                                                    shortScore,
                                                    longScore,
                                                    totalScore,
                                                    shortPricePercentage,
                                                    longPricePercentage,
                                                    totalPricePercentage,
                                                    calcLoss(shortScore, shortPricePercentage),
                                                    calcLoss(longScore, longPricePercentage),
                                                    calcLoss(totalScore, totalPricePercentage),
                                                )
                                            )
                                            synchronized(lock){
                                                lock.notifyAll()
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            appendMsg("$stockId get ta err: " + e?.message)
                                            synchronized(lock){
                                                lock.notifyAll()
                                            }
                                        }

                                    }
                                )
                                synchronized(lock){
                                    lock.wait()
                                }
                            }
                        }
                    }
                }

                override fun onException(e: Exception?) {
                    appendMsg("$stockId get history err: " + e?.message)
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.submit.setOnClickListener {
            val stockId = binding.input.text.toString()
            stockAnalysis(stockId)
        }

//        for (stockId in stockList){
//            stockAnalysis(stockId)
//        }
    }

    private fun calcLoss(predictScore: Int, actualProfitRate: Double): Boolean{
        return when(predictScore){
            in 0..60 -> actualProfitRate > 0
            in 80..100 -> actualProfitRate < 0
            else -> false
        }
    }

    private fun appendMsg(msg: String){
        Log.d(TAG, "appendMsg: $msg")
        msgBuilder.appendLine(msg)
        if(msgBuilder.length > msgLimits) msgBuilder.setLength(msgLimits)
        runOnUiThread {
            binding.msg.text = msgBuilder
        }
    }
}