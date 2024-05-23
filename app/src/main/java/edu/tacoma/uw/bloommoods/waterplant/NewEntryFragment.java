package edu.tacoma.uw.bloommoods.waterplant;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.journal.JournalViewModel;
import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.FragmentNewEntryBinding;

/**
 * A Fragment that displays the Water Plant page (i.e. adding a new journal entry)
 *
 * @author Chelsea Dacones
 * @author Amanda Nguyen
 */
public class NewEntryFragment extends Fragment {
    private FragmentNewEntryBinding mNewEntryBinding;
    private JournalViewModel mJournalViewModel;
    private PlantViewModel mPlantViewModel;
    private UserViewModel mUserViewModel;
    private Button saveButton, resetButton;
    private EditText titleEditText, entryEditText;
    private ImageButton selectPlantButton;
    private ImageView plantPhoto, leftArrow,rightArrow,switchedPlant;
    private LinearLayout moodLayout;
    private ProgressBar progressBar;
    private TextView plantGrowth, selectPlantText;
    private String activePlantName, currentPlantSwitch,mSelectedMood;
    private boolean loggedToday;
    private double plantGrowthPercent;
    private int streak, numberOfUnlocked, activePlantId, mCurrentUserId;
    private int tulipStage, sunflowerStage,peonyStage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNewEntryBinding = FragmentNewEntryBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mPlantViewModel = ((MainActivity) requireActivity()).getPlantViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();

        mNewEntryBinding.dateText.setText(new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));

        ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{50, 50, 50, 50, 50, 50, 50, 50}, null, null));
        shapeDrawable.getPaint().setColor(Color.parseColor("#4DFFD6C7"));
        mNewEntryBinding.plantGrowth.setBackground(shapeDrawable);

        return mNewEntryBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mCurrentUserId = userId;
                mJournalViewModel.getTodaysEntry(userId);
                mNewEntryBinding.saveButton.setOnClickListener(button -> addEntry(userId));
                mPlantViewModel.getCurrentPlantDetails(userId);
                mPlantViewModel.addPlantResponseObserver(getViewLifecycleOwner() , response -> {
                    if (response.length() > 0) {
                        observeResponsePlantDetails(response);
                    }
                });
                mPlantViewModel.addPlantDetailResponseObserver(getViewLifecycleOwner() , response -> {
                    if (response.length() > 0) {
                        mPlantViewModel.getCurrentPlantDetails(userId);
                    }
                });
                mPlantViewModel.addResetPlantResponseObserver(getViewLifecycleOwner(), response -> {
                    if (response.length() > 0) {
                        mPlantViewModel.getCurrentPlantDetails(userId);
                    }
                });
                mUserViewModel.getUserProfile(userId);
                mUserViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponseUserProfile);
                isUnlockedPlant();
            }
        });
        initializeListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNewEntryBinding = null;
    }

    /**
     * Initializes listeners for various UI elements.
     */
    private void initializeListeners() {
        resetButton = mNewEntryBinding.resetButton;
        titleEditText = mNewEntryBinding.titleEditText;
        entryEditText = mNewEntryBinding.entryEditText;
        selectPlantButton = mNewEntryBinding.selectPlantButton;
        plantPhoto = mNewEntryBinding.plantStage;
        saveButton = mNewEntryBinding.saveButton;
        leftArrow = mNewEntryBinding.leftArrow;
        rightArrow = mNewEntryBinding.rightArrow;
        switchedPlant = mNewEntryBinding.plantStageSwitch;
        selectPlantText = mNewEntryBinding.selectPlantText;
        progressBar = mNewEntryBinding.progressBar;
        plantGrowth = mNewEntryBinding.plantGrowth;
        moodLayout = mNewEntryBinding.linearLayout;

        leftArrow.setOnClickListener(v -> switchArrows("left"));
        rightArrow.setOnClickListener(v -> switchArrows("right"));
        resetButton.setOnClickListener(v -> showConfirmationDialog());

        mNewEntryBinding.switchButton.setOnClickListener(v -> toggleSwitchPlant());

        setOnMoodClicks();
        adjustToKeyboard();
    }

    /**
     * Adds a journal entry for the user.
     *
     * @param userId The user ID.
     */
    private void addEntry(int userId) {
        String title = titleEditText.getText().toString();
        String entry = entryEditText.getText().toString();

        if (mSelectedMood == null) {
            Toast.makeText(this.getContext(),"Please select a mood", Toast.LENGTH_LONG).show();
        } else if (title.isEmpty() || entry.isEmpty()) {
            Toast.makeText(this.getContext(),"Please enter a title and entry", Toast.LENGTH_LONG).show();
        } else {
            mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
            mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
            addGrowth();
        }
    }

    /**
     * Observes the response from adding a journal entry.
     *
     * @param response The JSON response from the server.
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("error")) {
                try {
                    Toast.makeText(this.getContext(), "Error Adding entry: " + response.get("error"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", Objects.requireNonNull(e.getMessage()));
                }
            } else {
                Toast.makeText(this.getContext(),"Entry added", Toast.LENGTH_LONG).show();
                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
                    if (moodEntry != null) {
                        NewEntryFragmentDirections.ActionNewEntryFragmentToTodaysEntryFragment directions =
                                NewEntryFragmentDirections.actionNewEntryFragmentToTodaysEntryFragment(moodEntry);
                        Navigation.findNavController(requireView()).navigate(directions);
                    } else {
                        Log.e("NewEntryFragment", "Mood entry is null");
                    }
                });
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }

    /**
     * Sets onClick listeners for mood selection.
     */
    private void setOnMoodClicks() {
        mNewEntryBinding.anxiousImageView.setOnClickListener(this::onMoodClicked);
        mNewEntryBinding.excitedImageView.setOnClickListener(this::onMoodClicked);
        mNewEntryBinding.happyImageView.setOnClickListener(this::onMoodClicked);
        mNewEntryBinding.sadImageView.setOnClickListener(this::onMoodClicked);
        mNewEntryBinding.neutralImageView.setOnClickListener(this::onMoodClicked);
        mNewEntryBinding.angryImageView.setOnClickListener(this::onMoodClicked);
    }


    /**
     * Handles the click event for mood selection.
     *
     * @param view The clicked view.
     */
    private void onMoodClicked(View view) {
        if (mNewEntryBinding.moodTextView.getVisibility() == View.GONE) {
            mNewEntryBinding.moodTextView.setVisibility(View.VISIBLE);
        }
        Log.i("NewEntryFragment onMoodClicked", (String) view.getTag());
        mSelectedMood = view.getTag().toString();
        mNewEntryBinding.moodTextView.setText(mSelectedMood);
    }

    /** All methods below were written by Amanda, due to conflicts more difficult to resolve, Chelsea manually cut and paste the code here,
     * resulting in Git showing Chelsea as the author.*/

    /**
     * Sets the text and image for the plant growth and details.
     */
    private void setTextImage() {
        mNewEntryBinding.dateText.setText(new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));

        // Update current growth according to user's current plant details
        String growth = "Current Growth: " + plantGrowthPercent + "%";
        plantGrowth.setText(growth);
        progressBar.setProgress((int) plantGrowthPercent);
    }

    /**
     * Sets the image for the plant stage.
     *
     * @param view      The ImageView to set the image on.
     * @param plantStage The plant stage.
     * @param plantName The plant name.
     */
    private void setPlantImage(ImageView view, int plantStage, String plantName) {
        String resourceName = plantName.toLowerCase().replace(" ", "_") + "_stage_" + plantStage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            mUserViewModel.getReset().observe(getViewLifecycleOwner(), reset -> {
                if (reset) {
                    setSaturation(view, 0f);
                }
            });
            view.setImageDrawable(drawable);
        } else {
            Log.e("WaterPlantFragment", "Drawable resource not found: " + resourceName);
        }
    }

    /**
     * Adds growth to the current plant.
     */
    private void addGrowth() {
        loggedToday = false;
        mUserViewModel.getLastEntryLogged().observe(getViewLifecycleOwner(), entry -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            if (!entry.equals("null")) {
                try {
                    // Parse the date string into a Date object
                    Date lastLoggedDate = dateFormat.parse(entry);

                    // Get the current date and time
                    Date currentDate = new Date();

                    // Check if lastLoggedDate is within the last 24 hours
                    long diff = currentDate.getTime() - Objects.requireNonNull(lastLoggedDate).getTime();
                    long hours = diff / (60 * 60 * 1000);

                    if (hours <= 24) {
                        loggedToday = true;
                    }
                } catch (ParseException e) {
                    System.out.println("Error parsing the date: " + e.getMessage());
                }
            }
        });

        double increaseGrowth = streak >= 2 ? 5 : 2.5;

        if (loggedToday) {
            // Get current user ID
            mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
                if (userId != null) {
                    mPlantViewModel.updateCurrentPlantDetails(userId, increaseGrowth);
                    if (plantGrowthPercent + increaseGrowth >= 100) {
                        unlockNewPlant(activePlantId);
                    }
                }
            });
            mUserViewModel.getReset().observe(getViewLifecycleOwner(), reset -> {
                if (reset) {
                    setSaturation(plantPhoto, 1.0f);
                    mUserViewModel.setReset(false);
                }
            });
        }
    }

    /**
     * Observes the response for plant details.
     *
     * @param response The JSON response from the server.
     */
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
                    int activePlantStage = response.getInt("stage");
                    setTextImage();
                    setPlantImage(plantPhoto, activePlantStage, activePlantName);
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", Objects.requireNonNull(e.getMessage()));
            }
        } else{
            Log.e("Plant details response", "Could not obtain plant details OBSERVE");
        }
    }


    /**
     * Observes the response for unlocked plants.
     *
     * @param response The JSON response from the server.
     */
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

    /**
     * Observes the response for user profile details.
     *
     * @param response The JSON response from the server.
     */
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
                Log.e("JSON Parse Error", Objects.requireNonNull(e.getMessage()));
            }
        }else{
            Log.e("User details", "Could not obtain user details");
        }
    }

    /**
     * Sets the saturation of the plant image view.
     *
     * @param imageView  The ImageView to set the saturation on.
     * @param saturation The saturation level.
     */
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
            resetButton.setVisibility(View.VISIBLE);
            entryEditText.setVisibility(View.GONE);
            titleEditText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            moodLayout.setVisibility(View.GONE);
            mNewEntryBinding.moodTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Toggles the plant switching UI elements.
     */
    private void toggleSwitchPlantOff() {
        resetButton.setVisibility(View.GONE);
        rightArrow.setVisibility(View.GONE);
        leftArrow.setVisibility(View.GONE);
        toggleSelectPlant(false);
        moodLayout.setVisibility(View.VISIBLE);
        entryEditText.setVisibility(View.VISIBLE);
        titleEditText.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        mNewEntryBinding.moodTextView.setVisibility(View.VISIBLE);
        currentPlantSwitch = activePlantName;

    }

    /**
     * Updates the visibility of the arrow buttons based on the current plant.
     *
     * @param plantName The name of the current plant.
     */
    private void updateArrows(String plantName) {
        switch (plantName) {
            case "Tranquil Tulip":
                rightArrow.setVisibility(View.VISIBLE);
                leftArrow.setVisibility(View.GONE);
                break;
            case "Serenity Sunflower":
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
                break;
            case "Peaceful Peony":
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Selects a plant for the user.
     *
     * @param plantName   The name of the plant.
     * @param plantOptionId The plant option ID.
     */
    private void selectPlant(String plantName, int plantOptionId) {
        resetButton.setVisibility(View.GONE);
        toggleSelectPlant(true);

        String resourceName = "select_plant_locked";
        String selected = "Not yet unlocked";
        selectPlantText.setVisibility(View.VISIBLE);
        int color = ContextCompat.getColor(requireContext(), R.color.black);

        updateArrows(plantName);

        switchedPlant.setVisibility(View.VISIBLE);

        if (numberOfUnlocked < plantOptionId && plantOptionId != activePlantId) {
            setPlantImage(switchedPlant, 1, plantName);
            setSaturation(switchedPlant, 0f);
        } else if (!(numberOfUnlocked < plantOptionId) && plantOptionId != activePlantId) {
            selected = "Select Plant";
            resourceName = "select_plant_unlocked";
            color = ContextCompat.getColor(getContext(), R.color.dark_green);
            selectPlantButton.setOnClickListener(v -> updatePlant(mCurrentUserId, plantOptionId));
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
            resetButton.setVisibility(View.VISIBLE);
        }
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            selectPlantButton.setImageDrawable(drawable);
        }
        selectPlantText.setTextColor(color);
        selectPlantText.setText(selected);

    }

    /**
     * Switches the plant based on the direction of the arrow clicked.
     *
     * @param direction The direction to switch the plant.
     */
    private void switchArrows(String direction) {
        plantPhoto.setVisibility(View.INVISIBLE);
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

    /**
     * Unlocks a new plant for the user if certain conditions are met.
     *
     * @param currentPlantId The current plant ID.
     */
    private void unlockNewPlant(int currentPlantId) {
        if (currentPlantId != 3 && numberOfUnlocked < 3 && !(currentPlantId > numberOfUnlocked)) {
            int finalCurrentPlantId = currentPlantId + 1;
            updatePlant(mCurrentUserId, finalCurrentPlantId);
            mPlantViewModel.getUnlockedPlants(mCurrentUserId);
            Toast.makeText(getContext(), "NEW PLANT UNLOCKED", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if the plant is unlocked.
     */
    private void isUnlockedPlant() {
        mPlantViewModel.getUnlockedPlants(mCurrentUserId);
        mPlantViewModel.addUnlockedPlantResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                observeUnlockedPlants(response);
            }
        });
    }

    /**
     * Updates the current plant for the user.
     *
     * @param userId  The user ID.
     * @param plantId The plant ID.
     */
    private void updatePlant(int userId, int plantId){
        mPlantViewModel.updateCurrentPlant(userId, plantId);
        mPlantViewModel.addUpdatedPlantResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.length() > 0) {
                mPlantViewModel.getCurrentPlantDetails(userId);
            }
        });
        Toast.makeText(getContext(), "SWITCHED PLANT", Toast.LENGTH_LONG).show();
        toggleSwitchPlantOff();
    }

    /**
     * Resets the current plant for the user.
     *
     * @param userId  The user ID.
     * @param plantId The plant ID.
     */
    private void resetPlant(int userId, int plantId) {
        mPlantViewModel.resetCurrentPlant(userId, plantId);
    }

    /**
     * Toggles the visibility of the plant selection UI elements.
     *
     * @param on Whether to show the plant selection UI elements.
     */
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

    /**
     * Adjusts the scroll view to the keyboard height.
     */
    public void adjustToKeyboard () {
        ScrollView scroll = mNewEntryBinding.waterPlantScrollView;
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            scroll.getWindowVisibleDisplayFrame(r);
            int screenHeight = scroll.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            // Adjust the bottom padding of the scroll view
            scroll.setPadding(0, 0, 0, keypadHeight);
        });
    }

    /**
     * Shows a confirmation dialog for resetting the plant.
     */
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("All plant growth will be lost. Are you sure?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    resetPlant(mCurrentUserId, activePlantId);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }
}