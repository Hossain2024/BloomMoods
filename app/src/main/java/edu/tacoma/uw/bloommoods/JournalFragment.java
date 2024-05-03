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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentJournalBinding;

public class JournalFragment extends Fragment implements RecyclerViewInterface {
    private final ArrayList<JournalEntry> journalEntries = new ArrayList<>();
    private ArrayList<JournalEntry> monthJournalEntries = new ArrayList<>();
    private MonthYearPicker myp;
    private FragmentJournalBinding journalBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        journalBinding = FragmentJournalBinding.inflate(inflater, container, false);
        myp = new MonthYearPicker(getActivity(), journalBinding.monthYearTextView);
        openDateDialog();

        RecyclerView recyclerView = journalBinding.entriesRecyclerView;
        setUpEntries();
        monthJournalEntries = getJournalEntries();

        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), monthJournalEntries, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        journalBinding.allEntriesButton.setOnClickListener(button -> updateRecyclerView(journalEntries));

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
        JournalEntry entry2 = new JournalEntry("Whirlwind Day", "Wednesday, 3 May 2024",
                "Today was one of those days that felt like a whirlwind. From the moment I woke up, it seemed like I was running to catch up with time itself. " +
                        "Work was a flurry of emails, meetings, and deadlines. Amidst the chaos, I found moments of clarity, brief respites where " +
                        "I could collect my thoughts and focus. But overall, it was a day that demanded resilience and adaptability. As the evening " +
                        "settles in, I’m grateful for the chance to unwind, reflect, and recharge for whatever tomorrow brings.", R.mipmap.smilingface);
        JournalEntry entry3 = new JournalEntry("Family Dinner", "Saturday, 2 May 2024",
                "Tonight, we gathered around the dinner table as a family, sharing stories, laughter, and love. " +
                        "The aroma of home-cooked food filled the air, mingling with the sound of animated conversation. We " +
                        "reminisced about cherished memories from the past and shared our hopes and dreams for the future. " +
                        "There was a sense of warmth and belonging that enveloped us, reaffirming the bond that ties us " +
                        "together.", R.mipmap.smilingface);
        JournalEntry entry4 = new JournalEntry("Work Meeting", "Wednesday, 3 April 2024",
                "Today's meeting with the client was incredibly productive. We gathered in the conference " +
                        "room, armed with charts, graphs, and presentations, ready to discuss the upcoming project. ", R.mipmap.smilingface);
        journalEntries.add(entry1);
        journalEntries.add(entry2);
        journalEntries.add(entry3);
        journalEntries.add(entry4);

        journalEntries.sort(new Comparator<JournalEntry>() { // Sort entries by most to least recent
            @Override
            public int compare(JournalEntry o1, JournalEntry o2) {
                return (o2.parseDate()).compareTo(o1.parseDate());
            }
        });
    }

    private void openDateDialog() {
        // Retrieves and sets current date
        // TO-DO: Create date dialog to select year
        Calendar calender = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calender.getTime());
        int year = calender.get(Calendar.YEAR);
        TextView monthYear = journalBinding.monthYearTextView;
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d    ⓥ", month, year));
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
                monthJournalEntries = getJournalEntries();
                // Update the RecyclerView with the new entries
                updateRecyclerView(monthJournalEntries);
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