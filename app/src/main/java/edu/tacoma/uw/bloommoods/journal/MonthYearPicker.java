package edu.tacoma.uw.bloommoods.journal;

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

import edu.tacoma.uw.bloommoods.R;

/**
 * Custom popup window for selecting a month.
 * NOTE: For now, user's cannot select a year.
 * @author Chelsea Dacones
 */
public class MonthYearPicker extends PopupWindow implements View.OnClickListener{
    private final Button monthSelector;
    private final ConstraintLayout monthButtonsLayout;
    private final TextView monthYearTextView;
    private Button prevSelectedMonthButton;
    private static final Map<String, String> monthMap = createMonthMap();

    /**
     * Constructs a new MonthYearPicker.
     *
     * @param context   the context in which the picker is used.
     * @param monthYear the TextView to update with the selected month and year.
     */
    @SuppressLint("ClickableViewAccessibility")
    public MonthYearPicker(Context context, TextView monthYear) {
        super(context);
        this.monthYearTextView = monthYear;

        View view = LayoutInflater.from(context).inflate(R.layout.date_picker, null);
        setContentView(view);

        // Initialize buttons
        monthSelector = view.findViewById(R.id.monthSelectionButton);
        TextView yearText = view.findViewById(R.id.yearDateTextView);
        Button submitDate = view.findViewById(R.id.selectedDateButton);
        monthButtonsLayout = view.findViewById(R.id.monthsLayout);

        // Set on click listeners
        monthSelector.setOnClickListener(this);
        submitDate.setOnClickListener(this);
        setOnClickListenerForMonthButtons();

        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String monthAbbr = monthFormat.format(calendar.getTime());
        monthSelector.setText(monthAbbr);
        yearText.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        // Set the width and height of the popup window
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

    /**
     * Creates a map of month abbreviations to full month names.
     *
     * @return a map where the keys are month abbreviations and the values are full month names.
     */
    private static Map<String, String> createMonthMap() {
        Map<String, String> monthMap = new HashMap<>();
        String[] monthAbbrs = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] monthNames = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < monthAbbrs.length; i++) {
            monthMap.put(monthAbbrs[i], monthNames[i]);
        }
        return monthMap;
    }

    /**
     * Sets click listeners for all month buttons in the layout.
     */
    private void setOnClickListenerForMonthButtons() {
        for (int i = 0; i < monthButtonsLayout.getChildCount(); i++) {
            View child = monthButtonsLayout.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(this);
            }
        }
    }

    /**
     * Handles click events for the different buttons in the popup window.
     *
     * @param view the view that was clicked.
     */
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.monthSelectionButton) {
            // Show the month selection layout
            monthButtonsLayout.setVisibility(View.VISIBLE);
        } else if (viewId == R.id.selectedDateButton) {
            // Set the selected date
            String selectedMonthAbbr = monthSelector.getText().toString();
            String selectedDate = monthMap.get(selectedMonthAbbr) + " 2024    ⓥ";
            monthYearTextView.setText(selectedDate);
            dismiss(); // Close the popup window
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
