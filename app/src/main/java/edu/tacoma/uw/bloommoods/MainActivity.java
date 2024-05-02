package edu.tacoma.uw.bloommoods;
import edu.tacoma.uw.bloommoods.R;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
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
    }
    protected void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.navBarView);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return switchToFragment(item.getItemId());
            }
        });
    }

    private boolean switchToFragment(int itemId) {
        // Attempt to retrieve the fragment from the fragment manager if it already exists
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getFragmentTag(itemId));
        if (fragment == null) {
            // Only create a new fragment if one does not already exist
            fragment = createFragmentByItemId(itemId);
            if (fragment == null) return false; // If no corresponding fragment found, return false

            // Replace the existing fragment with the new one, if needed
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment, getFragmentTag(itemId))
                    .commit();
        }
        return true;
    }

    private Fragment createFragmentByItemId(int itemId) {
        // Create a new fragment instance based on the item ID
        if (itemId == R.id.nav_home) {
            Log.d("FragmentManager", "Creating new HomeFragment instance");
            return new HomeFragment();
        } else if (itemId == R.id.nav_journal) {
            return new JournalFragment();
        }
        return null;
    }

    private String getFragmentTag(int itemId) {
        if (itemId == R.id.nav_home) {
            return "HOME_FRAGMENT";
        } else if (itemId == R.id.nav_journal) {
            return "JOURNAL_FRAGMENT";
        }
        return null;
    }



    protected void showBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.VISIBLE);
    }

    protected void hideBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.GONE);
    }

    public void goToWaterPlant(View view) {
        Intent intent = new Intent(this, WaterPlantActivity.class);
        Log.i("Water Plant", "Successfully going to Water Plant Page");
        startActivity(intent);
    }
    public void goToHomePage(View view) {
        Intent intent = new Intent(this, HomeFragment.class);
        Log.i("Home Page", "Successfully going to Home Page");
        startActivity(intent);
    }


    public void goToJournal(View view) {
        Intent intent = new Intent(this, JournalFragment.class);
        Log.i("Read Entry", "Successfully going to Journal Page");
        startActivity(intent);
    }
}