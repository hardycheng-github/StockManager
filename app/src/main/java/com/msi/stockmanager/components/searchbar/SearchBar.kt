/*
 * Copyright 2021 Paulo Pereira
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msi.stockmanager.components.searchbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.msi.stockmanager.R

@Composable
fun TextSearchBar(
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String? = null,
    label: String? = null,
    onImeActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onFocusChanged: (FocusState) -> Unit = {},
    onValueChanged: (String) -> Unit,
    isError: () -> Boolean,
){
    var placeholderFun:@Composable (() -> Unit)? = null
    if(placeholder != null) placeholderFun = {Text(text = placeholder)}
    var labelFun:@Composable (() -> Unit)? = null
    if(label != null) labelFun = {Text(text = label)}

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it) },
        value = when(enabled){
            true -> value
            false -> stringResource(id = R.string.no_holding_stock)
        },
        onValueChange = {
            onValueChanged(it)
        },
        textStyle = MaterialTheme.typography.subtitle1,
        singleLine = true,
        trailingIcon = {
            if(enabled) {
                IconButton(onClick = { onClearClick() }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            onImeActionClick()
        }),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        ),
        isError = isError(),
        placeholder = placeholderFun,
        label = labelFun,
        enabled = enabled,
    )
}
