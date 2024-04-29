package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.entriesList), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void goToWaterPlant(View view) {
        Intent intent = new Intent(this, WaterPlantActivity.class);
        Log.i("Water Plant", "Successfully going to Water Plant Page");
        startActivity(intent);
    }

    public void goToJournal(View view) {
        Intent intent = new Intent(this, JournalActivity.class);
        Log.i("Read Entry", "Successfully going to Journal Page");
        startActivity(intent);
    }
}