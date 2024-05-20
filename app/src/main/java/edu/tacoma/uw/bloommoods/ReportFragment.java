package edu.tacoma.uw.bloommoods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mReportBinding = FragmentReportBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();
        mJournalEntries = new ArrayList<>();
        mGridView = mReportBinding.calendarGridView;
        List<CalendarCell> calendarCells = generateCalendarCells();
        CalendarAdapter adapter = new CalendarAdapter(getActivity(), calendarCells);
        mGridView.setAdapter(adapter);
        mReportBinding.PieChartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = ReportFragmentDirections.actionNavReportToPieChartFragment();
                Navigation.findNavController(v).navigate(action);
            }
        });
        return mReportBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mJournalViewModel.getEntriesByDate(userId, currentMonth, currentYear);
            }
        });

        mJournalViewModel.getDateEntries().observe(getViewLifecycleOwner(), entries -> {
            Log.i("ReportFragment", "Observing entries" + entries);
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
                    List<Date> dates = generateDates(currentMonth, currentYear);
                    CalendarAdapter adapter = new CalendarAdapter(getContext(), dates, mJournalEntries);
                    mGridView.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.i("ReportFragment", "Error " + entries);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mReportBinding = null;
    }

    private List<CalendarCell> generateCalendarCells() {
        List<CalendarCell> calendarCells = new ArrayList<>();

        // Use Calendar class to get the current month and year
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the month

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            calendarCells.add(new CalendarCell(i, R.mipmap.happy));
        }

        return calendarCells;
    }

    private List<Date> generateDates(int month, int year) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        // Get the first day of the month
        int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Add dates from the previous month to fill the first week
        if (firstDayOfMonth > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth);
            for (int i = 0; i < firstDayOfMonth; i++) {
                dates.add(calendar.getTime());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        // Add dates for the current month
        calendar.set(year, month, 1);
        int maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < maxDaysInMonth; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Add dates from the next month to fill the remaining grid
        int remainingDays = 42 - dates.size(); // 42 cells for a 7x6 grid
        for (int i = 0; i < remainingDays; i++) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

}