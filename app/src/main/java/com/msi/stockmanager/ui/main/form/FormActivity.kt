package com.msi.stockmanager.ui.main.form

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
import com.msi.stockmanager.data.Constants
import com.msi.stockmanager.data.profile.Profile
import com.msi.stockmanager.data.stock.IStockApi
import com.msi.stockmanager.data.stock.StockApi
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.data.transaction.ITransApi
import com.msi.stockmanager.data.transaction.TransApi
import com.msi.stockmanager.data.transaction.TransType
import com.msi.stockmanager.data.transaction.Transaction
import com.msi.stockmanager.ui.main.overview.OverviewActivity
import com.msi.stockmanager.ui.theme.StockManagerTheme
import javax.annotation.Nullable
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

val TAG = "FormActivity"
val transTypelist = listOf(TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_STOCK_SELL)
var transObj: Transaction = Transaction()
var transApi: ITransApi? = null
var easyFormObj: EasyForms? = null
var activity: Activity? = null
var stockPriceLast = 0.0
var stockAmountLast = 0

class FormActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        try {
            transObj = intent.getSerializableExtra(Constants.EXTRA_TRANS_OBJECT) as Transaction
        } catch(e: Exception){
            transObj = Transaction()
            transObj.trans_type = transTypelist[0]
        }
        transApi = TransApi(activity)
        setContent {
            StockManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BuildForm()
                }
            }
        }
    }
}

//@Composable
//fun Space(padding: Dp = 16.dp) {
//    Spacer(modifier = Modifier.size(padding))
//}

@ExperimentalFoundationApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BuildForm(){
    val stockApi: IStockApi = StockApi(LocalContext.current)
    var easyFormAllValid by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.add_trans_stock))
                },
                navigationIcon = { IconButton(onClick = {
                    activity?.onBackPressed()
                }) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }},
                actions = {
                    IconButton(onClick = {
                        submitForm()
                    }, enabled = easyFormAllValid) {
                        Icon(Icons.Filled.Check, "check", tint = when(easyFormAllValid){
                            true -> MaterialTheme.colors.onPrimary
                            false -> MaterialTheme.colors.onPrimary
                                .copy(alpha = 0.3f)
                                .compositeOver(MaterialTheme.colors.primary)
                        })
                    }
                }
            )
        },
    ){
        BuildEasyForms { easyForm ->
            easyFormObj = easyForm
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                // Create references for the composables to constrain
                val (form, button) = createRefs()
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .constrainAs(form) {
                            top.linkTo(parent.top)
                            bottom.linkTo(button.top)
                            height = Dimension.fillToConstraints
                            width = Dimension.matchParent
                        }
                ) {
                    val transTypeState = easyForm.addAndGetCustomState(FormKeys.TRANS_TYPE, TransTypeSelectorState(transObj.trans_type))
                    val stockSelectorState = easyForm.addAndGetCustomState(
                        FormKeys.STOCK_SELECTOR,
                        EasyFormsStockSelectorState()
                    )
                    val stockPriceRange = 0.0..999999.0
                    val stockPricePrecision = 2
                    val stockPriceState = easyForm.addAndGetCustomState(
                        FormKeys.STOCK_PRICE,
                        DoubleSelectorState(stockPriceRange, transObj.stock_price, stockPricePrecision)
                    )
                    val stockPriceValue = stockPriceState.state.value.toDoubleOrNull()
                    val stockAmountRange = 1..999999
                    val stockAmountState = easyForm.addAndGetCustomState(FormKeys.STOCK_AMOUNT, IntSelectorState(stockAmountRange, transObj.stock_amount))
                    val stockAmountValue = stockAmountState.state.value.toIntOrNull()
                    val stockFeeRange = Profile.fee_minimum..999999
                    val stockFeeState = easyForm.addAndGetCustomState(FormKeys.FEE, IntSelectorState(stockFeeRange, transObj.fee))
                    val stockTaxRange = 0..999999
                    val stockTaxState = easyForm.addAndGetCustomState(FormKeys.TAX, IntSelectorState(stockTaxRange, transObj.tax))

                    var trailingVisible = stockSelectorState.state.value.stockId.isNotEmpty()
                    var trailingSyncing by remember{ mutableStateOf(false)}
                    var priceSyncFormCloud :@Composable (() -> Unit)? = null

                    TransTypeSelector(easyForm,
                        key = FormKeys.TRANS_TYPE,
                        title = stringResource(id = R.string.trans_type),
                        items = transTypelist,
                        default = transObj.trans_type
                    )
                    StockIdSelector(easyForm, transObj.stock_id)
                    DatePicker(easyForm,
                        title = stringResource(id = R.string.trans_date),
                        key=FormKeys.TRANS_DATE,
                        default = transObj.trans_time
                    )

                    if(trailingVisible) {
                        priceSyncFormCloud = {
                            if (trailingVisible) {
                                IconButton(onClick = {
                                    trailingSyncing = true
                                    if (stockPriceState != null && stockSelectorState != null) {
                                        stockApi.getRegularStockPrice(stockSelectorState.state.value.stockId) {
                                            stockPriceState.state.value =
                                                String.format(
                                                    "%." + stockPricePrecision + "f",
                                                    it.lastPrice
                                                )
                                            stockPriceState.onValueChangedCallback(stockPriceState.state.value)
                                            trailingSyncing = false
                                        }
                                    } else {
                                        trailingSyncing = false
                                    }
                                }, enabled = !trailingSyncing) {
                                    Icon(
                                        Icons.Outlined.CloudDownload,
                                        contentDescription = "sync"
                                    )
                                }
                            }
                        }
                    }
                    DoubleSelector(easyForm,
                        title = stringResource(id = R.string.trans_stock_price),
                        default = transObj.stock_price,
                        key=FormKeys.STOCK_PRICE,
                        precision = stockPricePrecision,
                        trailingIcon = priceSyncFormCloud,
                        showButtons = false,
                    )
                    IntegerSelector(easyForm,
                        title = stringResource(id = R.string.trans_stock_amount),
                        default = transObj.stock_amount, range =stockAmountRange, step =1000,
                        key =FormKeys.STOCK_AMOUNT)
                    IntegerSelector(easyForm,
                        title = stringResource(id = R.string.trans_stock_fee),
                        default = transObj.fee,
                        range = stockFeeRange,
                        key =FormKeys.FEE,
                        showButtons = false,
                    )
                    if(stockPriceValue != null && stockAmountValue != null) {
                        if(stockPriceValue != stockPriceLast || stockAmountValue != stockAmountLast) {
                            stockPriceLast = stockPriceValue
                            stockAmountLast = stockAmountValue
                            val feeStr = Integer.max(
                                floor(
                                    stockPriceValue
                                            * Profile.fee_rate
                                            * Profile.fee_discount
                                            * stockAmountValue
                                ).toInt(), Profile.fee_minimum
                            ).toString()
                            stockFeeState.onValueChangedCallback(feeStr)

                            if (stockPriceValue != null && stockAmountValue != null) {
                                val taxStr = floor(
                                    stockPriceValue
                                            * Profile.tax_rate
                                            * stockAmountValue
                                )
                                    .toInt()
                                    .toString()
                                stockTaxState.onValueChangedCallback(taxStr)
                            }
                        }
                    }
                    if(transTypeState.state.value == TransType.TRANS_TYPE_STOCK_SELL){
                        IntegerSelector(easyForm,
                            title = stringResource(id = R.string.trans_stock_tax),
                            default = transObj.tax,
                            range = stockTaxRange,
                            key =FormKeys.TAX,
                            showButtons = false,
                        )
                    }
                }
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        submitForm()
                    },
                    modifier = Modifier
                        .constrainAs(button) {
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                            height = Dimension.value(48.dp)
                        },
                    enabled = easyFormAllValid
                ) {
                    Text(stringResource(id = R.string.btn_check), style=MaterialTheme.typography.h6)
                }
                easyFormAllValid = easyForm.observeFormStates().value.all {
                    it.value == EasyFormsErrorState.VALID
                }
            }
        }
    }
}

fun submitForm(){
    if(easyFormObj != null){
        easyFormObj!!.formData().forEach {
            if(it.key == FormKeys.STOCK_SELECTOR){
                transObj.stock_id = (it as EasyFormsStockSelectorResult).value
                val info = StockUtil.stockMap.getOrDefault(transObj.stock_id, null)
                if (info != null) {
                    transObj.stock_name = info.stockName
                }
            } else if(it.key == FormKeys.TRANS_TYPE){
                transObj.trans_type = (it as TransTypeSelectorResult).value
            } else if(it.key == FormKeys.TRANS_DATE){
                transObj.trans_time = (it as DatePickerResult).value
            } else if(it.key == FormKeys.STOCK_PRICE){
                transObj.stock_price = (it as DoubleSelectorResult).value.toDoubleOrNull()!!
            } else if(it.key == FormKeys.STOCK_AMOUNT){
                transObj.stock_amount = (it as IntSelectorResult).value.toIntOrNull()!!
            } else if(it.key == FormKeys.FEE){
                transObj.fee = (it as IntSelectorResult).value.toIntOrNull()!!
            } else if(it.key == FormKeys.TAX &&
                transObj.trans_type == TransType.TRANS_TYPE_STOCK_SELL){
                transObj.tax = (it as IntSelectorResult).value.toIntOrNull()!!
            }
        }
        transObj.cash_amount = floor(transObj.stock_amount * transObj.stock_price).toInt()
        if(transObj.trans_type == TransType.TRANS_TYPE_STOCK_BUY){
            transObj.cash_amount = -transObj.cash_amount
        }
        transObj.cash_amount = transObj.cash_amount - transObj.fee - transObj.tax
        if(transObj.isIdValid){
            transApi?.updateTrans(transObj.trans_id, transObj)
        } else {
            transApi?.addTrans(transObj)
        }
    }

    activity?.finish()
}

@ExperimentalFoundationApi
@Composable
fun PreviewTest(){
    BuildForm()
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun Preview() {
    PreviewTest()
}