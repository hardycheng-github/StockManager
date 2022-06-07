package com.msi.stockmanager.ui.main.form

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
import com.msi.stockmanager.components.autocomplete.AutoCompleteBox
import com.msi.stockmanager.components.autocomplete.utils.AutoCompleteSearchBarTag
import com.msi.stockmanager.components.searchbar.TextSearchBar
import com.msi.stockmanager.data.DateUtil
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.data.transaction.TransType
import java.util.*

val itemHeight = 70.dp

@ExperimentalAnimationApi
@Composable
fun StockIdSelector(
    easyForm: EasyForms,
    default: String = "",
){
    val state = easyForm.addAndGetCustomState(FormKeys.STOCK_SELECTOR, EasyFormsStockSelectorState())

    var value by remember { mutableStateOf("") }
    var isFocus by remember { mutableStateOf(false)}
    if(default.isNotEmpty()){
        for(stock in StockUtil.stockList){
            if(stock.stockId == default){
                value = stock.getStockNameWithId()
                state.onValueChangedCallback(stock)
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(0.dp, itemHeight)) {
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
                placeholder = stringResource(id = R.string.hint_stock_search),
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
                }
            ) {
                state.errorState.value == EasyFormsErrorState.INVALID
            }
        }
    }

}

@ExperimentalFoundationApi
@Composable
fun IntegerSelector(
    easyForm: EasyForms,
    title:String,
    default: Int = 0,
    range: IntRange = 0..999999,
    step: Int = 1,
    key:Any,
    showButtons: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
){
    val state = easyForm.addAndGetCustomState(key, IntSelectorState(range, default))
    var numStr by remember { mutableStateOf(state.state.value)}
    val btnSize = 32.dp
    val view = LocalView.current
//    state.onValueChangedCallback(numStr)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(0.dp, itemHeight)){
        TextTitle(title)
        ConstraintLayout (modifier = Modifier.fillMaxWidth()){
            val (sub, text, add) = createRefs()
            if(showButtons) {
                IconButton(
                    onClick = {
                        try {
                            var n = numStr.toInt() - step
                            if (n < range.start) n = range.start
                            numStr = n.toString()
                        } catch (e: Exception) {
                            numStr = range.start.toString()
                        }
                        state.onValueChangedCallback(numStr)
                    },
                    modifier = Modifier.constrainAs(sub) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                    enabled = numStr.toIntOrNull() != null && numStr.toInt() > range.start
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircle,
                        contentDescription = "Minus",
                        Modifier.size(btnSize)
                    )
                }
            }
            OutlinedTextField(
                trailingIcon = trailingIcon,
                value = state.state.value,
                onValueChange = {
                    val maxLength = range.endInclusive.toString().length
                    numStr = it
                    if(numStr.length > maxLength) numStr = numStr.substring(0, maxLength)
                    state.onValueChangedCallback(numStr)
                },
//                trailingIcon = {
//                    IconButton(onClick = {
//                        numStr.value = "0"
//                        view.clearFocus()
//                    }) {
//                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
//                    }
//                },
                isError = state.errorState.value == EasyFormsErrorState.INVALID,
                keyboardActions = KeyboardActions(onDone = { view.clearFocus() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.constrainAs(text){
                    start.linkTo(sub.end)
                    end.linkTo(add.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            )
            if(showButtons) {
                IconButton(
                    onClick = {
                        try {
                            var n = numStr.toInt() + step
                            if (n > range.endInclusive) n = range.endInclusive
                            numStr = n.toString()
                        } catch (e: Exception) {
                            numStr = range.start.toString()
                        }
                        state.onValueChangedCallback(numStr)
                    },
                    modifier = Modifier.constrainAs(add) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                    enabled = numStr.toIntOrNull() != null && numStr.toInt() < range.endInclusive
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Add",
                        Modifier.size(btnSize)
                    )
                }
            }
        }
    }

}

class IntSelectorState(range: IntRange, default: Int):
    EasyFormsState<MutableState<String>, String>() {

    override val state: MutableState<String> = mutableStateOf(default.toString())

    override val onValueChangedCallback: (String) -> Unit = {
        state.value = it
        var n = it.toIntOrNull()
        errorState.value = when(n != null && n in range){
            true -> EasyFormsErrorState.VALID
            false -> EasyFormsErrorState.INVALID
        }
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return IntSelectorResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value,
        )
    }

    init{
        onValueChangedCallback(state.value)
    }

}

data class IntSelectorResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: String,
) : EasyFormsResult.GenericStateResult<String>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)

@ExperimentalFoundationApi
@Composable
fun DoubleSelector(
    easyForm: EasyForms,
    title:String,
    default: Double = 0.0,
    range: ClosedFloatingPointRange<Double> = 0.0..999999.0,
    step: Double = 1.0,
    precision:Int = 2,
    key:Any,
    showButtons: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
){
    val state = easyForm.addAndGetCustomState(key, DoubleSelectorState(range, default, precision))
    var numStr by remember { mutableStateOf(state.state.value) }
    val btnSize = 32.dp
    val view = LocalView.current
//    state.onValueChangedCallback(numStr)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(0.dp, itemHeight)){
        TextTitle(title)
        ConstraintLayout (modifier = Modifier.fillMaxWidth()){
            val (sub, text, add) = createRefs()
            if(showButtons) {
                IconButton(
                    onClick = {
                        try {
                            var n = numStr.toDouble() - step
                            if (n < range.start) n = range.start
                            numStr = String.format("%." + precision + "f", n);
                        } catch (e: Exception) {
                            numStr = String.format("%." + precision + "f", range.start);
                        }
                        state.onValueChangedCallback(numStr)
                    },
                    modifier = Modifier.constrainAs(sub) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                    enabled = numStr.toDoubleOrNull() != null && numStr.toDouble() > range.start
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircle,
                        contentDescription = "Minus",
                        Modifier.size(btnSize)
                    )
                }
            }
            OutlinedTextField(
                trailingIcon = trailingIcon,
                value = state.state.value,
                onValueChange = {
                    val maxLength = String.format("%."+precision+"f", range.endInclusive).length
                    numStr = it
                    if(numStr.length > maxLength) numStr = numStr.substring(0, maxLength)
                    else state.onValueChangedCallback(numStr)
                },
//                trailingIcon = {
//                    IconButton(onClick = {
//                        numStr.value = String.format("%."+precision+"f", 0.0);
//                        view.clearFocus()
//                    }) {
//                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
//                    }
//                },
                isError = state.errorState.value == EasyFormsErrorState.INVALID,
                keyboardActions = KeyboardActions(onDone = { view.clearFocus() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.constrainAs(text){
                    start.linkTo(sub.end)
                    end.linkTo(add.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                },
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
            if(showButtons) {
                IconButton(
                    onClick = {
                        try {
                            var n = numStr.toDouble() + step
                            if (n > range.endInclusive) n = range.endInclusive
                            numStr = String.format("%." + precision + "f", n)
                        } catch (e: Exception) {
                            numStr = String.format("%." + precision + "f", range.start);
                        }
                        state.onValueChangedCallback(numStr)
                    },
                    modifier = Modifier.constrainAs(add) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                    enabled = numStr.toDoubleOrNull() != null && numStr.toDouble() < range.endInclusive
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Add",
                        Modifier.size(btnSize)
                    )
                }
            }
        }
    }

}

class DoubleSelectorState(range: ClosedFloatingPointRange<Double>, default: Double, precision: Int):
    EasyFormsState<MutableState<String>, String>() {

    override val state: MutableState<String> = mutableStateOf(String.format("%."+precision+"f", default))

    override val onValueChangedCallback: (String) -> Unit = {
        state.value = it
        var n = it.toDoubleOrNull()
        errorState.value = when(n != null && n in range){
            true -> EasyFormsErrorState.VALID
            false -> EasyFormsErrorState.INVALID
        }
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return DoubleSelectorResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value,
        )
    }

    init{
        onValueChangedCallback(state.value)
    }

}

data class DoubleSelectorResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: String,
) : EasyFormsResult.GenericStateResult<String>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)


@Composable
fun DatePicker(easyForm: EasyForms, title: String, default: Long = Date().time, key:Any){
    val state = easyForm.addAndGetCustomState(key, DatePickerState(default))
    val defaultDate = Date(default)
    var dateStr by remember { mutableStateOf(DateUtil.toDateString(default))}
    val mDatePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _: DatePicker, y: Int, _m: Int, d: Int ->
            val m = _m+1
            Log.d(TAG, "date selected: $y-$m-$d")
            val selected = Date(y-1900, m-1, d)
            dateStr = DateUtil.toDateString(selected.time)
            state.onValueChangedCallback(selected.time)
        }, defaultDate.year+1900, defaultDate.month, defaultDate.date
    )
//    state.onValueChangedCallback(default)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(0.dp, itemHeight)) {
        TextTitle(title)
        OutlinedTextField(
            value = dateStr,
            onValueChange = {
                Log.d(TAG, "date onValueChange: $it")
            },
            readOnly = true,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            modifier = Modifier.clickable { mDatePickerDialog.show() },
            enabled = false,
            colors =  TextFieldDefaults.textFieldColors(
                disabledTextColor = LocalContentColor.current.copy(LocalContentAlpha.current),
            )
        )
    }
}

class DatePickerState(default: Long): EasyFormsState<MutableState<Long>, Long>(){
    override val state: MutableState<Long> = mutableStateOf(default)

    override val onValueChangedCallback: (Long) -> Unit = {
        state.value = it
        errorState.value = when(it > 0){
            true -> EasyFormsErrorState.VALID
            false -> EasyFormsErrorState.INVALID
        }
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return DatePickerResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value,
        )
    }

    init{
        onValueChangedCallback(state.value)
    }
}

data class DatePickerResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: Long,
) : EasyFormsResult.GenericStateResult<Long>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)

@Composable
fun TransTypeSelector(easyForm: EasyForms, title: String, items: List<Int>, default: Int = items[0], key:Any){
    val state = easyForm.addAndGetCustomState(key, TransTypeSelectorState(default))
//    state.onValueChangedCallback(default)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(0.dp, itemHeight)) {
        TextTitle(title)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.horizontalScroll(rememberScrollState())){
            for(type in items){
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .clickable { state.onValueChangedCallback(type) }){
                    RadioButton(
                        selected = state.state.value == type,
                        onClick = {},
                        enabled = false,
                        colors = RadioButtonDefaults.colors(
                            disabledColor = MaterialTheme.colors.secondary
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(text = transTypeToString(type), modifier = Modifier.padding(end=8.dp))
                }
            }
        }
    }
}

@Composable
fun transTypeToString(type: Int): String {
    return when(type){
        TransType.TRANS_TYPE_STOCK_BUY -> stringResource(id = R.string.TRANS_TYPE_STOCK_BUY)
        TransType.TRANS_TYPE_STOCK_SELL -> stringResource(id = R.string.TRANS_TYPE_STOCK_SELL)
        TransType.TRANS_TYPE_CASH_IN -> stringResource(id = R.string.TRANS_TYPE_CASH_IN)
        TransType.TRANS_TYPE_CASH_OUT -> stringResource(id = R.string.TRANS_TYPE_CASH_OUT)
        TransType.TRANS_TYPE_CASH_DIVIDEND -> stringResource(id = R.string.TRANS_TYPE_CASH_DIVIDEND)
        TransType.TRANS_TYPE_STOCK_DIVIDEND -> stringResource(id = R.string.TRANS_TYPE_STOCK_DIVIDEND)
        TransType.TRANS_TYPE_CASH_REDUCTION -> stringResource(id = R.string.TRANS_TYPE_CASH_REDUCTION)
        TransType.TRANS_TYPE_STOCK_REDUCTION -> stringResource(id = R.string.TRANS_TYPE_STOCK_REDUCTION)
        else -> ""
    }
}

class TransTypeSelectorState(default: Int): EasyFormsState<MutableState<Int>, Int>(){
    override val state: MutableState<Int> = mutableStateOf(default)

    override val onValueChangedCallback: (Int) -> Unit = {
        state.value = it
        errorState.value = EasyFormsErrorState.VALID
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return TransTypeSelectorResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value,
        )
    }

    init{
        onValueChangedCallback(state.value)
    }
}

data class TransTypeSelectorResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: Int,
) : EasyFormsResult.GenericStateResult<Int>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)

@Composable
fun TextTitle(title:String){
    Text(text=title, modifier = Modifier.fillMaxWidth(0.3f), style=MaterialTheme.typography.subtitle1)
}