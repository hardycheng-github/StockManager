package com.msi.stockmanager.ui.main.form

import android.app.Activity
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
import com.msi.stockmanager.data.AccountUtil
import com.msi.stockmanager.data.ApiUtil
import com.msi.stockmanager.data.Constants
import com.msi.stockmanager.data.FormatUtil
import com.msi.stockmanager.data.profile.Profile
import com.msi.stockmanager.data.stock.IStockApi
import com.msi.stockmanager.data.stock.StockApi
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.data.stock.StockUtil
import com.msi.stockmanager.data.transaction.ITransApi
import com.msi.stockmanager.data.transaction.TransApi
import com.msi.stockmanager.data.transaction.TransType
import com.msi.stockmanager.data.transaction.Transaction
import com.msi.stockmanager.ui.theme.StockManagerTheme
import kotlin.math.abs
import kotlin.math.floor

val TAG = "FormActivity"
var transTypeList: List<Int> = listOf(TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_STOCK_SELL)
val cashMinusList: List<Int> = listOf(TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_CASH_OUT)
val stockMinusList: List<Int> = listOf(TransType.TRANS_TYPE_STOCK_SELL,
                                        TransType.TRANS_TYPE_STOCK_REDUCTION,
                                        TransType.TRANS_TYPE_CASH_REDUCTION)
var transObj: Transaction = Transaction()
var transApi: ITransApi = ApiUtil.transApi
var stockApi: IStockApi = ApiUtil.stockApi
var easyFormObj: EasyForms? = null
var stockPriceLast = 0.0
var stockAmountLast = 0
var transEditType: TransEditType = TransEditType.ERROR
var titleStr = ""
var activity: Activity? = null

enum class TransEditType {
    CASH, STOCK, DIVIDEND, REDUCTION, ERROR
}

fun getTransEditType(trans: Transaction): TransEditType {
    return when(trans.trans_type){
        TransType.TRANS_TYPE_CASH_IN, TransType.TRANS_TYPE_CASH_OUT -> TransEditType.CASH
        TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_STOCK_SELL -> TransEditType.STOCK
        TransType.TRANS_TYPE_STOCK_DIVIDEND, TransType.TRANS_TYPE_CASH_DIVIDEND -> TransEditType.DIVIDEND
        TransType.TRANS_TYPE_STOCK_REDUCTION, TransType.TRANS_TYPE_CASH_REDUCTION -> TransEditType.REDUCTION
        else -> TransEditType.ERROR
    }
}

@Composable
fun init(){
    transEditType = getTransEditType(transObj)
    if(transObj.isIdValid) {
        titleStr = FormatUtil.transType(transObj.trans_type)
        transTypeList = listOf(transObj.trans_type)
        return
    }
    when (transEditType) {
        TransEditType.CASH -> {
            titleStr = stringResource(R.string.title_trans_cash)
            transTypeList = listOf(TransType.TRANS_TYPE_CASH_IN, TransType.TRANS_TYPE_CASH_OUT)
        }
        TransEditType.STOCK -> {
            titleStr = stringResource(R.string.title_trans_stock)
            transTypeList =
                listOf(TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_STOCK_SELL)
        }
        TransEditType.DIVIDEND -> {
            titleStr = stringResource(R.string.title_trans_dividend)
            transTypeList =
                listOf(TransType.TRANS_TYPE_STOCK_DIVIDEND, TransType.TRANS_TYPE_CASH_DIVIDEND)
        }
        TransEditType.REDUCTION -> {
            titleStr = stringResource(R.string.title_trans_reduction)
            transTypeList = listOf(
                TransType.TRANS_TYPE_STOCK_REDUCTION,
                TransType.TRANS_TYPE_CASH_REDUCTION
            )
        }
    }
    if(transObj.trans_type !in transTypeList){
        transObj.trans_type = transTypeList[0]
    }
}

class FormActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        try {
            transObj = intent.getSerializableExtra(Constants.EXTRA_TRANS_OBJECT) as Transaction
        } catch(e: Exception){
            transObj = Transaction(transTypeList[0])
        }

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
    init()
    var easyFormAllValid by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(titleStr)
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
                    when(transEditType){
                        TransEditType.CASH -> buildCashForm()
                        TransEditType.STOCK -> buildStockForm()
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun buildCashForm(){
    val easyForm = easyFormObj!!
    val transTypeState = easyForm.addAndGetCustomState(FormKeys.TRANS_TYPE, TransTypeSelectorState(transObj.trans_type))
    val cashAmountRange = 1..999999999
    if(!transObj.isIdValid) {
        TransTypeSelector(
            easyForm,
            key = FormKeys.TRANS_TYPE,
            title = stringResource(id = R.string.trans_type),
            items = transTypeList,
            default = transObj.trans_type
        )
    }
    DatePicker(easyForm,
        title = stringResource(id = R.string.trans_date),
        key=FormKeys.TRANS_DATE,
        default = transObj.trans_time
    )
    IntegerSelector(easyForm,
        title = stringResource(id = R.string.trans_cash),
        default = abs(transObj.cash_amount),
        range = cashAmountRange,
        key =FormKeys.CASH_AMOUNT,
        step = 1000
    )
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun buildStockForm(){
    val easyForm = easyFormObj!!
    val transTypeState = easyForm.addAndGetCustomState(FormKeys.TRANS_TYPE, TransTypeSelectorState(transObj.trans_type))
    val stockSelectorState = easyForm.addAndGetCustomState(
        FormKeys.STOCK_SELECTOR,
        EasyFormsStockSelectorState(StockUtil.stockMap.getOrDefault(transObj.stock_id, StockInfo()))
    )
    var stockAmountRange = 1..999999
    val stockRemainingMap: MutableMap<String, Int> = ApiUtil.transApi.holdingStockAmount
    if(transTypeState.state.value == TransType.TRANS_TYPE_STOCK_SELL) {
        if (stockRemainingMap.containsKey(stockSelectorState.state.value.stockId)) {
            var stockRemaining: Int = stockRemainingMap[stockSelectorState.state.value.stockId]!!
            if (transObj.isIdValid) {
                stockRemaining += abs(transObj.stock_amount)
            }
            if(stockRemaining > stockAmountRange.start) {
                stockAmountRange = stockAmountRange.start..stockRemaining
            }
        }
    }
    val stockPriceRange = 0.0..999999.0
    val stockPricePrecision = 2
    val stockPriceState = easyForm.addAndGetCustomState(
        FormKeys.STOCK_PRICE,
        DoubleSelectorState(stockPriceRange, transObj.stock_price, stockPricePrecision)
    )
    val stockPriceValue = stockPriceState.state.value.toDoubleOrNull()
    val stockAmountState = easyForm.addAndGetCustomState(FormKeys.STOCK_AMOUNT,
        IntSelectorState(stockAmountRange, abs(transObj.stock_amount)))
    val stockAmountValue = stockAmountState.state.value.toIntOrNull()
    val stockFeeRange = Profile.fee_minimum..999999
    val stockFeeState = easyForm.addAndGetCustomState(FormKeys.FEE, IntSelectorState(stockFeeRange, transObj.fee))
    val stockTaxRange = 0..999999
    var cashAmountRange = 0..999999999
    if(transTypeState.state.value == TransType.TRANS_TYPE_STOCK_BUY){
        var cashBalance: Int = AccountUtil.getAccount().cashBalance
        if (transObj.isIdValid) {
            cashBalance += abs(transObj.cash_amount)
        }
        cashAmountRange = 0..cashBalance
    }
    val stockTaxState = easyForm.addAndGetCustomState(FormKeys.TAX, IntSelectorState(stockTaxRange, transObj.tax))
    val transCashState = easyForm.addAndGetCustomState(FormKeys.CASH_AMOUNT, IntSelectorState(cashAmountRange, transObj.cash_amount))
    val transCashValue = transCashState.state.value.toIntOrNull()

    var trailingVisible = stockSelectorState.state.value.stockId.isNotEmpty()
    var trailingSyncing by remember{ mutableStateOf(false)}
    var priceSyncFormCloud :@Composable (() -> Unit)? = null

    if(!transObj.isIdValid) {
        TransTypeSelector(
            easyForm,
            key = FormKeys.TRANS_TYPE,
            title = stringResource(id = R.string.trans_type),
            items = transTypeList,
            default = transObj.trans_type,
            state = transTypeState
        )
    }
    val searchThreshold = when(transTypeState.state.value){
        TransType.TRANS_TYPE_STOCK_SELL -> 0
        else -> 1
    }

    if(transTypeState.state.value == TransType.TRANS_TYPE_STOCK_SELL){
        val stockSelectorList: MutableList<StockInfo> = mutableListOf()
        for(entry in ApiUtil.transApi.holdingStockAmount){
            StockUtil.stockMap[entry.key]?.let { stockSelectorList.add(it) }
        }
        StockIdSelector(easyForm, searchThreshold, stockSelectorList, state = stockSelectorState)
    } else {
        StockIdSelector(easyForm, searchThreshold, StockUtil.stockList, state = stockSelectorState)
    }
    DatePicker(easyForm,
        title = stringResource(id = R.string.trans_date),
        key=FormKeys.TRANS_DATE,
        default = transObj.trans_time,
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
        state = stockPriceState,
    )
    val stockAmonut = stockAmountState.state.value.toIntOrNull()
    IntegerSelector(
        easyForm,
        title = stringResource(id = R.string.trans_stock_amount),
        default = when(stockAmonut == null){
            true -> stockAmountRange.start
            false -> stockAmonut
        },
        range = stockAmountRange, step = 1000,
        key = FormKeys.STOCK_AMOUNT,
        state = stockAmountState,
    )
    IntegerSelector(easyForm,
        title = stringResource(id = R.string.trans_stock_fee),
        default = transObj.fee,
        range = stockFeeRange,
        key =FormKeys.FEE,
        showButtons = false,
        state = stockFeeState,
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
            state = stockTaxState,
        )
        IntegerSelector(easyForm,
            title = stringResource(id = R.string.trans_cash),
            default = transObj.cash_amount,
            range = 0..999999999,
            key =FormKeys.CASH_AMOUNT,
            showButtons = false,
            readOnly = true,
            state = transCashState,
        )
    } else {
        IntegerSelector(easyForm,
            title = stringResource(id = R.string.trans_cash),
            default = transObj.cash_amount,
            range = cashAmountRange,
            key =FormKeys.CASH_AMOUNT,
            showButtons = false,
            readOnly = true,
            state = transCashState,
        )
    }

    try {
        var cashAmount: Int
        if(transTypeState.state.value == TransType.TRANS_TYPE_STOCK_SELL) {
            cashAmount = floor(stockAmountState.state.value.toInt() * stockPriceState.state.value.toDouble()).toInt()
            cashAmount -= stockFeeState.state.value.toInt()
            cashAmount -= stockTaxState.state.value.toInt()
            transCashState.onValueChangedCallback(cashAmount.toString())
        } else {
            cashAmount = -floor(stockAmountState.state.value.toInt() * stockPriceState.state.value.toDouble()).toInt()
            cashAmount -= stockFeeState.state.value.toInt()
            transCashState.onValueChangedCallback((-cashAmount).toString())
        }
    } catch (e: Exception){}
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
            } else if(it.key == FormKeys.CASH_AMOUNT){
                transObj.cash_amount = (it as IntSelectorResult).value.toIntOrNull()!!
                if(transObj.trans_type in cashMinusList){
                    transObj.cash_amount = -transObj.cash_amount
                }
            }
        }

        if(transObj.isIdValid){
            transApi.updateTrans(transObj.trans_id, transObj)
        } else {
            transApi.addTrans(transObj)
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