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
import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.sort.SortState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.msi.stockmanager.R
import com.msi.stockmanager.data.ApiUtil
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.data.stock.getStockInfoOrNull
import com.msi.stockmanager.databinding.ActivityRevenueBinding
import com.msi.stockmanager.databinding.LayoutRevenueFilterBinding
import com.msi.stockmanager.ui.main.StockFilterAdapter
import com.msi.stockmanager.ui.main.revenue.tableview.IRevenueTableListener
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
                    initFilter()
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

    private fun onRevenueFilterApply(hiddenItemList: Set<String>, sortingAscending: Boolean, sortingTarget: String){
        Log.d(TAG, "onRevenueFilterApply(hiddenItemList: ${hiddenItemList.joinToString()}, sortingAscending: $sortingAscending, sortingTarget: $sortingTarget)")
        launch(Dispatchers.Main) {
            filterBinding.revenueTableHeaderCompanyType.isSelected = false
            filterBinding.revenueTableHeaderRevenueThisMonth.isSelected = false
            filterBinding.revenueTableHeaderRevenueLastMonth.isSelected = false
            filterBinding.revenueTableHeaderRevenueMom.isSelected = false
            filterBinding.revenueTableHeaderRevenueLastYear.isSelected = false
            filterBinding.revenueTableHeaderRevenueYoy.isSelected = false
            filterBinding.revenueTableHeaderRevenueThisYtd.isSelected = false
            filterBinding.revenueTableHeaderRevenueLastYtd.isSelected = false
            filterBinding.revenueTableHeaderRevenueYoyYtd.isSelected = false

            filterBinding.revenueFilterSortTypeAsc.isSelected = sortingAscending
            filterBinding.revenueFilterSortTypeDes.isSelected = !sortingAscending

            filterBinding.revenueSortHeaderStockId.isSelected = false
            filterBinding.revenueSortHeaderCompanyType.isSelected = false
            filterBinding.revenueSortHeaderRevenueThisMonth.isSelected = false
            filterBinding.revenueSortHeaderRevenueLastMonth.isSelected = false
            filterBinding.revenueSortHeaderRevenueMom.isSelected = false
            filterBinding.revenueSortHeaderRevenueLastYear.isSelected = false
            filterBinding.revenueSortHeaderRevenueYoy.isSelected = false
            filterBinding.revenueSortHeaderRevenueThisYtd.isSelected = false
            filterBinding.revenueSortHeaderRevenueLastYtd.isSelected = false
            filterBinding.revenueSortHeaderRevenueYoyYtd.isSelected = false

            when(sortingTarget.toIntOrNull()?:0){
                R.string.revenue_table_header_stock_id -> {
                    filterBinding.revenueSortHeaderStockId.isSelected = true
                }
                R.string.revenue_table_header_company_type -> {
                    filterBinding.revenueSortHeaderCompanyType.isSelected = true
                }
                R.string.revenue_table_header_revenue_this_month -> {
                    filterBinding.revenueSortHeaderRevenueThisMonth.isSelected = true
                }
                R.string.revenue_table_header_revenue_last_month -> {
                    filterBinding.revenueSortHeaderRevenueLastMonth.isSelected = true
                }
                R.string.revenue_table_header_revenue_mom -> {
                    filterBinding.revenueSortHeaderRevenueMom.isSelected = true
                }
                R.string.revenue_table_header_revenue_last_year -> {
                    filterBinding.revenueSortHeaderRevenueLastYear.isSelected = true
                }
                R.string.revenue_table_header_revenue_yoy -> {
                    filterBinding.revenueSortHeaderRevenueYoy.isSelected = true
                }
                R.string.revenue_table_header_revenue_this_ytd -> {
                    filterBinding.revenueSortHeaderRevenueThisYtd.isSelected = true
                }
                R.string.revenue_table_header_revenue_last_ytd -> {
                    filterBinding.revenueSortHeaderRevenueLastYtd.isSelected = true
                }
                R.string.revenue_table_header_revenue_yoy_ytd -> {
                    filterBinding.revenueSortHeaderRevenueYoyYtd.isSelected = true
                }
            }

            hiddenItemList.forEach {
                when(it.toIntOrNull()?:0){
                    R.string.revenue_table_header_company_type -> {
                        filterBinding.revenueTableHeaderCompanyType.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_this_month -> {
                        filterBinding.revenueTableHeaderRevenueThisMonth.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_last_month -> {
                        filterBinding.revenueTableHeaderRevenueLastMonth.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_mom -> {
                        filterBinding.revenueTableHeaderRevenueMom.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_last_year -> {
                        filterBinding.revenueTableHeaderRevenueLastYear.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_yoy -> {
                        filterBinding.revenueTableHeaderRevenueYoy.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_this_ytd -> {
                        filterBinding.revenueTableHeaderRevenueThisYtd.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_last_ytd -> {
                        filterBinding.revenueTableHeaderRevenueLastYtd.isSelected = true
                    }
                    R.string.revenue_table_header_revenue_yoy_ytd -> {
                        filterBinding.revenueTableHeaderRevenueYoyYtd.isSelected = true
                    }
                }
            }

            val columnHeaderList = tableViewModel.columnHeaderList
            val columnHeaderSize = columnHeaderList.size
            // sorting
            (0 until columnHeaderSize).forEach {
                val columnHeader = columnHeaderList[it]
                var state = if(columnHeader.id == filterUtil.sortingTarget){
                    if(filterUtil.sortingAscending) SortState.ASCENDING
                    else SortState.DESCENDING
                } else SortState.UNSORTED
                binding.revenueTable.sortColumn(it, state)
            }
            if(filterUtil.sortingTarget == R.string.revenue_table_header_stock_id.toString()){
                binding.revenueTable.sortRowHeader(
                    if(filterUtil.sortingAscending) SortState.ASCENDING
                    else SortState.DESCENDING
                )
            }
//            delay(1000)
            //filter
            (0 until columnHeaderSize).forEach {
                val columnHeader = columnHeaderList[it]
                if(hiddenItemList.contains(columnHeader.id)){
                    binding.revenueTable.setColumnWidth(it, 0)
//                    binding.revenueTable.hideColumn(it)
                    // TODO: hide column cause error when onBind with wrong index
                } else {
                    // Recalculate of the width values of the columns
//                    binding.revenueTable.getCellLayoutManager().fitWidthSize(it, false)
//                    try { binding.revenueTable.remeasureColumnWidth(it) } catch (e: Exception){}
                    binding.revenueTable.setColumnWidth(it, 300)
                }
            }
            binding.revenueTable.adapter?.notifyDataSetChanged()
        }
    }

    private fun initFilter(){
        filterUtil = RevenueFilterUtil(::onRevenueFilterApply)
        filterBinding = binding.layoutRevenueFilter
        filterBinding.reset.setOnClickListener { resetFilter() }
        filterBinding.apply.setOnClickListener { applyFilter() }
        filterBinding.revenueTableHeaderCompanyType.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueThisMonth.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueLastMonth.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueMom.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueLastYear.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueYoy.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueThisYtd.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueLastYtd.setOnClickListener { it.isSelected = !it.isSelected }
        filterBinding.revenueTableHeaderRevenueYoyYtd.setOnClickListener { it.isSelected = !it.isSelected }

        filterBinding.revenueFilterSortTypeAsc.setOnClickListener { selectSortingType(it) }
        filterBinding.revenueFilterSortTypeDes.setOnClickListener { selectSortingType(it) }

        filterBinding.revenueSortHeaderStockId.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderCompanyType.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueThisMonth.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueLastMonth.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueMom.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueLastYear.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueYoy.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueThisYtd.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueLastYtd.setOnClickListener { selectSortingTarget(it) }
        filterBinding.revenueSortHeaderRevenueYoyYtd.setOnClickListener { selectSortingTarget(it) }
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
        tableViewModel = TableViewModel(this, filterUtil);
        tableViewAdapter = TableViewAdapter(tableViewModel)
        binding.revenueTable.setAdapter(tableViewAdapter)
//        binding.revenueTable.isIgnoreSelectionColors = true
        ApiUtil.revenueApi.clearWatchingList()
        ApiUtil.revenueApi.addWatchingList(ApiUtil.transApi.holdingStockList)

        tableViewModel.setListener(object:IRevenueTableListener{
            override fun onCornerClicked() {
                Log.v(TAG, "[IRevenueTableListener] onCornerClicked")
                if(filterUtil.sortingTarget == R.string.revenue_table_header_stock_id.toString()) {
                    filterUtil.sortingAscending = !filterUtil.sortingAscending
                } else {
                    filterUtil.sortingTarget = R.string.revenue_table_header_stock_id.toString()
                    filterUtil.sortingAscending = true
                }
                filterUtil.update()
            }

            override fun onCornerLongPressed() {
                Log.v(TAG, "[IRevenueTableListener] onCornerLongPressed")
            }

            override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int) {
                Log.v(TAG, "[IRevenueTableListener] onCellClicked: $column, $row")
            }

            override fun onCellDoubleClicked(
                cellView: RecyclerView.ViewHolder,
                column: Int,
                row: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onCellDoubleClicked: $column, $row")
            }

            override fun onCellLongPressed(
                cellView: RecyclerView.ViewHolder,
                column: Int,
                row: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onCellLongPressed: $column, $row")
            }

            override fun onColumnHeaderClicked(
                columnHeaderView: RecyclerView.ViewHolder,
                column: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onColumnHeaderClicked: $column")
                val item = tableViewModel.columnHeaderList[column]
                if(filterUtil.sortingTarget == item.id) {
                    filterUtil.sortingAscending = !filterUtil.sortingAscending
                } else {
                    filterUtil.sortingTarget = item.id
                    filterUtil.sortingAscending = true
                }
                filterUtil.update()
            }

            override fun onColumnHeaderDoubleClicked(
                columnHeaderView: RecyclerView.ViewHolder,
                column: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onColumnHeaderDoubleClicked: $column")
            }

            override fun onColumnHeaderLongPressed(
                columnHeaderView: RecyclerView.ViewHolder,
                column: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onColumnHeaderLongPressed: $column")
            }

            override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
                Log.v(TAG, "[IRevenueTableListener] onRowHeaderClicked: $row")
            }

            override fun onRowHeaderDoubleClicked(
                rowHeaderView: RecyclerView.ViewHolder,
                row: Int
            ) {
                Log.v(TAG, "[IRevenueTableListener] onRowHeaderDoubleClicked: $row")
            }

            override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int) {
                Log.v(TAG, "[IRevenueTableListener] onRowHeaderLongPressed: $row")
                val stockId = tableViewModel.rowHeaderList[row].id
                val stockName = getStockInfoOrNull(stockId)?.getStockNameWithId()?:stockId
                MaterialAlertDialogBuilder(this@RevenueActivity)
                    .setTitle(getString(R.string.revenue_watching_list_remove_title).replace("\${stock_name}", stockName))
                    .setMessage(getString(R.string.revenue_watching_list_remove_msg).replace("\${stock_name}", stockName))
                    .setNegativeButton(R.string.revenue_watching_list_remove_no){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.revenue_watching_list_remove_yes){ dialog, _ ->
                        ApiUtil.revenueApi.removeWatchingList(stockId)
                        reload(enforce = true)
                        dialog.dismiss()
                    }
                    .show()
            }

        })
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
                        tableViewAdapter.cornerView?.visibility = View.VISIBLE
                        binding.loading.visibility = View.INVISIBLE
                        binding.areaTable.visibility = View.VISIBLE
                        filterUtil.update()
                    }
                }
            }
        }
    }

    private fun applyFilter(){
        filterUtil.hiddenItems.clear()
        if(filterBinding.revenueTableHeaderCompanyType.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_company_type.toString()
        if(filterBinding.revenueTableHeaderRevenueThisMonth.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_this_month.toString()
        if(filterBinding.revenueTableHeaderRevenueLastMonth.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_last_month.toString()
        if(filterBinding.revenueTableHeaderRevenueMom.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_mom.toString()
        if(filterBinding.revenueTableHeaderRevenueLastYear.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_last_year.toString()
        if(filterBinding.revenueTableHeaderRevenueYoy.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_yoy.toString()
        if(filterBinding.revenueTableHeaderRevenueThisYtd.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_this_ytd.toString()
        if(filterBinding.revenueTableHeaderRevenueLastYtd.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_last_ytd.toString()
        if(filterBinding.revenueTableHeaderRevenueYoyYtd.isSelected) filterUtil.hiddenItems += R.string.revenue_table_header_revenue_yoy_ytd.toString()

        filterUtil.sortingAscending = filterBinding.revenueFilterSortTypeAsc.isSelected

        if(filterBinding.revenueSortHeaderStockId.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_stock_id.toString()
        if(filterBinding.revenueSortHeaderCompanyType.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_company_type.toString()
        if(filterBinding.revenueSortHeaderRevenueThisMonth.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_this_month.toString()
        if(filterBinding.revenueSortHeaderRevenueLastMonth.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_last_month.toString()
        if(filterBinding.revenueSortHeaderRevenueMom.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_mom.toString()
        if(filterBinding.revenueSortHeaderRevenueLastYear.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_last_year.toString()
        if(filterBinding.revenueSortHeaderRevenueYoy.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_yoy.toString()
        if(filterBinding.revenueSortHeaderRevenueThisYtd.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_this_ytd.toString()
        if(filterBinding.revenueSortHeaderRevenueLastYtd.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_last_ytd.toString()
        if(filterBinding.revenueSortHeaderRevenueYoyYtd.isSelected) filterUtil.sortingTarget = R.string.revenue_table_header_revenue_yoy_ytd.toString()

        filterUtil.update()
        if (binding.drawer.isDrawerVisible(GravityCompat.END)) {
            binding.drawer.closeDrawer(GravityCompat.END)
        }
    }

    private fun resetFilter(){
        filterUtil.reset()
        filterUtil.update()
        if (binding.drawer.isDrawerVisible(GravityCompat.END)) {
            binding.drawer.closeDrawer(GravityCompat.END)
        }
    }

    private fun selectSortingType(target: View){
        filterBinding.revenueFilterSortTypeAsc.isSelected = false
        filterBinding.revenueFilterSortTypeDes.isSelected = false
        target.isSelected = true
    }

    private fun selectSortingTarget(target: View){
        filterBinding.revenueSortHeaderStockId.isSelected = false
        filterBinding.revenueSortHeaderCompanyType.isSelected = false
        filterBinding.revenueSortHeaderRevenueThisMonth.isSelected = false
        filterBinding.revenueSortHeaderRevenueLastMonth.isSelected = false
        filterBinding.revenueSortHeaderRevenueMom.isSelected = false
        filterBinding.revenueSortHeaderRevenueLastYear.isSelected = false
        filterBinding.revenueSortHeaderRevenueYoy.isSelected = false
        filterBinding.revenueSortHeaderRevenueThisYtd.isSelected = false
        filterBinding.revenueSortHeaderRevenueLastYtd.isSelected = false
        filterBinding.revenueSortHeaderRevenueYoyYtd.isSelected = false
        target.isSelected = true
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