package edu.tacoma.uw.bloommoods;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MonthYearPicker extends PopupWindow implements View.OnClickListener{

    private TextView headerTextView;
    private Button monthSelector, yearSelector;
    private final ConstraintLayout monthButtonsLayout;
    private TextView monthYearTextView;
    private Spinner yearSpinner;
    private boolean isShowingMonth;
    private String selectedMonth;
    private final Button submitDate;
    private Button selectedMonthButton, prevSelectedMonthButton;

    public MonthYearPicker(Context context, TextView monthYear) {
        super(context);
        this.monthYearTextView = monthYear;

        View view = LayoutInflater.from(context).inflate(R.layout.date_picker, null);
        setContentView(view);

        // Initialize buttons and set click listeners
        monthSelector = view.findViewById(R.id.monthSelectionButton);
        yearSelector = view.findViewById(R.id.yearSelectionButton);
        submitDate = view.findViewById(R.id.selectedDateButton);
        monthButtonsLayout = view.findViewById(R.id.monthsLayout);

        monthSelector.setOnClickListener(this);
        yearSelector.setOnClickListener(this);
        submitDate.setOnClickListener(this);
        setOnClickListenerForMonthButtons();

        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void setOnClickListenerForMonthButtons() {
        for (int i = 0; i < monthButtonsLayout.getChildCount(); i++) {
            View child = monthButtonsLayout.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.monthSelectionButton) {
            monthButtonsLayout.setVisibility(View.VISIBLE);
        } else if (viewId == R.id.yearSelectionButton) {
            monthButtonsLayout.setVisibility(View.GONE);
        } else if (viewId == R.id.selectedDateButton) {
            // Update the TextView directly
            String selectedDate = selectedMonth + " 2024";
            monthYearTextView.setText(selectedDate);
            dismiss();
        } else {
            // Handle click for month buttons
            selectedMonthButton = ((Button) view);
            selectedMonthButton.setSelected(true);
            if (prevSelectedMonthButton != null) {
                prevSelectedMonthButton.setSelected(false);
            }
            prevSelectedMonthButton = selectedMonthButton;

            selectedMonth = selectedMonthButton.getText().toString();
            monthSelector.setText(selectedMonth);
        }
    }
}
