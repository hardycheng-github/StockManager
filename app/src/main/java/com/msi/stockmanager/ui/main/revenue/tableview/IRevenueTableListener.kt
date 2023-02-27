package com.msi.stockmanager.ui.main.revenue.tableview

import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener

interface IRevenueTableListener: ITableViewListener {
    fun onCornerClicked()
    fun onCornerLongPressed()
}