package edu.tacoma.uw.bloommoods.journal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.FragmentJournalBinding;

/**
 * A Fragment that displays a list of journal entries.
 * Implements RecyclerViewInterface to handle item click events.
 * @author Chelsea Dacones
 */
public class JournalFragment extends Fragment implements RecyclerViewInterface {
    private static final String[] MONTHS = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    private final ArrayList<JournalEntry> journalEntries = new ArrayList<>();
    private final Calendar calender = Calendar.getInstance();
    private final int currentMonth = calender.get(Calendar.MONTH) + 1; // Jan starts at 0
    private FragmentJournalBinding mJournalBinding;
    private JournalViewModel mJournalViewModel;
    private UserViewModel mUserViewModel;
    private MonthYearPicker myp;
    private RecyclerView recyclerView;
    private Integer userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mJournalBinding = FragmentJournalBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        myp = new MonthYearPicker(getActivity(), mJournalBinding.monthYearTextView);
        openDateDialog();

        recyclerView = mJournalBinding.entriesRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return mJournalBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe changes to the user ID and fetch entries of current month/year when it changes
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                userId = currentUser;
                mJournalViewModel.getEntriesByDate(currentUser, currentMonth, 2024);
            }
        });

        // Observe changes to the list of journal entries and update the UI
        mJournalViewModel.getDateEntries().observe(getViewLifecycleOwner(), string -> {
            journalEntries.clear();
            Log.i("JournalFragment", "Observing entries" + string);
            if (!Objects.equals(string, "No entries found")) {
                Log.i("JournalFragment", "Entries found");
                try {
                    JSONArray jsonArray = new JSONArray(string);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JournalEntry entry = mJournalViewModel.parseJsonObject(jsonObject);
                        journalEntries.add(entry);
                    }
                    updateRecyclerView(journalEntries);
                    mJournalBinding.noEntriesTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.i("JournalFragment", "Error " + string);
                journalEntries.clear();
                updateRecyclerView(journalEntries);
                mJournalBinding.noEntriesTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mJournalBinding = null;
    }

    /**
     * Opens the date picker dialog and sets the date text view with the current/selected date.
     */
    private void openDateDialog() {
        String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calender.getTime());
        int year = calender.get(Calendar.YEAR);
        TextView monthYear = mJournalBinding.monthYearTextView;
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d    ⓥ", month, year));
        Log.i("JournalFragment monthYear", String.valueOf(monthYear));
        monthYear.setOnClickListener(v -> myp.showAsDropDown(monthYear));

        monthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String selectedMonthYear = mJournalBinding.monthYearTextView.getText().toString();
                Log.i("JournalFragment selectedMonthYear", selectedMonthYear);
                String[] parts = selectedMonthYear.split(" ");
                String selectedMonth = parts[0];
                int monthInt = getMonthInt(selectedMonth);
                // Fetch journal entries for the selected month (we've limited it to year 2024 for now)
                mJournalViewModel.getEntriesByDate(userId, monthInt, 2024);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Converts a month name to its corresponding month number (1-based).
     *
     * @param monthString The month name.
     * @return The month number.
     * @throws IllegalArgumentException if the month name is invalid.
     */
    private int getMonthInt(String monthString) {
        String month = monthString.toUpperCase(Locale.ENGLISH);
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].equals(month)) {
                return i + 1; // Make months 1-based (January is 1)
            }
        }
        throw new IllegalArgumentException("Invalid month name: " + monthString);
    }

    /**
     * Updates the RecyclerView with the specified list of journal entries.
     *
     * @param entries The list of journal entries to display.
     */
    private void updateRecyclerView(ArrayList<JournalEntry> entries) {
        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), entries, this);
        mJournalBinding.entriesRecyclerView.setAdapter(adapter);
    }

    /**
     * Handles item click events in the RecyclerView.
     *
     * @param position The position of the clicked item.
     */
    @Override
    public void onItemClick(int position) {
        JournalEntry selectedEntry = journalEntries.get(position);
        // Navigate to the entry reader fragment with the selected entry
        JournalFragmentDirections.ActionJournalFragmentToEntryReaderFragment directions =
                JournalFragmentDirections.actionJournalFragmentToEntryReaderFragment(selectedEntry);
        Navigation.findNavController(getView()).navigate(directions);
    }
}