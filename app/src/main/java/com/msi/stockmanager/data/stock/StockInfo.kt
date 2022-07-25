package com.msi.stockmanager.data.stock

import com.msi.stockmanager.components.autocomplete.AutoCompleteEntity
import com.msi.stockmanager.data.DateUtil

class StockInfo(
    id:String = "",
    name:String = "",
    isin:String = "",
    listed:String = "",
    mType:String = "",
    cType:String = "",
    cfi:String = "",
    ): AutoCompleteEntity{
    var stockId: String = id
    var stockName = name
    var isinCode = isin
    var listedDate = listed
    var marketType = mType
    var companyType = cType
    var cfiCode = cfi
    var lastPrice = 0.0
    var lastOpen = 0.0
    var lastHigh = 0.0
    var lastLow = 0.0
    var lastVolume = 0.0
    var lastChange = 0.0
    var lastChangePercent = 0.0
    var previosClose = 0.0
    var lastUpdateTime: Long = 0

    override fun filter(query: String): Boolean {
        return getStockNameWithId().contains(query)
    }

    fun getStockNameWithId(): String {
        return if(stockId.isEmpty()) ""
        else String.format("%s - %s", stockId, stockName)
    }

    override fun toString(): String {
        return String.format(
            "stock_id %s, stock_name %s, last_price %.2f, last_update_time %s",
            stockId,
            stockName,
            lastPrice,
            DateUtil.toDateTimeString(lastUpdateTime)

        )
    }
}