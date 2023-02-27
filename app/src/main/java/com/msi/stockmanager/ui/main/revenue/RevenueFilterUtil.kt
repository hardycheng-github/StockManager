package com.msi.stockmanager.ui.main.revenue

import com.msi.stockmanager.R

class RevenueFilterUtil(var listener: (hiddenItemList: Set<String>, sortingAscending: Boolean, sortingTarget: String)->Unit) {
    val hiddenItems = mutableSetOf(R.string.revenue_table_header_company_type.toString())
    var sortingAscending = true
    var sortingTarget = R.string.revenue_table_header_stock_id.toString()


    init {
        reset()
    }

    fun reset(){
        hiddenItems.clear()
        hiddenItems += R.string.revenue_table_header_company_type.toString()
        sortingAscending = true
        sortingTarget = R.string.revenue_table_header_stock_id.toString()
    }

    fun update(){
        listener?.let{it(hiddenItems, sortingAscending, sortingTarget)}
    }
}