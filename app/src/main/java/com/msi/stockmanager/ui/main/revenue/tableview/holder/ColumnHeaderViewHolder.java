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

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.evrencoskun.tableview.sort.SortState;
import com.msi.stockmanager.R;
import com.msi.stockmanager.ui.main.revenue.tableview.model.ColumnHeader;

/**
 * Created by evrencoskun on 23/10/2017.
 */

public class ColumnHeaderViewHolder extends AbstractSorterViewHolder {

    private static final String LOG_TAG = ColumnHeaderViewHolder.class.getSimpleName();

    @NonNull
    public final LinearLayout column_header_container;
    @NonNull
    public final TextView column_header_textview;
    @NonNull
    public final ImageButton column_header_sortButton;
    @Nullable
    public final ITableView tableView;

    public ColumnHeaderViewHolder(@NonNull View itemView, @Nullable ITableView tableView) {
        super(itemView);
        this.tableView = tableView;
        column_header_textview = itemView.findViewById(R.id.column_header_textView);
        column_header_container = itemView.findViewById(R.id.column_header_container);
        column_header_sortButton = itemView.findViewById(R.id.column_header_sortButton);

        // Set click listener to the sort button
//        column_header_container.setOnClickListener(mSortButtonClickListener);
//        column_header_sortButton.setOnClickListener(mSortButtonClickListener);
        column_header_sortButton.setClickable(false);
    }

    /**
     * This method is calling from onBindColumnHeaderHolder on TableViewAdapter
     */
    public void setColumnHeader(@Nullable ColumnHeader columnHeader) {
        column_header_textview.setText(String.valueOf(columnHeader.getData()));

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can remove them.

        // It is necessary to remeasure itself.
        column_header_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        column_header_textview.requestLayout();

    }

    @NonNull
    private final View.OnClickListener mSortButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSortState() == SortState.ASCENDING) {
                tableView.sortColumn(getBindingAdapterPosition(), SortState.DESCENDING);
            } else if (getSortState() == SortState.DESCENDING) {
                tableView.sortColumn(getBindingAdapterPosition(), SortState.ASCENDING);
            } else {
                // Default one
                tableView.sortColumn(getBindingAdapterPosition(), SortState.DESCENDING);
            }

        }
    };

    @Override
    public void onSortingStatusChanged(@NonNull SortState sortState) {
        Log.e(LOG_TAG, " + onSortingStatusChanged: x:  " + getBindingAdapterPosition() + ", " +
                "old state: " + getSortState() + ", current state: " + sortState + ", " +
                "visibility: " + column_header_sortButton.getVisibility());

        super.onSortingStatusChanged(sortState);

        // It is necessary to remeasure itself.
        column_header_container.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;

        controlSortState(sortState);

        Log.e(LOG_TAG, " - onSortingStatusChanged: x:  " + getBindingAdapterPosition() + ", " +
                "old state: " + getSortState() + ", current state: " + sortState + ", " +
                "visibility: " + column_header_sortButton.getVisibility());

        column_header_textview.requestLayout();
        column_header_sortButton.requestLayout();
        column_header_container.requestLayout();
        itemView.requestLayout();
    }

    private void controlSortState(@NonNull SortState sortState) {
        if (sortState == SortState.ASCENDING) {
            column_header_sortButton.setVisibility(View.VISIBLE);
            column_header_sortButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);

        } else if (sortState == SortState.DESCENDING) {
            column_header_sortButton.setVisibility(View.VISIBLE);
            column_header_sortButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        } else {
            column_header_sortButton.setVisibility(View.INVISIBLE);
        }
    }
}
