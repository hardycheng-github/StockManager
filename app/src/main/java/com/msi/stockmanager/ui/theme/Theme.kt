package com.msi.stockmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.msi.stockmanager.R

@Composable
fun StockManagerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = colorResource(id = R.color.main_m),
        primaryVariant = colorResource(id = R.color.main_l),
        secondary = colorResource(id = R.color.sub_m),
        secondaryVariant = colorResource(id = R.color.sub_l),
        error = colorResource(id = R.color.error)
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}