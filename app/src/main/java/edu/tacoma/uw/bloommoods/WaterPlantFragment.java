package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {
    private JournalViewModel mJournalViewModel;

    private static final String ADD_ENTRY_ENDPOINT = "https://students.washington.edu/nchi22/api/log/update_mood_log.php";
    private UserViewModel userViewModel;
    private PlantViewModel plantViewModel;
    private int streak;
    private ImageView plantPhoto;
    private boolean loggedToday;
    private String activePlantName;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private double plantGrowthPercent;
    private int numberOfUnlocked;
    private int activePlantId;
    private String currentPlantSwitch;
    private int activePlantStage;
    private ProgressBar progressBar;
    private TextView plantGrowth;
    private TextView selectPlantText;
    private ImageButton selectPlantButton;
    private ImageView switchedPlant;
    private EditText titleEditText;
    private EditText entryEditText;
    private Button saveButton;
    private LinearLayout moodLayout;
    private int userId;
    private int tulipStage;
    private int sunflowerStage;
    private int peonyStage;
    FragmentWaterPlantBinding waterPlantBinding;
    String selectedMood;
  
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("WaterPlantFragment", "CREATED VIEW");
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        waterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        userViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        plantViewModel = ((MainActivity) requireActivity()).getPlantViewModel();

        return waterPlantBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                this.userId = userId;
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry using the userId
                waterPlantBinding.saveButton.setOnClickListener(v -> addEntry(userId));
                plantViewModel.getCurrentPlantDetails(userId);
                plantViewModel.addPlantResponseObserver(getViewLifecycleOwner() , response -> {
                    if (response.length() > 0) {
                        observeResponsePlantDetails(response);
                    }
                });
                plantViewModel.addPlantDetailResponseObserver(getViewLifecycleOwner() , response -> {
                    if (response.length() > 0) {
                        plantViewModel.getCurrentPlantDetails(userId);
                    }
                });
                userViewModel.getUserProfile(userId);
                userViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponseUserProfile);
                isUnlockedPlant();
            }
        });
        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
        listeners();
    }

    private void listeners() {
        moodLayout = waterPlantBinding.linearLayout;
        titleEditText = waterPlantBinding.titleEditText;
        entryEditText = waterPlantBinding.entryEditText;
        switchedPlant = waterPlantBinding.plantStageSwitch;
        selectPlantText = waterPlantBinding.selectPlantText;
        selectPlantButton = waterPlantBinding.selectPlantButton;
        plantPhoto = waterPlantBinding.plantStage;

        ImageView switchButton = waterPlantBinding.switchButton;
        switchButton.setOnClickListener(v -> toggleSwitchPlant());


        leftArrow = waterPlantBinding.leftArrow;
        rightArrow = waterPlantBinding.rightArrow;

        rightArrow.setOnClickListener(v -> switchArrows("right"));
        leftArrow.setOnClickListener(v -> switchArrows("left"));
        LinearLayout moodLayout = waterPlantBinding.linearLayout;

        setOnMoodClicks(moodLayout);
        adjustToKeyboard();
    }


    private void setTextImage() {
        TextView date = waterPlantBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        plantGrowth = waterPlantBinding.plantGrowth;

        // Update current growth according to user's current plant details
        String growth = "Current Growth: " + plantGrowthPercent + "%";
        plantGrowth.setText(growth);

        progressBar = waterPlantBinding.progressBar;
        progressBar.setProgress((int) plantGrowthPercent);

    }

    private void setPlantImage(ImageView view, int plantStage, String plantName) {
        String resourceName = plantName.toLowerCase().replace(" ", "_") + "_stage_" + plantStage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            userViewModel.getReset().observe(getViewLifecycleOwner(), reset -> {
                if (reset) {
                    setSaturation(view, 0f);
                }
            });
            view.setImageDrawable(drawable);
        } else {
            Log.e("WaterPlantFragment", "Drawable resource not found: " + resourceName);
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


    private void addEntry(int userId) {
        String title = waterPlantBinding.titleEditText.getText().toString();
        String entry = waterPlantBinding.entryEditText.getText().toString();
        mJournalViewModel.addEntry(userId, title, selectedMood, entry);
        addGrowth();
    }

    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("error")) {
                try {
                    Toast.makeText(this.getContext(),
                            "Error Adding entry: " +
                                    response.get("error"), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }

            } else {
                Toast.makeText(this.getContext(),"Entry added", Toast.LENGTH_LONG).show();
                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
                    Log.i("NewEntryFragment", String.valueOf(moodEntry));
                });
            }

        } else {
            Log.d("JSON Response", "No Response");
        }
    }


    private void addGrowth() {
        loggedToday = false;
        userViewModel.getLastEntryLogged().observe(getViewLifecycleOwner(), entry -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (!entry.equals("null")) {
                try {
                    // Parse the date string into a Date object
                    Date lastLoggedDate = dateFormat.parse(entry);

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
            }
            });

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
                    plantViewModel.updateCurrentPlantDetails(userId, increaseGrowth);
                    if (plantGrowthPercent + increaseGrowth >= 100) {
                        unlockNewPlant(activePlantId);
                    }
                }
            });
            userViewModel.getReset().observe(getViewLifecycleOwner(), reset -> {
                if (reset) {
                    setSaturation(plantPhoto, 1.0f);
                    userViewModel.setReset(false);
                }
            });
        }
    }


    @NonNull
    private JsonObjectRequest getRequest(JSONObject json) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ADD_ENTRY_ENDPOINT,
                json,
                response ->
                        Toast.makeText(getContext(), "Entry saved successfully", Toast.LENGTH_SHORT).show(),
                Throwable::printStackTrace);
        Log.i("UserViewModel", request.getUrl().toString());
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
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display plant details", errorMessage);
                    }
                } else {
                        plantGrowthPercent = response.getDouble("growthLevel");
                        activePlantName = response.getString(("name"));
                        currentPlantSwitch = activePlantName;
                        activePlantId = response.getInt(("plant_option_id"));
                        activePlantStage = response.getInt("stage");
                        setTextImage();
                        setPlantImage(plantPhoto, activePlantStage, activePlantName);
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("Plant details response", "Could not obtain plant details OBSERVE");
        }
    }
    private void observeUnlockedPlants(final JSONArray response) {
        if (response.length() > 0) {
            numberOfUnlocked = response.length();
            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonObject = response.getJSONObject(i);
                    if (i == 0) {
                        tulipStage = jsonObject.getInt("stage");
                    } else if (i == 1) {
                        sunflowerStage = jsonObject.getInt("stage");
                    } else {
                        peonyStage = jsonObject.getInt("stage");
                    }
                }
            } catch (JSONException e) {
                Log.e("Unlocked Plants response", "Could not obtain UPDATED plant details");
            }
        }else{
            Log.e("Plant details response", "Could not obtain UPDATED plant details");
        }
    }

    private void observeResponseUserProfile(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("error")) {
                    String result = response.getString("error");
                    if ("Invalid User ID.".equals(result)) {
                        String errorMessage = response.optString("message", "Unknown error");
                        Log.i("Error to display profile", errorMessage);
                    }
                } else {
                    if (response.has("streak") && response.has("last_log_date")) {
                        streak = response.getInt("streak");
                    }
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("User details", "Could not obtain user details");
        }
    }

    private void setSaturation(ImageView imageView, float saturation) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(saturation);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        imageView.setColorFilter(filter);
    }


    private void toggleSwitchPlant() {
        if (rightArrow.getVisibility() == View.VISIBLE || leftArrow.getVisibility() == View.VISIBLE) {
            toggleSwitchPlantOff();
        } else {
            updateArrows(activePlantName);
            entryEditText.setVisibility(View.GONE);
            titleEditText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            moodLayout.setVisibility(View.GONE);
        }
    }
    private void toggleSwitchPlantOff() {
        rightArrow.setVisibility(View.GONE);
        leftArrow.setVisibility(View.GONE);
        toggleSelectPlant(false);
        moodLayout.setVisibility(View.VISIBLE);
        entryEditText.setVisibility(View.VISIBLE);
        titleEditText.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        currentPlantSwitch = activePlantName;

    }

    private void updateArrows(String plantName) {
        if (plantName.equals("Tranquil Tulip")) {
            rightArrow.setVisibility(View.VISIBLE);
            leftArrow.setVisibility(View.GONE);
        } else if (plantName.equals("Serenity Sunflower")) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.VISIBLE);
        } else if (plantName.equals("Peaceful Peony")) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.GONE);
        }
    }

    private void selectPlant(String plantName, int plantOptionId) {
        toggleSelectPlant(true);

        String resourceName = "select_plant_locked";
        String selected = "Not yet unlocked";
        selectPlantText.setVisibility(View.VISIBLE);
        int color = ContextCompat.getColor(getContext(), R.color.black);

        updateArrows(plantName);

        switchedPlant.setVisibility(View.VISIBLE);


        if (numberOfUnlocked < plantOptionId && plantOptionId != activePlantId) {
            setPlantImage(switchedPlant, 1, plantName);
            setSaturation(switchedPlant, 0f);
        } else if (!(numberOfUnlocked < plantOptionId) && plantOptionId != activePlantId) {
            selected = "Select Plant";
            resourceName = "select_plant_unlocked";
            color = ContextCompat.getColor(getContext(), R.color.dark_green);
            selectPlantButton.setOnClickListener(v -> updatePlant(userId, plantOptionId));
            if (plantOptionId == 1) {
                setPlantImage(switchedPlant, tulipStage, plantName);
            } else if (plantOptionId == 2) {
                setPlantImage(switchedPlant, sunflowerStage, plantName);
            } else {
                setPlantImage(switchedPlant, peonyStage, plantName);
            }
            setSaturation(switchedPlant, 1.0f);
        }
        else {
            toggleSelectPlant(false);
        }
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            selectPlantButton.setImageDrawable(drawable);
        }
        selectPlantText.setTextColor(color);
        selectPlantText.setText(selected);

    }

    private void switchArrows(String direction) {
        plantPhoto.setVisibility(View.INVISIBLE);
        Log.i("CURRENT PLANT SWITCH", currentPlantSwitch);
        Log.i("DIRECTIONS", direction);

        if (direction.equals("right") && currentPlantSwitch.equals("Tranquil Tulip")) {
            selectPlant("Serenity Sunflower", 2);
            currentPlantSwitch = "Serenity Sunflower";
        }
        else if (direction.equals("right") && currentPlantSwitch.equals("Serenity Sunflower")) {
            selectPlant("Peaceful Peony", 3);
            currentPlantSwitch = "Peaceful Peony";
        }
        else if (direction.equals("left") && currentPlantSwitch.equals("Serenity Sunflower")) {
            selectPlant("Tranquil Tulip", 1);
            currentPlantSwitch = "Tranquil Tulip";
        }
        else if (direction.equals("left") && currentPlantSwitch.equals("Peaceful Peony")) {
            selectPlant("Serenity Sunflower", 2);
            currentPlantSwitch = "Serenity Sunflower";
        }

    }

    private void unlockNewPlant(int currentPlantId) {
        if (currentPlantId != 3 && numberOfUnlocked < 3 && !(currentPlantId > numberOfUnlocked)) {
            int finalCurrentPlantId = currentPlantId + 1;
            updatePlant(userId, finalCurrentPlantId);
            plantViewModel.getUnlockedPlants(userId);
            Toast.makeText(getContext(), "NEW PLANT UNLOCKED", Toast.LENGTH_LONG).show();
        }
    }
    private void isUnlockedPlant() {
        plantViewModel.getUnlockedPlants(userId);
        plantViewModel.addUnlockedPlantResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                observeUnlockedPlants(response);
            }
        });
    }
    private void updatePlant(int userId, int plantId){
        Log.i("UPDATED PLANT()", "START");
        plantViewModel.updateCurrentPlant(userId, plantId);
        plantViewModel.addUpdatedPlantResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                plantViewModel.getCurrentPlantDetails(userId);
            }
        });
        Toast.makeText(getContext(), "SWITCHED PLANT", Toast.LENGTH_LONG).show();
        toggleSwitchPlantOff();
        Log.i("UPDATED PLANT()", "FINISHED");
    }

    private void toggleSelectPlant(Boolean on) {
        if (on) {
            progressBar.setVisibility(View.INVISIBLE);
            plantGrowth.setVisibility(View.INVISIBLE);
            selectPlantButton.setVisibility(View.VISIBLE);
        } else {
            selectPlantButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            plantGrowth.setVisibility(View.VISIBLE);
            selectPlantText.setVisibility(View.INVISIBLE);
            switchedPlant.setVisibility(View.GONE);
            plantPhoto.setVisibility(View.VISIBLE);
        }
    }

    public void adjustToKeyboard () {
        ScrollView scroll = waterPlantBinding.waterPlantScrollView;
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                scroll.getWindowVisibleDisplayFrame(r);
                int screenHeight = scroll.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Adjust the bottom padding of the scroll view
                scroll.setPadding(0, 0, 0, keypadHeight);
            }
        });

    }

}