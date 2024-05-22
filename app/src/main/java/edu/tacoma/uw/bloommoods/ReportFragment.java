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
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
    private TextView happytext, excitedtext, sadtext, anxioustext, angrytext, neutraltext;
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

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).bottomNavBarBackground();
        }

           happytext = mReportBinding.happytext;
           excitedtext= mReportBinding.excitedtext;
           angrytext= mReportBinding.angrytext;
           neutraltext = mReportBinding.neutraltext;
           sadtext = mReportBinding.sadtext;
           anxioustext = mReportBinding.anxioustext;
      
        // Initialize the PieChart
          pieChart = mReportBinding.piechart;
        // Set the data for the PieChart
        mGridView = mReportBinding.calendarGridView;
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), mDates, mJournalEntries);
        mGridView.setAdapter(adapter);

        return mReportBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).bottomNavBarBackground();
        }

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
                            updateViews();

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

    private void setData() {
        Map<Integer, Integer> moodCounts = new HashMap<>();
        moodCounts.put(R.mipmap.happy, 0);
        moodCounts.put(R.mipmap.excited, 0);
        moodCounts.put(R.mipmap.anxious, 0);
        moodCounts.put(R.mipmap.angry, 0);
        moodCounts.put(R.mipmap.neutral, 0);
        moodCounts.put(R.mipmap.sad, 0);

        // Count the moods from the journal entries
        for (JournalEntry entry : mJournalEntries) {
            int moodImage = entry.getMoodImage();
            if (moodCounts.containsKey(moodImage)) {
                moodCounts.put(moodImage, moodCounts.get(moodImage) + 1);
            }
        }

       happytext.setText(String.valueOf(moodCounts.get(R.mipmap.happy)));
       excitedtext.setText(String.valueOf(moodCounts.get(R.mipmap.excited)));
       anxioustext.setText(String.valueOf(moodCounts.get(R.mipmap.anxious)));
       angrytext.setText(String.valueOf(moodCounts.get(R.mipmap.angry)));
       neutraltext.setText(String.valueOf(moodCounts.get(R.mipmap.neutral)));
       sadtext.setText(String.valueOf(moodCounts.get(R.mipmap.sad)));

        pieChart.clearChart(); // Clear the previous data

        pieChart.addPieSlice(new PieModel("Happy", moodCounts.get(R.mipmap.happy), Color.parseColor("#D1EFC6")));
        pieChart.addPieSlice(new PieModel("Excited", moodCounts.get(R.mipmap.excited), Color.parseColor("#F8F1D7")));
        pieChart.addPieSlice(new PieModel("Anxious", moodCounts.get(R.mipmap.anxious), Color.parseColor("#C9C3EA")));
        pieChart.addPieSlice(new PieModel("Angry", moodCounts.get(R.mipmap.angry), Color.parseColor("#F4C7CA")));
        pieChart.addPieSlice(new PieModel("Neutral", moodCounts.get(R.mipmap.neutral), Color.parseColor("#DCDCDC")));
        pieChart.addPieSlice(new PieModel("Sad", moodCounts.get(R.mipmap.sad), Color.parseColor("#CFDCED")));

        // Start the animation
        pieChart.startAnimation();
    }
    private void updateViews() {
        // Update GridView
        updateGridView(mJournalEntries);
        setData();
    }

}
