package com.msi.stockmanager.ui.main.form

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.k0shk0sh.compose.easyforms.EasyForms
import com.github.k0shk0sh.compose.easyforms.EasyFormsErrorState
import com.msi.stockmanager.components.autocomplete.AutoCompleteBox
import com.msi.stockmanager.components.autocomplete.utils.AutoCompleteSearchBarTag
import com.msi.stockmanager.components.searchbar.TextSearchBar
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.data.stock.StockUtil

@ExperimentalAnimationApi
@Composable
fun StockIdSelector(
    easyForm: EasyForms,
    selected: String = "",
){
    val state = easyForm.addAndGetCustomState(FormKeys.STOCK_SELECTOR, EasyFormsStockSelectorState())

    var value by remember { mutableStateOf("") }
    if(selected.isNotEmpty()){
        for(stock in StockUtil.stockList){
            if(stock.stockId == selected){
                value = stock.getStockNameWithId()
                state.onValueChangedCallback(stock)
            }
        }
    }

    AutoCompleteBox(
        items = StockUtil.stockList,
        itemContent = { stock ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stock.getStockNameWithId(), style = MaterialTheme.typography.subtitle2)
            }
        }
    ) {
        val view = LocalView.current

        onItemSelected { stock ->
            value = stock.getStockNameWithId()
            filter(value)
            view.clearFocus()
            state.onValueChangedCallback(stock)
        }

        TextSearchBar(
            modifier = Modifier.testTag(AutoCompleteSearchBarTag),
            value = value,
            label = "Search",
            onImeActionClick = {
                view.clearFocus()
            },
            onClearClick = {
                value = ""
                filter(value)
                view.clearFocus()

                state.onValueChangedCallback(StockInfo())
            },
            onFocusChanged = { focusState ->
                isSearching = focusState.isFocused
            },
            onValueChanged = { query ->
                value = query
                filter(value)
            },
            isError = {
                state.errorState.value == EasyFormsErrorState.INVALID
            }
        )
    }
}