package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private String userName;
    private int userStreak;
    private int userEntries;
    private UserViewModel mUserViewModel;
    private FragmentHomeBinding homeBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return homeBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                // Call the method in ViewModel to perform the API operation
                mUserViewModel.getUserProfile(userId);
                mUserViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    observeResponseUserId(response);
                });
                Log.i("Home", String.valueOf(userId));
                mUserViewModel.getCurrentPlantDetails(userId);
                mUserViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    observeResponsePlantDetails(response);
                });

            }
        });
    }


    @Override
    public void onDestroyView() {
        Log.d("FragmentManager", "HomeFragment destroyed.");
        super.onDestroyView();
        homeBinding = null;
    }

    private void observeResponseUserId(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("error")) {
                    String result = response.getString("error");
                    if ("Invalid User ID.".equals(result)) {
                        // If the result is "failed to login", display the error message to the user
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display profile", errorMessage);
                    }
                } else {
                    if (response.has("name") && response.has("streak") && response.has("total_entries") && response.has("last_log_date")) {
                        userName = response.getString("name");
                        Log.i("Username", userName);
                        userStreak = response.getInt("streak");
                        Log.i("User streak", String.valueOf(userStreak));
                        userEntries = response.getInt("total_entries");
                        String lastEntry = response.getString("last_log_date");
                        Log.i("Last logged entry", lastEntry);
                        // Create a SimpleDateFormat object for parsing the date in the given format
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        try {
                            // Parse the date string into a Date object
                            Date lastLoggedDate = dateFormat.parse(lastEntry);

                            // Get the current date and time
                            Date currentDate = new Date();

                            // Check if lastLoggedDate is within the last 24 hours
                            long diff = currentDate.getTime() - lastLoggedDate.getTime();
                            long hours = diff / (60 * 60 * 1000);

                            if (!(hours <= 24)) {
                                resetStreak();
                            }
                        } catch (ParseException e) {
                            System.out.println("Error parsing the date: " + e.getMessage());
                        }
                        setEditText();
                    }
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("User profile response", "Could not obtain profile data");
        }
        Log.i("HomeFragment", "FINISHED OBSERVE RESPONSE");
    }

    /*
    Observe response from method getCurrentPlantDetails. Method will take the plant growth,
    stage, and name. Then call local method setPlantDetails to update the progress bar in Home page
    and the image view.
     */
    private void observeResponsePlantDetails(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("error")) {
                    String result = response.getString("error");
                    if ("The user ID does not exist or is not active.".equals(result)) {
                        // If the result is "failed to login", display the error message to the user
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display plant details", errorMessage);
                    }
                } else {
                    if (response.has("stage") && response.has("growthLevel") && response.has("name")) {
                        int plantGrowth = response.getInt("growthLevel");
                        Log.i("Plant growth", String.valueOf(plantGrowth));
                        String activePlantName = response.getString(("name"));
                        int stage = response.getInt("stage");
                        Log.i("Plant stage", String.valueOf(stage));
                        setPlantDetails(activePlantName, plantGrowth, stage);
                    }
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("Plant details response", "Could not obtain plant details");
        }
        Log.i("HomeFragment", "FINISHED OBSERVE RESPONSE");
    }

    /*
    Updates progress bar based on plant growth.
    Updates image of plant stage based on the current stage.
     */
    private void setPlantDetails(String activePlantName, int plantGrowth, int stage) {
        ImageView plantStage = homeBinding.plantStageView;
        TextView progressBar = homeBinding.progressLabel;
        String growth= "Plant Growth: " + plantGrowth + "%";
        progressBar.setText(growth);

        String resourceName = activePlantName.toLowerCase().replace(" ", "_") + "_stage_" + stage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            plantStage.setImageDrawable(drawable);
        } else {
            Log.e("HomeFragment", "Drawable resource not found: " + resourceName);
        }
    }



    private void setEditText() {
        TextView usernameText = homeBinding.textUserName;
        String name = "Hey, " + userName + "!";

        usernameText.setText(name);

        // Initialize EditText after setContentView
        TextView editText = homeBinding.textStreak;

        String text = "Streak\n " + userStreak + "  days";
        SpannableString spannableString = new SpannableString(text);


        // Apply a size span to "Big Text"
        RelativeSizeSpan daysSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableString.setSpan(daysSpan, text.indexOf(String.valueOf(userStreak)), text.indexOf(String.valueOf(userStreak)) + String.valueOf(userStreak).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan smallTextSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableString.setSpan(smallTextSpan, text.indexOf("days"), text.indexOf("days") + "days".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        editText.setText(spannableString);

        TextView entriesText = homeBinding.textEntries;
        String totalentries = "Total Entries\n " + userEntries + "  entries";
        SpannableString spannableStringEntries = new SpannableString(totalentries);

        // Apply a size span to "Big Text"
        RelativeSizeSpan entriesSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableStringEntries.setSpan(entriesSpan, totalentries.indexOf(String.valueOf(userEntries)), totalentries.indexOf(String.valueOf(userEntries)) + String.valueOf(userEntries).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan entSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableStringEntries.setSpan(entSpan, totalentries.indexOf("entries"), totalentries.indexOf("entries") + "entries".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        entriesText.setText(spannableStringEntries);
    }

    private void resetStreak() {
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                // Call the method in ViewModel to perform the API operation
                mUserViewModel.resetStreak(userId);
            }
        });
    }
}
