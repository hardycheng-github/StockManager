package com.msi.stockmanager.data.analytics

import android.util.Log
import com.msi.stockmanager.data.stock.StockHistory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.indicators.CachedIndicator
import org.ta4j.core.indicators.PPOIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.WilliamsRIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

class TaApi : ITaApi {

    companion object {
        val TAG: String = TaApi::class.java.simpleName
        const val DEFAULT_INDICATOR_COUNT = 14
    }

    private fun getMinMaxValue(indicator: CachedIndicator<Num>): Pair<Double, Double>{
        var minValue = Double.MAX_VALUE
        var maxValue = Double.MIN_VALUE
        for(i in 0 until indicator.barSeries.endIndex){
            if(indicator.getValue(i).isNaN) continue
            val value = indicator.getValue(i).doubleValue()
            if(value < minValue) minValue = value
            if(value > maxValue) maxValue = value
        }
        return Pair(minValue, maxValue)
    }

    private fun getScore(key: String, value: Double): Int{
        return maxOf(
            minOf(
                when(key){
                    ITaApi.KEY_PPO -> (value*5+50)
                    ITaApi.KEY_WILLIAMS_R -> value+100
                    else -> value
                }.toInt()
                , ITaApi.SCORE_MAX)
            , ITaApi.SCORE_MIN)
    }

    override fun getAllIndicatorLastScores(dataList: List<StockHistory>, callback: ITaApi.Callback) {
        if(dataList.isEmpty()){
            callback.onError(Exception("data list is null or empty"))
            return
        }
        val snapshot = dataList.toList()
        Log.d(TAG, "getAllIndicatorScores: id ${snapshot[0].stock_id}, size ${snapshot.size}")
        Single.create<Map<String, Int>> {
            try {
                val series = getTimeSeries(snapshot)
                val closes = ClosePriceIndicator(series)
                val rsi = RSIIndicator(closes, DEFAULT_INDICATOR_COUNT)
                val ppo = PPOIndicator(closes)
                val wr = WilliamsRIndicator(series, DEFAULT_INDICATOR_COUNT)
                val endIdx = series.endIndex
                val lastRSI = getScore(ITaApi.KEY_RSI, rsi.getValue(endIdx).doubleValue())
                val lastPPO = getScore(ITaApi.KEY_PPO, ppo.getValue(endIdx).doubleValue())
                val lastWR = getScore(ITaApi.KEY_WILLIAMS_R, wr.getValue(endIdx).doubleValue())
                val total = (lastRSI + lastPPO*2 + lastWR) / 4
                Log.d(TAG, "getAllIndicatorScores result: id ${snapshot[0].stock_id}, total $total" +
                        ", RSI $lastRSI, PPO $lastPPO, WR $lastWR"
                )

                        val map = mutableMapOf<String, Int>()
                map[ITaApi.KEY_RSI] = lastRSI
                map[ITaApi.KEY_PPO] = lastPPO
                map[ITaApi.KEY_WILLIAMS_R] = lastWR
                map[ITaApi.KEY_TOTAL] = total
                it.onSuccess(map)
            } catch (e: Exception){
                Log.e(TAG, "getAllIndicatorScores err: id ${snapshot[0].stock_id}, ${e.message}", e)
                it.onError(e)
            }
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(callback)
    }

    private fun getTimeSeries(dataList: List<StockHistory>): BarSeries{
        val series: BarSeries = BaseBarSeriesBuilder().build()
        var lastTime: ZonedDateTime? = null
        for(data in dataList){
            if (data.date_timestamp <= 0) continue
            val time = timestampToZonedDateTime(data.date_timestamp)
            if (lastTime != null && !time.isAfter(lastTime)) continue
            lastTime = time
            val bar: BaseBar = BaseBar.builder(DecimalNum::valueOf, Number::class.java)
                .timePeriod(Duration.ofDays(1))
                .endTime(time)
                .openPrice(data.price_open)
                .highPrice(data.price_high)
                .lowPrice(data.price_low)
                .closePrice(data.price_close)
                .volume(data.price_volume)
                .build()
            series.addBar(bar)
        }
        if (series.isEmpty) {
            throw IllegalArgumentException("no valid history bars after timestamp filtering")
        }
        return series
    }

    private fun timestampToZonedDateTime(time: Long): ZonedDateTime{
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
    }
}