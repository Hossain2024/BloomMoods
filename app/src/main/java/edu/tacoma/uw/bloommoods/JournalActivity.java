package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity implements RecyclerViewInterface {
    ArrayList<JournalEntry> journalEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.entriesList), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        openDateDialog();

        RecyclerView recyclerView = findViewById(R.id.entriesRecyclerView);
        setUpEntries();

        JournalEntryAdapter adapter = new JournalEntryAdapter(this, journalEntries, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        TextView monthYear = findViewById(R.id.monthYearTextView);
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d", month, year));
    }

    @Override
    public void onItemClick(int position) {
        JournalEntry clickedEntry = journalEntries.get(position);
        Intent intent = new Intent(this, ReadEntryActivity.class);

        intent.putExtra("title", clickedEntry.getTitle());
        intent.putExtra("date", clickedEntry.getDate());
        intent.putExtra("content", clickedEntry.getContent());
        intent.putExtra("mood", clickedEntry.getMoodImage());

        startActivity(intent);
    }
}