package com.msi.stockmanager.ui.main.form

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.k0shk0sh.compose.easyforms.*
import com.msi.stockmanager.R
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.ui.main.pager.PagerActivity
import com.msi.stockmanager.ui.theme.StockManagerTheme
import javax.annotation.Nullable

val TAG = "FormActivity"

class FormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


@Composable
fun EmailTextField(easyForm: EasyForms) {
    val emailTextFieldState = easyForm.getTextFieldState("email", EmailValidationType)
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

@Composable
fun Space(padding: Dp = 16.dp) {
    Spacer(modifier = Modifier.padding(padding))
}

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
                    StockIdSelector(selected = "2330", easyForm = easyForm)
                    Space()
                    EmailTextField(easyForm)
                    Space()
                }
                Button(
                    shape = RoundedCornerShape(0.dp),
                    onClick = {
                        activity?.startActivity(Intent(activity, PagerActivity::class.java))
                    },
                    modifier = Modifier
                        .height(64.dp)
                        .constrainAs(button) {
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                        },
                ) {
                    Text(stringResource(id = R.string.btn_check), fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
fun PreviewTest(){
    BuildForm()
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    PreviewTest()
}