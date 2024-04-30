package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Intent intent;

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

        intent = getIntent();
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String content = intent.getStringExtra("content");
        int mood = intent.getIntExtra("mood", 0);

        titleTextView.setText(title);
        dateTextView.setText(date);
        entryTextView.setText(content);
        moodImageView.setImageResource(mood);

        Button backButton = findViewById(R.id.exitReadEntryButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous activity (journal page)
                finish();
            }
        });

    }
}