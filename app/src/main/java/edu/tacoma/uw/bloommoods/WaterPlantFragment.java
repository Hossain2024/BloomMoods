package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {

    private static final String ADD_ENTRY_ENDPOINT = "https://students.washington.edu/nchi22/api/log/update_mood_log.php";
    private UserViewModel userViewModel;
    private int streak;
    private String lastEntry;
    FragmentWaterPlantBinding waterPlantBinding;
    String selectedMood;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        waterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        userViewModel = ((MainActivity) requireActivity()).getUserViewModel();

        return waterPlantBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                userViewModel.getCurrentPlantDetails(userId);
                userViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    observeResponsePlantDetails(response);
                });
                userViewModel.getUserProfile(userId);
                userViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
                    observeResponseUserProfile(response);
                });
            }
        });
        Button saveButton = waterPlantBinding.saveButton;
        saveButton.setOnClickListener(v -> addEntry());
        saveButton.setOnClickListener(v -> addGrowth());
        LinearLayout moodLayout = waterPlantBinding.linearLayout;

        setOnMoodClicks(moodLayout);
    }

    private void setTextImage(double currentGrowth, int plantStage, String plantName) {
        TextView date = waterPlantBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        TextView plantGrowth = waterPlantBinding.plantGrowth;

        // Update current growth according to user's current plant details
        String growth = "Current Growth: " + currentGrowth + "%";
        plantGrowth.setText(growth);

        ProgressBar progressBar = waterPlantBinding.progressBar;
        progressBar.setProgress((int) currentGrowth);

        ImageView plantPhoto = waterPlantBinding.plantStage;
        String resourceName = plantName.toLowerCase().replace(" ", "_") + "_stage_" + plantStage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            plantPhoto.setImageDrawable(drawable);
        } else {
            Log.e("HomeFragment", "Drawable resource not found: " + resourceName);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        waterPlantBinding = null;
    }

    private void setOnMoodClicks(LinearLayout moodLayout) {
        for (int i = 0; i < moodLayout.getChildCount(); i++) {
            View childView = moodLayout.getChildAt(i);
            if (childView instanceof ImageView) {
                childView.setOnClickListener(this::onMoodClicked);
            }
        }
    }


    private void addEntry() {
        EditText titleEditText = waterPlantBinding.titleEditText;
        EditText entryEditText = waterPlantBinding.entryEditText;

        String title = titleEditText.getText().toString();
        String entry = entryEditText.getText().toString();

        // Get current user ID
        userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                Log.i("Water, User id", String.valueOf(userId));
                try {
                    // Create JSON object with the entry data
                    JSONObject json = new JSONObject();
                    json.put("user_id", userId);
                    json.put("title", title);
                    json.put("mood", "Testing");
                    json.put("journal_entry", entry);

                    JsonObjectRequest request = getRequest(json, ADD_ENTRY_ENDPOINT, response -> {
                        Toast.makeText(getContext(), "Entry saved successfully", Toast
                                .LENGTH_SHORT).show();
                    });
                    RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
                    requestQueue.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addGrowth() {
        boolean loggedToday = false;
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

            if (hours <= 24) {
                loggedToday = true;
            }
        } catch (ParseException e) {
            System.out.println("Error parsing the date: " + e.getMessage());
        }

        double increaseGrowth;
        if (streak >= 2) {
            increaseGrowth = 5;
        } else {
            increaseGrowth = 2.5;
        }
        if (!loggedToday) {
            // Get current user ID
            userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
                if (userId != null) {
                    userViewModel.updateCurrentPlantDetails(userId, increaseGrowth);
                }
            });
        }
    }


    @NonNull
    private JsonObjectRequest getRequest(JSONObject json, String API, Consumer<JSONObject> response) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API,
                json,
                response::accept,
                Throwable::printStackTrace);

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }



    public void onMoodClicked(View view) {
        selectedMood = view.getTag().toString();
        Log.i("Selected Mood", selectedMood);
    }

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
                    if (response.has("growthLevel") && response.has("name") && response.has("stage")) {
                        double plantGrowthPercent = response.getDouble("growthLevel");
                        Log.i("Plant growth", String.valueOf(plantGrowthPercent));
                        String activePlantName = response.getString(("name"));
                        int activePlantStage = response.getInt("stage");
                        Log.i("Plant stage", String.valueOf(activePlantStage));
                        setTextImage(plantGrowthPercent, activePlantStage, activePlantName);
                    }
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("Plant details response", "Could not obtain plant details");
        }
        Log.i("WaterPlantFragment", "FINISHED OBSERVE RESPONSE");
    }

    private void observeResponseUserProfile(final JSONObject response) {
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
                    if (response.has("streak") && response.has("last_log_date")) {
                        streak = response.getInt("streak");
                        lastEntry = response.getString("last_log_date");
                    }
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("User details", "Could not obtain user details");
        }
        Log.i("WaterPlantFragment", "FINISHED OBSERVE RESPONSE");
    }
}