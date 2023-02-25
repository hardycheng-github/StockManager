package com.msi.stockmanager.ui.main.revenue

import android.annotation.SuppressLint
import android.database.DataSetObserver
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import com.msi.stockmanager.R
import com.msi.stockmanager.data.ApiUtil
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.data.stock.getStockInfoOrNull
import com.msi.stockmanager.databinding.ActivityRevenueBinding
import com.msi.stockmanager.databinding.LayoutRevenueFilterBinding
import com.msi.stockmanager.ui.main.StockFilterAdapter
import com.msi.stockmanager.ui.main.revenue.tableview.TableViewAdapter
import com.msi.stockmanager.ui.main.revenue.tableview.TableViewModel
import kotlinx.coroutines.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class RevenueActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        private val TAG = RevenueActivity::class.java.simpleName
    }

    lateinit var filterBinding: LayoutRevenueFilterBinding
    lateinit var binding: ActivityRevenueBinding
    lateinit var mMenu: Menu
    private var mYearMonth: YearMonth = YearMonth.now()
    lateinit var tableViewModel: TableViewModel
    lateinit var tableViewAdapter: TableViewAdapter

    private lateinit var mSearchItem: MenuItem
    private lateinit var mSearchView: SearchView
    private lateinit var mSearchSrcTextView: SearchAutoComplete
    private lateinit var mSearchCloseBtn: ImageView
    private var isSearchExpand = false
    private val mColumnCount = 1

    private lateinit var filterUtil: RevenueFilterUtil

    private val mSearchLayoutChangListener =
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (isSearchExpand != !mSearchView.isIconified) {
                isSearchExpand = !mSearchView.isIconified
            }
        }

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
                    initFilter()
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

    private fun initFilter(){
        filterUtil = RevenueFilterUtil()
        filterBinding = binding.layoutRevenueFilter
        filterBinding.reset.setOnClickListener {
            filterUtil.reset()
        }
    }

    private fun onSearchApply(keyword: String) {
        val info = getStockInfoOrNull(keyword)
        if (info != null) {
            ApiUtil.revenueApi.addWatchingList(info.stockId)
        }
        mSearchView.onActionViewCollapsed()
        reload(enforce=true)
    }

    fun getListPreferredItemHeightInPixels(): Int {
        val value = TypedValue()
        val metrics = resources.displayMetrics
        theme.resolveAttribute(
            android.R.attr.listPreferredItemHeight, value, true
        )
        return TypedValue.complexToDimension(value.data, metrics).toInt()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu
        menuInflater.inflate(R.menu.menu_revenue, menu)
        mSearchItem = mMenu.findItem(R.id.app_bar_search)
        mSearchView = mSearchItem.getActionView() as SearchView
        mSearchSrcTextView = mSearchView.findViewById<SearchAutoComplete>(R.id.search_src_text)
        mSearchCloseBtn = mSearchView.findViewById<ImageView>(R.id.search_close_btn)
        mSearchCloseBtn.setOnClickListener(View.OnClickListener { v: View? -> mSearchView.onActionViewCollapsed() })
        val stockNameList: MutableList<String> = ArrayList()
        for (info in StockUtil.stockList) {
            stockNameList.add(info.getStockNameWithId())
        }
        val adapter: StockFilterAdapter = object : StockFilterAdapter(this, stockNameList) {
            override fun onItemSelected(position: Int, target: String) {
                onSearchApply(target)
            }
        }
        adapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                val count = adapter.count
                val maxHeight = (resources.displayMetrics.heightPixels * .33).toInt()
                val itemHeight: Int = getListPreferredItemHeightInPixels()
                var listHeight = maxHeight
                if (count > 0) {
                    listHeight = Integer.min(maxHeight, itemHeight * count)
                }
                mSearchSrcTextView.setDropDownHeight(listHeight)
            }
        })
        mSearchSrcTextView.setAdapter<StockFilterAdapter>(adapter)
        mSearchSrcTextView.setDropDownBackgroundResource(R.color.white)
        //        mSearchSrcTextView.setDropDownHeight((int) (getResources().getDisplayMetrics().heightPixels*.33));
        mSearchSrcTextView.setThreshold(1)
        mSearchView.addOnLayoutChangeListener(mSearchLayoutChangListener)
        mSearchView.setOnSearchClickListener(View.OnClickListener { mSearchView.onActionViewExpanded() })
        mSearchView.setQueryHint(getString(R.string.hint_stock_search))
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchView.onActionViewCollapsed()
                onSearchApply(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        reload(enforce = true)
        return true
    }

    private fun initTable(){
        tableViewModel = TableViewModel(this)
        tableViewAdapter = TableViewAdapter(tableViewModel)
        binding.revenueTable.setAdapter(tableViewAdapter)
        ApiUtil.revenueApi.clearWatchingList()
        ApiUtil.revenueApi.addWatchingList(ApiUtil.transApi.holdingStockList)
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

    private fun reload(yearShift: Int = 0, monthShift: Int = 0, enforce: Boolean = false){
        var tmpYearMonth = mYearMonth
        val maxYearMonth = YearMonth.now().minusMonths(1)
        val minYearMonth = YearMonth.of(103+1911, 1)
        tmpYearMonth = tmpYearMonth.plusMonths(monthShift.toLong())
        tmpYearMonth = tmpYearMonth.plusYears(yearShift.toLong())
        if(tmpYearMonth.isBefore(minYearMonth)) tmpYearMonth = minYearMonth
        if(tmpYearMonth.isAfter(maxYearMonth)) tmpYearMonth = maxYearMonth
        if(tmpYearMonth != mYearMonth || enforce){
            Log.d(TAG, "[reload] date change $tmpYearMonth -> $mYearMonth, enforce: $enforce")
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
                        applyFilter()
                    }
                }
            }
        }
    }

    private fun applyFilter(){
        binding.revenueTable.clearHiddenColumnList()
        val columnHeaderMap = mutableMapOf<String, Integer>()
        filterUtil.hiddenItems.forEach {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.app_bar_filter -> {
                binding.drawer.openDrawer(GravityCompat.END);
                return true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerVisible(GravityCompat.END)) {
            binding.drawer.closeDrawer(GravityCompat.END)
        } else if (!mSearchView.isIconified) {
            mSearchView.onActionViewCollapsed()
            mSearchItem.collapseActionView()
            MenuItemCompat.collapseActionView(mSearchItem)
        } else {
            super.onBackPressed()
        }
    }

}