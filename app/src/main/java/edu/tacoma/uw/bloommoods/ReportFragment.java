package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.tacoma.uw.bloommoods.databinding.FragmentPieChartBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentReportBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {
    private GridView mGridView;
    private FragmentReportBinding mReportBinding;
    private UserViewModel mUserViewModel;
    private ImageView excitedImageView, anxiousImageView, angryImageView,  neutralImageView, sadImageView;

    private PieChart pieChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mReportBinding = FragmentReportBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).bottomNavBarBackground();
        }

        excitedImageView = mReportBinding.excitedImageView;
        anxiousImageView = mReportBinding.anxiousImageView;
        angryImageView = mReportBinding.angryImageView;
        neutralImageView = mReportBinding.neutralImageView;
        sadImageView = mReportBinding.sadImageView;

        // Initialize the PieChart
        pieChart = mReportBinding.piechart;

        // Set the data for the PieChart
        setData();

        mGridView = mReportBinding.calendarGridView;
        List<CalendarCell> calendarCells = generateCalendarCells();
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), calendarCells);
        mGridView.setAdapter(adapter);

        return mReportBinding.getRoot();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mReportBinding = null;
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).bottomNavBarResetBg();
        }

    }

    private List<CalendarCell> generateCalendarCells() {
        List<CalendarCell> calendarCells = new ArrayList<>();

        // Use Calendar class to get the current month and year
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the month

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            // For demonstration purposes, let's assume all days have the same image
            // Replace R.drawable.default_image with the appropriate image resource IDs
            calendarCells.add(new CalendarCell(i, R.mipmap.happy));
        }

        return calendarCells;
    }

    private void setData() {
        // Dummy data for the moods (replace with your actual data)
        int happyCount = 20;
        int anxiousCount = 15;
        int angryCount = 10;
        int excitedCount = 6;
        int neutralCount = 30;
        int sadCount = 25;

        // Add data to the PieChart
        pieChart.addPieSlice(new PieModel("Happy", happyCount, Color.parseColor("#D1EFC6")));
        pieChart.addPieSlice(new PieModel("Excited", excitedCount, Color.parseColor("#F8F1D7")));
        pieChart.addPieSlice(new PieModel("Anxious", anxiousCount, Color.parseColor("#C9C3EA")));
        pieChart.addPieSlice(new PieModel("Angry", angryCount, Color.parseColor("#F4C7CA")));
        pieChart.addPieSlice(new PieModel("Neutral", neutralCount, Color.parseColor("#DCDCDC")));
        pieChart.addPieSlice(new PieModel("Sad", sadCount, Color.parseColor("#CFDCED")));

        // Start the animation
        pieChart.startAnimation();
    }

}
