package com.msi.stockmanager.ui.main.revenue

import android.annotation.SuppressLint
import android.database.DataSetObserver
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.*
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.msi.stockmanager.R
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.databinding.ActivityRevenueBinding
import com.msi.stockmanager.ui.main.StockFilterAdapter

class RevenueActivity : AppCompatActivity() {
    companion object {
        private val TAG = RevenueActivity.javaClass.simpleName
    }

    lateinit var binding: ActivityRevenueBinding
    lateinit var mMenu: Menu

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            Log.d(TAG, "onStateChanged: " + event.name)
            when(event){
                ON_CREATE -> {
                    binding = ActivityRevenueBinding.inflate(layoutInflater)
                    setContentView(binding.root)
                    setSupportActionBar(binding.toolbar)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
                ON_START -> {

                }
                ON_DESTROY -> {

                }
                else -> {}
            }
        })
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