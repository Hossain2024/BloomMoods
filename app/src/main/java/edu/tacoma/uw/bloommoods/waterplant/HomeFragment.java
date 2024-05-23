package edu.tacoma.uw.bloommoods.waterplant;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private String userName;
    private int userStreak;
    private int userEntries;
    private double plantGrowth;
    private int stage;
    private double days;
    private ImageView plantStage;
    private UserViewModel userViewModel;
    private PlantViewModel plantViewModel;
    private FragmentHomeBinding homeBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        userViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        plantViewModel = ((MainActivity) requireActivity()).getPlantViewModel();
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return homeBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FragmentManager", "HomeFragment created.");
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setHomeBg(true);
        }
        plantStage = homeBinding.plantStageView;
        userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                // Call the method in ViewModel to perform the API operation
                userViewModel.getUserProfile(userId);
                userViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    if (response.length() > 0) {
                        observeResponseUserId(response, userId);
                    }
                });
                plantViewModel.getCurrentPlantDetails(userId);
                plantViewModel.addPlantResponseObserver(getViewLifecycleOwner(), response -> {
                    if (response.length() > 0) {
                        observeResponsePlantDetails(response, userId);
                    }
                });
            }
        });
        ImageButton tooltipButton = homeBinding.tooltipButton;
        tooltipButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_aboutFragment));
    }


    @Override
    public void onDestroyView() {
        Log.d("FragmentManager", "HomeFragment destroyed.");
        super.onDestroyView();
        homeBinding = null;
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setHomeBg(false);
        }
    }

    /**
     * Observes the response for user profile details.
     *
     * @param response The JSON response from the server.
     * @param userId The user ID.
     */
    private void observeResponseUserId(final JSONObject response, int userId) {
        Log.d("RESPONSE LENGTH", String.valueOf(response.length()));
        if (response.length() > 0) {
            try {
                if (response.has("error")) {
                    String result = response.getString("error");
                    if ("User ID does not exist.".equals(result)) {
                        // If the result is "failed to login", display the error message to the user
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display profile", errorMessage);
                    }
                } else {
                        userName = response.getString("name");
                        userStreak = response.getInt("streak");
                        userEntries = response.getInt("total_entries");
                        String lastEntry = response.getString("last_log_date");
                        userViewModel.setLastEntryLogged(lastEntry);
                        calculateHours();
                        if (days > 1.0) {
                            Log.i("Difference in Days: ", String.valueOf(days));
                            resetStreak(userId);
                        }
                        setEditText();
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("HOME FRAGMENT", "Could not obtain profile data");
        }
    }

    /**
     * Observes the response for plant details.
     *
     * @param response The JSON response from the server.
     * @param userId The user ID.
     */
    private void observeResponsePlantDetails(final JSONObject response, int userId) {
            try {
                if (response.has("error")) {
                    String result = response.getString("error");
                    if ("The user ID does not exist or is not active.".equals(result)) {
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display plant details", errorMessage);
                    }
                } else {
                    if (response.has("stage") && response.has("growthLevel") && response.has("name")) {
                        plantGrowth = response.getDouble("growthLevel");
                        String activePlantName = response.getString(("name"));
                        stage = response.getInt("stage");
                        if (days > 7.0) {
                            resetStage(userId);
                        }
                        setPlantDetails(activePlantName);
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
    }

    /**
     * Sets the plant details including the plant image and growth level.
     *
     * @param activePlantName The name of the active plant.
     */
    private void setPlantDetails(String activePlantName) {
        String resourceName = activePlantName.toLowerCase().replace(" ", "_") + "_stage_" + stage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            plantStage.setImageDrawable(drawable);
        } else {
            Log.e("HomeFragment", "Drawable resource not found: " + resourceName);
        }

        ProgressBar progressBar = homeBinding.progressBar;
        TextView progressLabel = homeBinding.progressLabel;
        String growth= "Plant Growth: " + plantGrowth + "%";
        progressLabel.setText(growth);
        progressBar.setProgress((int) plantGrowth);

        }

    /**
     * Sets the text fields for the user name, streak, and total entries.
     */
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

    /**
     * Resets the user's streak.
     *
     * @param userId The user ID for which to reset the streak.
     */
    private void resetStreak(int userId) {
        userViewModel.resetStreak(userId);
    }

    /**
     * Resets the plant's stage.
     *
     * @param userId The user ID for which to reset the plant stage.
     */
    private void resetStage(int userId) {
        plantViewModel.resetCurrentPlantStage(userId);
        setSaturation(plantStage);
        userViewModel.setReset(true);

    }

    /**
     * Calculates the difference in hours since the last entry logged.
     */
    private void calculateHours() {
        userViewModel.getLastEntryLogged().observe(getViewLifecycleOwner(), lastEntry -> {
            if (!Objects.equals(lastEntry, "null")) {
                // Create a SimpleDateFormat object for parsing the date in the given format
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                try {
                    // Parse the date string into a Date object
                    Date lastLoggedDate = dateFormat.parse(lastEntry);

                    // Get the current date and time
                    Date currentDate = new Date();

                    // Get difference between current date and last logged date in hours.
                    long differenceInMillis = currentDate.getTime() - lastLoggedDate.getTime();
                    days = (double) differenceInMillis / (1000 * 60 * 60 * 24);

                } catch (ParseException e) {
                    System.out.println("Error parsing the date: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Sets the saturation of the plant image view to grayscale.
     *
     * @param imageView The ImageView to be desaturated.
     */
    private void setSaturation(ImageView imageView) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation((float) 0.0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        imageView.setColorFilter(filter);
    }
}
