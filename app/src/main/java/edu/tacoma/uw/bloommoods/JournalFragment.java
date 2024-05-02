package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentJournalBinding;

public class JournalFragment extends Fragment implements RecyclerViewInterface {
    ArrayList<JournalEntry> journalEntries = new ArrayList<>();
    MonthYearPicker myp;
    FragmentJournalBinding journalBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        journalBinding = FragmentJournalBinding.inflate(inflater, container, false);
        myp = new MonthYearPicker(getActivity(), journalBinding.monthYearTextView);
        openDateDialog();

        RecyclerView recyclerView = journalBinding.entriesRecyclerView;
        setUpEntries();

        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), journalEntries, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return journalBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        journalBinding = null;
    }

    private void setUpEntries() {
        // Mock data for journal entries
        JournalEntry entry1 = new JournalEntry("Morning Walk", "Tuesday, 2 April 2024",
                "Took a refreshing walk in the park. Enjoyed the sunrise and the chirping of birds.", R.mipmap.smilingface);
        JournalEntry entry2 = new JournalEntry("Work Meeting", "Wednesday, 3 April 2024",
                "Today's meeting with the client was incredibly productive. We gathered in the conference " +
                        "room, armed with charts, graphs, and presentations, ready to discuss the upcoming project. ", R.mipmap.smilingface);
        JournalEntry entry3 = new JournalEntry("Family Dinner", "Saturday, 20 April 2024",
                "Tonight, we gathered around the dinner table as a family, sharing stories, laughter, and love. " +
                        "The aroma of home-cooked food filled the air, mingling with the sound of animated conversation. We " +
                        "reminisced about cherished memories from the past and shared our hopes and dreams for the future. " +
                        "There was a sense of warmth and belonging that enveloped us, reaffirming the bond that ties us " +
                        "together.", R.mipmap.smilingface);

        journalEntries.add(entry1);
        journalEntries.add(entry2);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
        journalEntries.add(entry3);
    }

    private void openDateDialog() {
        // Retrieves and sets current date
        // TO-DO: Create date dialog to select year and date
        Calendar calender = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calender.getTime());
        int year = calender.get(Calendar.YEAR);
        TextView monthYear = journalBinding.monthYearTextView;
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d", month, year));
        monthYear.setOnClickListener(v -> {
            myp.showAsDropDown(monthYear);
        });

        // Add TextWatcher to the monthYearTextView
        monthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When the text (month and year) changes, fetch the corresponding journal entries
                ArrayList<JournalEntry> newEntries = getJournalEntries();
                // Update the RecyclerView with the new entries
                updateRecyclerView(newEntries);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private ArrayList<JournalEntry> getJournalEntries() {
        ArrayList<JournalEntry> filteredEntries = new ArrayList<>();
        String selectedMonthYear = journalBinding.monthYearTextView.getText().toString();
        String[] parts = selectedMonthYear.split(" ");
        String selectedMonth = parts[0];
//        String selectedYear = parts[1];

        // Filter journal entries based on selected month and year
        for (JournalEntry entry : journalEntries) {
            Calendar calendar = entry.parseDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            String entryMonth = dateFormat.format(calendar.getTime());
            if (selectedMonth.equals(entryMonth)) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }

    private void updateRecyclerView(ArrayList<JournalEntry> entries) {
        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), entries, this);
        journalBinding.entriesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        JournalEntry selectedEntry = journalEntries.get(position);
        JournalFragmentDirections.ActionJournalFragmentToEntryReaderFragment directions =
                JournalFragmentDirections.actionJournalFragmentToEntryReaderFragment(selectedEntry);

        Navigation.findNavController(getView()).navigate(directions);
    }
}