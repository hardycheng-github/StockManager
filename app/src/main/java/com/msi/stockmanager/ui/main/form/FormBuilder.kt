package com.msi.stockmanager.ui.main.form

import android.view.View
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
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
    var isFocus by remember { mutableStateOf(false)}
    if(selected.isNotEmpty()){
        for(stock in StockUtil.stockList){
            if(stock.stockId == selected){
                value = stock.getStockNameWithId()
                state.onValueChangedCallback(stock)
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        TextTitle(stringResource(id = R.string.trans_stock_selector))
        AutoCompleteBox(
            items = StockUtil.stockList,
            itemContent = { stock ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stock.getStockNameWithId(),
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        ) {
            val view = LocalView.current

            filter(value)

            onItemSelected { stock ->
                value = stock.getStockNameWithId()
                filter(value)
                view.clearFocus()
                state.onValueChangedCallback(stock)
            }

            TextSearchBar(
                modifier = Modifier.testTag(AutoCompleteSearchBarTag),
                value = value,
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
                    isFocus = focusState.isFocused
                    if(isFocus && value.isNotEmpty()) isSearching = true;
                    if(!isFocus && isSearching) isSearching = false
                },
                onValueChanged = { query ->
                    value = query
                    filter(value)
                    isSearching = isFocus && value.isNotEmpty()
                    state.onValueChangedCallback(StockInfo())
                },
                isError = {
                    state.errorState.value == EasyFormsErrorState.INVALID
                }
            )
        }
    }

}

object IntegerValidationType : EasyFormsValidationType(
    regex = "^(\\d+)\$",
    minLength = 1,
    maxLength = 6
)

@Composable
fun StockAmountSelector(easyForm: EasyForms, range: IntRange = 0..999999){
    val state = easyForm.addAndGetCustomState(FormKeys.STOCK_AMOUNT, EasyFormsNumberSelectorState(range))
    val numStr = state.state
    val btnSize = 32.dp
    val view = LocalView.current

    Row(verticalAlignment = Alignment.CenterVertically){
        TextTitle(stringResource(id = R.string.trans_stock_amount))
        ConstraintLayout (modifier = Modifier.fillMaxWidth()){
            val (sub, text, add) = createRefs()
            IconButton(onClick = {
                try {
                    var n = numStr.value.toInt()-1
                    if(n < range.first) n = range.first
                    numStr.value = n.toString()
                } catch (e: Exception){
                    numStr.value = "0"
                }
                state.onValueChangedCallback(numStr.value)
            }, modifier = Modifier.constrainAs(sub){
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }) {
                Icon(imageVector = Icons.Filled.RemoveCircle, contentDescription = "Minus", Modifier.size(btnSize))
            }
            OutlinedTextField(
                value = numStr.value,
                onValueChange = {
                    val maxLength = range.last.toString().length
                    if(it.length > maxLength) numStr.value = it.substring(0, maxLength)
                    else state.onValueChangedCallback(it)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        numStr.value = "0"
                        view.clearFocus()
                    }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                    }
                },
                isError = state.errorState.value == EasyFormsErrorState.INVALID,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.constrainAs(text){
                    start.linkTo(sub.end)
                    end.linkTo(add.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
            )
            IconButton(onClick = {
                try {
                    var n = numStr.value.toInt()+1
                    if(n > range.last) n = range.last
                    numStr.value = n.toString()
                }catch (e: Exception){
                    numStr.value = "0"
                }
                state.onValueChangedCallback(numStr.value)
            }, modifier = Modifier.constrainAs(add){
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "Add", Modifier.size(btnSize))
            }
        }
    }


}

class EasyFormsNumberSelectorState(var range: IntRange):
    EasyFormsState<MutableState<String>, String>() {

    override val state: MutableState<String> = mutableStateOf("0")

    override val onValueChangedCallback: (String) -> Unit = {
        state.value = it
        var n = it.toIntOrNull()
        errorState.value = when(n != null && n in range){
            true -> EasyFormsErrorState.VALID
            false -> EasyFormsErrorState.INVALID
        }
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return EasyFormsNumberSelectorResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value,
        )
    }

}

data class EasyFormsNumberSelectorResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: String,
) : EasyFormsResult.GenericStateResult<String>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)

@Composable
fun TextTitle(title:String){
    Text(text=title, modifier = Modifier.fillMaxWidth(0.3f), style=MaterialTheme.typography.subtitle1)
}