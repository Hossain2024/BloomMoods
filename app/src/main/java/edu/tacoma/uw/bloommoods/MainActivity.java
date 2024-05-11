package edu.tacoma.uw.bloommoods;
import edu.tacoma.uw.bloommoods.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    UserViewModel mUserViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
         mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frame_main_fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.SignIN_PREFS), Context.MODE_PRIVATE);
        Log.d("SharedPreferences", "SignIN_PREFS: " + getString(R.string.SignIN_PREFS)); // Log the value of SignIN_PREFS
        Log.d("SharedPreferences", "Context: " + this); // Log the context being used
        boolean isRemembered = sharedPreferences.getBoolean(getString(R.string.SignedIN), false);
        if (isRemembered) {
            initUserFromPrefs(sharedPreferences);
        }

    }


    protected void initUserFromPrefs(SharedPreferences sharedPreferences){
            //navigateToHomeFragment();
            int userId = sharedPreferences.getInt("userId", 0); // Retrieve user ID from shared preferences
            if (userId != 0) {
                // Set the user ID in the user view model
                mUserViewModel.setUserId(userId);
                Fragment homeFragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, homeFragment)
                        .commit();
                showBottomNavigation();
                setupBottomNavigation();
            }



    }

    /**
     * Maliha- keep this here for to debug
     */

//    private void navigateToHomeFragment() {
//        // Navigate to the HomeFragment using Navigation component
//        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_loginFragment_to_homeFragment);
//    }
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
        Fragment fragment;
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_journal) {
            fragment = new JournalFragment();
        } else if (itemId == R.id.nav_water) {
            fragment = new WaterPlantFragment();
        } else if (itemId == R.id.nav_user) {
            fragment = new AboutFragment();
        } else {
            return false;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
        return true;
    }

    protected void showBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.VISIBLE);
    }

    protected void hideBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.GONE);
    }
//     public void goToWaterPlant(View view) {
//         Intent intent = new Intent(this, WaterPlantActivity.class);
//         Log.i("Water Plant", "Successfully going to Water Plant Page");
//         startActivity(intent);
//     }
//     public void goToHomePage(View view) {
//         Intent intent = new Intent(this, HomeFragment.class);
//         Log.i("Home Page", "Successfully going to Home Page");
//         startActivity(intent);
//     }
//     public void goToJournal(View view) {
//         Intent intent = new Intent(this, JournalFragment.class);
//         Log.i("Read Entry", "Successfully going to Journal Page");
//         startActivity(intent);
//     }
}