package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private TextView editText;  // Declare editText here
    private TextView entriesText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frame_main_fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        setEditText();
    }
//    private void setEditText() {
//        int days = 15;
//        // Initialize EditText after setContentView
//        editText = findViewById(R.id.textStreak);
//
//        String text = "Streak\n " + days + "  days";
//        SpannableString spannableString = new SpannableString(text);
//
//
//        // Apply a size span to "Big Text"
//        RelativeSizeSpan daysSpan = new RelativeSizeSpan(2.8f); // 150% larger size
//        spannableString.setSpan(daysSpan, text.indexOf(String.valueOf(days)), text.indexOf(String.valueOf(days)) + String.valueOf(days).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // Apply a size span to "Small Text"
//        RelativeSizeSpan smallTextSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
//        spannableString.setSpan(smallTextSpan, text.indexOf("days"), text.indexOf("days") + "days".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        editText.setText(spannableString);
//
//        int entries = 40;
//        entriesText = findViewById(R.id.textEntries);
//        String totalentries = "Total Entries\n " + entries + "  entries";
//        SpannableString spannableStringEntries = new SpannableString(totalentries);
//
//        // Apply a size span to "Big Text"
//        RelativeSizeSpan entriesSpan = new RelativeSizeSpan(2.8f); // 150% larger size
//        spannableStringEntries.setSpan(entriesSpan, totalentries.indexOf(String.valueOf(entries)), totalentries.indexOf(String.valueOf(entries)) + String.valueOf(entries).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // Apply a size span to "Small Text"
//        RelativeSizeSpan entSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
//        spannableStringEntries.setSpan(entSpan, totalentries.indexOf("entries"), totalentries.indexOf("entries") + "entries".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
//        entriesText.setText(spannableStringEntries);
//    }

    public void goToWaterPlant(View view) {
        Intent intent = new Intent(this, WaterPlantActivity.class);
        Log.i("Water Plant", "Successfully going to Water Plant Page");
        startActivity(intent);
    }

    public void goToJournal(View view) {
        Intent intent = new Intent(this, JournalFragment.class);
        Log.i("Read Entry", "Successfully going to Journal Page");
        startActivity(intent);
    }
}