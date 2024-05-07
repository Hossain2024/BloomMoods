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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

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
//        // Initialize NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Set up BottomNavigationView with NavController
        BottomNavigationView bottomNavView = findViewById(R.id.navBarView);
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }

//    private boolean switchToFragment(int itemId) {
//        Fragment fragment;
//        if (itemId == R.id.nav_home) {
//            fragment = new HomeFragment();
//        } else if (itemId == R.id.nav_journal) {
//            fragment = new JournalFragment();
//        } else if (itemId == R.id.nav_water) {
//            fragment = new WaterPlantFragment();
//        } else if (itemId == R.id.nav_user) {
//            fragment = new AboutFragment();
//        } else {
//            return false;
//        }
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.nav_host_fragment, fragment)
//                .commit();
//        return true;
//    }

    protected void showBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.VISIBLE);
    }

    protected void hideBottomNavigation() {
        BottomNavigationView navBarView = findViewById(R.id.navBarView);
        navBarView.setVisibility(View.GONE);
    }
}