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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
import com.msi.stockmanager.data.Constants
import com.msi.stockmanager.data.profile.Profile
import com.msi.stockmanager.data.transaction.TransType
import com.msi.stockmanager.data.transaction.Transaction
import com.msi.stockmanager.ui.main.overview.OverviewActivity
import com.msi.stockmanager.ui.main.pager.PagerActivity
import com.msi.stockmanager.ui.theme.StockManagerTheme
import javax.annotation.Nullable

val TAG = "FormActivity"

class FormActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var transObj: Transaction? = null
        val transType = intent.getIntExtra(Constants.EXTRA_TRANS_TYPE, TransType.TRANS_TYPE_OTHER)
        try {
            transObj = intent.getSerializableExtra(Constants.EXTRA_TRANS_OBJECT) as Transaction
        } catch(e: Exception){}
        setContent {
            StockManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BuildForm(this)
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
fun BuildForm(@Nullable activity: Activity? = null){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(activity == null) Text("Test")
                    else Text(stringResource(R.string.title_activity_compose))
                },
                navigationIcon = { IconButton(onClick = {
                    activity?.onBackPressed()
                }) {
                    Icon(Icons.Filled.ArrowBack, "back")
                }},
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Check, "check", tint = Color.White)
                    }
                }
            )
        },
    ){
        BuildEasyForms { easyForm ->
            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                // Create references for the composables to constrain
                val (form, button) = createRefs()
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .constrainAs(form) {
                            bottom.linkTo(button.top)
                            height = Dimension.matchParent
                            width = Dimension.matchParent
                        }
                ) {
                    TransTypeSelector(easyForm, key = FormKeys.TRANS_TYPE, title = stringResource(id = R.string.trans_type), items = listOf(TransType.TRANS_TYPE_STOCK_BUY, TransType.TRANS_TYPE_STOCK_SELL))
                    StockIdSelector(easyForm)
                    DatePicker(easyForm,title = stringResource(id = R.string.trans_date), key=FormKeys.TRANS_DATE)
                    DoubleSelector(easyForm, title = stringResource(id = R.string.trans_stock_price), default = 0.0, key=FormKeys.STOCK_PRICE)
                    IntegerSelector(easyForm, title= stringResource(id = R.string.trans_stock_amount), default = 1000, range=1..999999, step=1000, key=FormKeys.STOCK_AMOUNT)
                    IntegerSelector(easyForm, title= stringResource(id = R.string.trans_stock_fee), default = Profile.fee_minimum, range= Profile.fee_minimum..999999, step=1, key=FormKeys.FEE)
                }
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        activity?.startActivity(Intent(activity, OverviewActivity::class.java))
                    },
                    modifier = Modifier
                        .height(48.dp)
                        .constrainAs(button) {
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                        },
                ) {
                    Text(stringResource(id = R.string.btn_check), style=MaterialTheme.typography.h6)
                }
            }
        }
    }
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