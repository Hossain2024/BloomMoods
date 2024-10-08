package edu.tacoma.uw.bloommoods;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.ActivityMainBinding;
import edu.tacoma.uw.bloommoods.journal.JournalViewModel;
import edu.tacoma.uw.bloommoods.waterplant.PlantViewModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.util.Log;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Serves as the main entry point for the app.
 *
 * @author Amanda Nguyen
 * @author Maliha Hossain
 */
public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private JournalViewModel mJournalViewModel;
    private PlantViewModel mPlantViewModel;
    private UserViewModel mUserViewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frame_main_fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize nav controller after view is created
        findViewById(R.id.nav_host_fragment).post(() -> navController = Navigation.findNavController(this, R.id.nav_host_fragment));

        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        mPlantViewModel = new ViewModelProvider(this).get(PlantViewModel.class);
        mJournalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);

        // Load shared preferences and check is user is remembered
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.SignIN_PREFS), Context.MODE_PRIVATE);
        Log.d("SharedPreferences", "SignIN_PREFS: " + getString(R.string.SignIN_PREFS)); // Log the value of SignIN_PREFS
        Log.d("SharedPreferences", "Context: " + this); // Log the context being used
        boolean isRemembered = sharedPreferences.getBoolean(getString(R.string.SignedIN), false);
        if (isRemembered) {
            initUserFromPrefs(sharedPreferences);
        }
    }

    /**
     * Initializes the user from shared preferences and navigates to home fragment if user is found.
     *
     * @param sharedPreferences The SharedPreferences from which to retrieve the user ID.
     */
    protected void initUserFromPrefs(SharedPreferences sharedPreferences) {
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

    /**
     * Sets up bottom navigation bar.
     */
    public void setupBottomNavigation() {
//        // Initialize NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // Set up BottomNavigationView with NavController
        BottomNavigationView bottomNavView = findViewById(R.id.navBarView);
        NavigationUI.setupWithNavController(bottomNavView, navController);
        bottomNavView.setOnItemSelectedListener(item -> {
            int currentDestinationId = navController.getCurrentDestination().getId();
            int itemId = item.getItemId();

            if (currentDestinationId == itemId) {
                // The user clicked the currently displayed fragment; do nothing
                return true;
            } else {
                // Navigate to the selected fragment
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });
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

    /**
     * Shows the bottom navigation bar.
     */
    public void showBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the bottomo navigation bar.
     */
    public void hideBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.GONE);
    }

    /**
     * Sets the background color of the bottom navigation bar to light pink.
     */
    public void bottomNavBarBackground() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        int color = ContextCompat.getColor(this, R.color.light_pink);
        navBarView.setBackgroundColor(color);
    }

    /**
     * Resets the background of the bottom navigation bar to its default state.
     */
    public void bottomNavBarResetBg() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setBackground(null);
    }

    /**
     * Sets home background image based on a condition.
     *
     * @param check A boolean for whether to set the home background or use the default background
     */
    public void setHomeBg(boolean check) {
        String resourceName = "background";
        if (check) {
            resourceName = "home_background";
        }
        int resourceId = getResources().getIdentifier(resourceName, "drawable", this.getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(this, resourceId);
            binding.frameMainFragmentContainer.setBackground(drawable);
        }
    }
}