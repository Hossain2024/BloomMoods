package edu.tacoma.uw.bloommoods;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MonthYearPicker extends PopupWindow implements View.OnClickListener{
    private final Button monthSelector;
    private final ConstraintLayout monthButtonsLayout;
    private final TextView monthYearTextView;
    private Button prevSelectedMonthButton;
    // Map of month abbreviations to full month names
    private static final Map<String, String> monthMap = createMonthMap();

    @SuppressLint("ClickableViewAccessibility")
    public MonthYearPicker(Context context, TextView monthYear) {
        super(context);
        this.monthYearTextView = monthYear;

        View view = LayoutInflater.from(context).inflate(R.layout.date_picker, null);
        setContentView(view);

        // Initialize buttons and set click listeners
        monthSelector = view.findViewById(R.id.monthSelectionButton);
        TextView yearText = view.findViewById(R.id.yearDateTextView);
        Button submitDate = view.findViewById(R.id.selectedDateButton);
        monthButtonsLayout = view.findViewById(R.id.monthsLayout);

        monthSelector.setOnClickListener(this);
        submitDate.setOnClickListener(this);
        setOnClickListenerForMonthButtons();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String monthAbbr = monthFormat.format(calendar.getTime());
        monthSelector.setText(monthAbbr);
        yearText.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        int maxWidth = 1170;
        int setWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        int width = Math.min(maxWidth, setWidth);
        setWidth(width);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // Make pop-up window close when clicked outside
        setOutsideTouchable(true);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismiss();
                return true;
            }
            return false;
        });
    }

    private static Map<String, String> createMonthMap() {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("Jan", "January");
        monthMap.put("Feb", "February");
        monthMap.put("Mar", "March");
        monthMap.put("Apr", "April");
        monthMap.put("May", "May");
        monthMap.put("Jun", "June");
        monthMap.put("Jul", "July");
        monthMap.put("Aug", "August");
        monthMap.put("Sep", "September");
        monthMap.put("Oct", "October");
        monthMap.put("Nov", "November");
        monthMap.put("Dec", "December");
        return monthMap;
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
        } else if (viewId == R.id.selectedDateButton) {
            String selectedMonthAbbr = monthSelector.getText().toString();
            String selectedDate = monthMap.get(selectedMonthAbbr) + " 2024    ⓥ";
            monthYearTextView.setText(selectedDate);
            dismiss();
        } else {
            // Handle click for month buttons
            Button selectedMonthButton = ((Button) view);
            selectedMonthButton.setSelected(true);
            if (prevSelectedMonthButton != null) {
                prevSelectedMonthButton.setSelected(false);
            }
            prevSelectedMonthButton = selectedMonthButton;
            String selectedMonth = selectedMonthButton.getText().toString();
            monthSelector.setText(selectedMonth);
        }
    }
}
