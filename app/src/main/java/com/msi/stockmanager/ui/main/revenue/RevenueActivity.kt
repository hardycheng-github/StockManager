package com.msi.stockmanager.ui.main.revenue

import android.annotation.SuppressLint
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import com.msi.stockmanager.R
import com.msi.stockmanager.data.ApiUtil
import com.msi.stockmanager.databinding.ActivityRevenueBinding
import com.msi.stockmanager.ui.main.revenue.tableview.TableViewAdapter
import com.msi.stockmanager.ui.main.revenue.tableview.TableViewModel
import kotlinx.coroutines.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class RevenueActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        private val TAG = RevenueActivity::class.java.simpleName
    }

    lateinit var binding: ActivityRevenueBinding
    lateinit var mMenu: Menu
    private var mYearMonth: YearMonth = YearMonth.now()
    lateinit var tableViewModel: TableViewModel
    lateinit var tableViewAdapter: TableViewAdapter

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            Log.d(TAG, "onStateChanged: " + event.name)
            when(event){
                ON_CREATE -> {
                    binding = ActivityRevenueBinding.inflate(layoutInflater)
                    setContentView(binding.root)
                    setSupportActionBar(binding.toolbar)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    initView()
                    initTable()
                    reload(0, -1)
                }
                ON_START -> {

                }
                ON_DESTROY -> {

                }
                else -> {}
            }
        })
    }

    private fun initTable(){
        tableViewModel = TableViewModel(this)
        tableViewAdapter = TableViewAdapter(tableViewModel)
        binding.revenueTable.setAdapter(tableViewAdapter)
        if(ApiUtil.revenueApi.getWatchingList().isEmpty()){
            ApiUtil.revenueApi.addWatchingList(ApiUtil.transApi.holdingStockList)
        }
        ApiUtil.revenueApi.addWatchingList(listOf("1101", "2330", "1234"))
    }

    private fun initView(){
        binding.btnTitleMonthAdd.setOnClickListener { reload(0, +1) }
        binding.btnTitleMonthSub.setOnClickListener { reload(0, -1) }
        binding.btnTitleYearAdd.setOnClickListener { reload(+1, 0) }
        binding.btnTitleYearSub.setOnClickListener { reload(-1, 0) }
        binding.btnTitleMonthAdd.setOnLongClickListener {
            reload(0, 12-mYearMonth.monthValue)
            true
        }
        binding.btnTitleMonthSub.setOnLongClickListener {
            reload(0, 1-mYearMonth.monthValue)
            true
        }
        binding.btnTitleYearAdd.setOnLongClickListener {
            reload(+999, 0)
            true
        }
        binding.btnTitleYearSub.setOnLongClickListener {
            reload(-999, 0)
            true
        }
    }

    private fun reload(yearShift: Int = 0, monthShift: Int = 0){
        var tmpYearMonth = mYearMonth
        val maxYearMonth = YearMonth.now().minusMonths(1)
        val minYearMonth = YearMonth.of(103+1911, 1)
        tmpYearMonth = tmpYearMonth.plusMonths(monthShift.toLong())
        tmpYearMonth = tmpYearMonth.plusYears(yearShift.toLong())
        if(tmpYearMonth.isBefore(minYearMonth)) tmpYearMonth = minYearMonth
        if(tmpYearMonth.isAfter(maxYearMonth)) tmpYearMonth = maxYearMonth
        if(tmpYearMonth != mYearMonth){
            Log.d(TAG, "[reload] date change $tmpYearMonth -> $mYearMonth")
            mYearMonth = tmpYearMonth
            launch(Dispatchers.Main) {
                binding.areaTable.visibility = View.INVISIBLE
                binding.loading.visibility = View.VISIBLE
                binding.textTitle.text = mYearMonth.format(DateTimeFormatter.ofPattern(getString(R.string.revenue_title_date)))
                binding.btnTitleMonthAdd.visibility = if(mYearMonth == maxYearMonth) View.INVISIBLE else View.VISIBLE
                binding.btnTitleYearAdd.visibility = if(mYearMonth == maxYearMonth) View.INVISIBLE else View.VISIBLE
                binding.btnTitleMonthSub.visibility = if(mYearMonth == minYearMonth) View.INVISIBLE else View.VISIBLE
                binding.btnTitleYearSub.visibility = if(mYearMonth == minYearMonth) View.INVISIBLE else View.VISIBLE
                withContext(Dispatchers.IO){
                    if(!ApiUtil.revenueApi.hasSync()) ApiUtil.revenueApi.sync(true)
                    withContext(Dispatchers.Main){
                        tableViewAdapter.setAllItems(tableViewModel.columnHeaderList, tableViewModel.rowHeaderList, tableViewModel.getCellList(mYearMonth.year, mYearMonth.monthValue))
                        binding.loading.visibility = View.INVISIBLE
                        binding.areaTable.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu
        menuInflater.inflate(R.menu.menu_revenue, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}