package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.databinding.FragmentPieChartBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentReportBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {
    private GridView mGridView;
    private FragmentReportBinding mReportBinding;
    private JournalViewModel mJournalViewModel;
    private UserViewModel mUserViewModel;
    private List<JournalEntry> mJournalEntries;
    private List<Date> mDates;
    private UserViewModel mUserViewModel;
    private ImageView excitedImageView, anxiousImageView, angryImageView,  neutralImageView, sadImageView;
    private PieChart pieChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mReportBinding = FragmentReportBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();
        mJournalEntries = new ArrayList<>();
        mDates = new ArrayList<>();
        mGridView = mReportBinding.calendarGridView;
        mReportBinding.PieChartbutton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_nav_report_to_pieChartFragment));
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // January starts at 0
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        mReportBinding.calendarDateTextView.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        mDates = generateDates(currentMonth, currentYear);
        CalendarAdapter adapter = new CalendarAdapter(getContext(), mDates, mJournalEntries);
        mGridView.setAdapter(adapter);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mJournalViewModel.getEntriesByDate(userId, currentMonth, currentYear);
            }
        });

        mJournalViewModel.getRequestCompleted().observe(getViewLifecycleOwner(), isCompleted -> {
            if (Boolean.TRUE.equals(isCompleted)) {
                mJournalViewModel.getDateEntries().observe(getViewLifecycleOwner(), entries -> {
                    Log.i("ReportFragment", "Observing entries " + entries);
                    if (!Objects.equals(entries, "No entries found")) {
                        Log.i("ReportFragment", "Entries found");
                        try {
                            JSONArray jsonArray = new JSONArray(entries);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                JournalEntry entry = mJournalViewModel.parseJsonObject(jsonObject);
                                mJournalEntries.add(entry);
                            }
                            Log.i("ReportFragment", "Journal entries length: " + mJournalEntries.size());
                            updateGridView(mJournalEntries);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.i("ReportFragment", "Error " + entries);
                    }
                });
            }
        });
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

    private List<Date> generateDates(int month, int year) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // month is 0-based in Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

        while (dates.size() < 42) { // 6 weeks * 7 days = 42 days to cover the entire grid
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    private void updateGridView(List<JournalEntry> entries) {
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), mDates, entries);
        mGridView.setAdapter(adapter);
    }

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
