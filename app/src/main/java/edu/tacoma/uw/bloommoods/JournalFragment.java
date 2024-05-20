package edu.tacoma.uw.bloommoods;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.databinding.FragmentJournalBinding;

public class JournalFragment extends Fragment implements RecyclerViewInterface {
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

        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
                userId = currentUser;
                mJournalViewModel.getEntriesByDate(currentUser, currentMonth, 2024);
            }
        });

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
//                    mJournalBinding.allEntriesButton.setOnClickListener(button -> updateRecyclerView(journalEntries));
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

    // Method to parse JSON object and create JournalEntry object
//    private JournalEntry parseJsonObject(JSONObject jsonObject) throws JSONException, ParseException {
//        String timestamp = jsonObject.getString("timestamp");
//        String entry = jsonObject.getString("journal_entry");
//        String mood = jsonObject.getString("mood");
//        String title = jsonObject.getString("title");
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//        Date date = sdf.parse(timestamp);
//
//        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
//        assert date != null;
//        String formattedDate = outputFormat.format(date);
//
//        int moodResourceId = getMipMapForMood(mood);
//
//        return new JournalEntry(title, formattedDate, entry, moodResourceId);
//    }

    private int getMipMapForMood(String mood) {
        switch (mood) {
            case "Excited": return R.mipmap.excited;
            case "Happy": return R.mipmap.happy;
            case "Neutral":  return R.mipmap.neutral;
            case "Sad":  return R.mipmap.sad;
            case "Anxious": return R.mipmap.anxious;
            case "Angry": return R.mipmap.angry;
            default: return R.mipmap.neutral;
        }
    }

    private void openDateDialog() {
        String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calender.getTime());
        int year = calender.get(Calendar.YEAR);
        TextView monthYear = mJournalBinding.monthYearTextView;
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d    ⓥ", month, year));
        monthYear.setOnClickListener(v -> myp.showAsDropDown(monthYear));

        monthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String selectedMonthYear = mJournalBinding.monthYearTextView.getText().toString();
                String[] parts = selectedMonthYear.split(" ");
                String selectedMonth = parts[0];
                int monthInt = getMonthInt(selectedMonth);
                mJournalViewModel.getEntriesByDate(userId, monthInt, 2024);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private int getMonthInt(String monthString) {
        return Month.valueOf(monthString.toUpperCase()).getValue();
    }

    private void updateRecyclerView(ArrayList<JournalEntry> entries) {
        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), entries, this);
        mJournalBinding.entriesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        JournalEntry selectedEntry = journalEntries.get(position);
        JournalFragmentDirections.ActionJournalFragmentToEntryReaderFragment directions =
                JournalFragmentDirections.actionJournalFragmentToEntryReaderFragment(selectedEntry);
        Navigation.findNavController(getView())
                .navigate(directions);
    }
}