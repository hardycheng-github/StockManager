/*
 * MIT License
 *
 * Copyright (c) 2021 Evren Coşkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.msi.stockmanager.ui.main.revenue.tableview.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.msi.stockmanager.R;
import com.msi.stockmanager.data.ColorUtil;
import com.msi.stockmanager.data.FormatUtil;
import com.msi.stockmanager.ui.main.revenue.tableview.model.Cell;

/**
 * Created by evrencoskun on 23/10/2017.
 */

public class PercentViewHolder extends RevenueViewHolder {

    public PercentViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void setCell(@Nullable Cell cell) {
        float value = (float) cell.getData();
        int textColor = ColorUtil.getProfitColor(value);
        cell_textview.setTextColor(textColor);
        cell_textview.setText(FormatUtil.percent(value));
    }
}
