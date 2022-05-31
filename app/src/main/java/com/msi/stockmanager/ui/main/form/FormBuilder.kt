package com.msi.stockmanager.ui.main.form

import android.content.Intent
import android.view.View
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.util.PatternsCompat
import com.github.k0shk0sh.compose.easyforms.EasyForms
import com.github.k0shk0sh.compose.easyforms.EasyFormsErrorState
import com.github.k0shk0sh.compose.easyforms.EasyFormsValidationType
import com.github.k0shk0sh.compose.easyforms.EmailValidationType
import com.msi.stockmanager.R
import com.msi.stockmanager.components.autocomplete.AutoCompleteBox
import com.msi.stockmanager.components.autocomplete.utils.AutoCompleteSearchBarTag
import com.msi.stockmanager.components.searchbar.TextSearchBar
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.ui.main.pager.PagerActivity

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
            label = stringResource(id = R.string.trans_stock_selector),
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

object IntegerValidationType : EasyFormsValidationType(
    regex = "^(\\d+)\$",
    minLength = 1,
    maxLength = 6
)

@Composable
fun StockAmountSelector(easyForm: EasyForms){
    val state = easyForm.getTextFieldState(FormKeys.STOCK_AMOUNT, IntegerValidationType)
    val value = state.state.value
    val btnSize = 32.dp
    ConstraintLayout(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ){
        val (btnSub, textContent, btnAdd) = createRefs()
        IconButton(onClick = { }, modifier = Modifier.constrainAs(btnSub){
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Minus", Modifier.size(btnSize))
        }
        OutlinedTextField(
            value = value,
            onValueChange = state.onValueChangedCallback,
            isError = state.errorState.value == EasyFormsErrorState.INVALID,
            label = { Text(stringResource(id = R.string.trans_stock_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .constrainAs(textContent){
                start.linkTo(btnSub.end)
                end.linkTo(btnAdd.start)
                height = Dimension.wrapContent
            },
            singleLine = true
        )
        IconButton(onClick = { }, modifier = Modifier.constrainAs(btnAdd){
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus", Modifier.size(btnSize))
        }
    }


}

@Composable
fun EmailTextField(easyForm: EasyForms) {
    val emailTextFieldState = easyForm.getTextFieldState(FormKeys.STOCK_AMOUNT, EmailValidationType)
    val emailState = emailTextFieldState.state
    TextField(
        value = emailState.value,
        onValueChange = emailTextFieldState.onValueChangedCallback,
        isError = emailTextFieldState.errorState.value == EasyFormsErrorState.INVALID,
        label = { Text("Email") },
        placeholder = { Text("email@example.com") },
        leadingIcon = {
            Icon(
                Icons.Outlined.Email,
                "Email",
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}