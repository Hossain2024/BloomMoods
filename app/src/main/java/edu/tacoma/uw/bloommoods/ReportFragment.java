package edu.tacoma.uw.bloommoods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
    //private FragmentPieChartBinding pieChartBinding;
    private UserViewModel mUserViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mReportBinding = FragmentReportBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();

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
            // For demonstration purposes, let's assume all days have the same image
            // Replace R.drawable.default_image with the appropriate image resource IDs
            calendarCells.add(new CalendarCell(i, R.mipmap.happy));
        }

        return calendarCells;
    }
}