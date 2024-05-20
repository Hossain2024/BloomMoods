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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private JournalViewModel mJournalViewModel;
    private PlantViewModel mPlantViewModel;
    private UserViewModel mUserViewModel;

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
        findViewById(R.id.nav_host_fragment).post(() -> {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        });

        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        mPlantViewModel = new ViewModelProvider(this).get(PlantViewModel.class);
        mJournalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.SignIN_PREFS), Context.MODE_PRIVATE);
        Log.d("SharedPreferences", "SignIN_PREFS: " + getString(R.string.SignIN_PREFS)); // Log the value of SignIN_PREFS
        Log.d("SharedPreferences", "Context: " + this); // Log the context being used
        boolean isRemembered = sharedPreferences.getBoolean(getString(R.string.SignedIN), false);
        if (isRemembered) {
            initUserFromPrefs(sharedPreferences);
        }
    }

    protected void initUserFromPrefs(SharedPreferences sharedPreferences) {
        //navigateToHomeFragment();
        int userId = sharedPreferences.getInt("userId", 0); // Retrieve user ID from shared preferences
        if (userId != 0) {
            // Set the user ID in the user view model
            mUserViewModel.setUserId(userId);
            findViewById(R.id.nav_host_fragment).post(() -> {
                navController.navigate(R.id.nav_home);
                setupBottomNavigation();
                showBottomNavigation();
            });
        }
    }

    protected void setupBottomNavigation() {
//        // Initialize NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // Set up BottomNavigationView with NavController
        BottomNavigationView bottomNavView = findViewById(R.id.navBarView);
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }

    public UserViewModel getUserViewModel() {
        return mUserViewModel;
    }
    public PlantViewModel getPlantViewModel() {
        return mPlantViewModel;
    }
    public JournalViewModel getJournalViewModel() {
        return mJournalViewModel;
    }

    protected void showBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.VISIBLE);
    }

    protected void hideBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.GONE);
    }

    protected void bottomNavBarBackground() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        int color = ContextCompat.getColor(this, R.color.light_pink);
        navBarView.setBackgroundColor(color);
    }
    protected void bottomNavBarResetBg() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setBackground(null);
    }
}