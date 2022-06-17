package com.msi.stockmanager.ui.main.form

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.k0shk0sh.compose.easyforms.EasyFormsErrorState
import com.github.k0shk0sh.compose.easyforms.EasyFormsResult
import com.github.k0shk0sh.compose.easyforms.EasyFormsState
import com.msi.stockmanager.data.stock.StockInfo
import com.msi.stockmanager.data.stock.StockUtil

class EasyFormsStockSelectorState(default:StockInfo = StockInfo(), list: MutableList<StockInfo> = StockUtil.stockList): EasyFormsState<MutableState<StockInfo>, StockInfo>() {
    var default: StockInfo = default
    var list: MutableList<StockInfo> = list
    override val state: MutableState<StockInfo> = mutableStateOf(this.default)

    override val onValueChangedCallback: (StockInfo) -> Unit = {
        state.value = it
        errorState.value = when (it in this.list) {
            true -> EasyFormsErrorState.VALID
            false -> EasyFormsErrorState.INVALID
        }
    }

    override fun mapToResult(key: Any): EasyFormsResult {
        return EasyFormsStockSelectorResult(
            key = key,
            easyFormsErrorState = errorState.value,
            value = state.value.stockId,
        )
    }

    init {
        onValueChangedCallback(state.value)
    }

}

data class EasyFormsStockSelectorResult(
    override val key: Any,
    override val easyFormsErrorState: EasyFormsErrorState,
    override val value: String,
) : EasyFormsResult.GenericStateResult<String>(
    key = key,
    easyFormsErrorState = easyFormsErrorState,
    value = value,
)