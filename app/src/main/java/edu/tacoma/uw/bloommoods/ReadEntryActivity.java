package edu.tacoma.uw.bloommoods;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReadEntryActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView entryTextView;
    private ImageView moodImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.entriesList), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        entryTextView = findViewById(R.id.entryTextView);
        moodImageView = findViewById(R.id.moodImageView);

        // Fetch journal entry data from database (simulate for now)
        // Replace with actual database interaction later
        JournalEntry entry = fetchJournalEntryFromDatabase();

        // Populate UI with fetched data
        if (entry != null) {
            titleTextView.setText(entry.getTitle());
            dateTextView.setText(entry.getDate());
            entryTextView.setText(entry.getContent());
            moodImageView.setImageResource(entry.getMoodImage());
        }
    }

    // Fetch journal entry from database (placeholder for now)
    private JournalEntry fetchJournalEntryFromDatabase() {
        // Simulate fetching data from database (replace with actual implementation later)
        // For now, return a mock journal entry
        return new JournalEntry(
                "Hike in the Mountains",
                "Thursday, 25 March 2024",
                "The city of southern California, san diego is locally known as America's Finest City. " +
                        "It's located on San Diego Bay, an inlet of the Pacific Ocean near the Mexican border. San Diego " +
                        "is the second largest city in California and the seventh largest in the United States. " +
                        "It is also nicknames as America's biggest small town.",
                R.mipmap.smilingface // Replace with appropriate mood image resource id
        );
    }
}