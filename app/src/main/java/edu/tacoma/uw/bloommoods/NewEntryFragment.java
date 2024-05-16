package edu.tacoma.uw.bloommoods;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.databinding.FragmentNewEntryBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEntryFragment extends Fragment {

    private JournalViewModel mJournalViewModel;
    private UserViewModel mUserViewModel;
    FragmentNewEntryBinding mNewEntryBinding;
    private String mSelectedMood;
    private int streak;
    private ImageView plantPhoto;
    private boolean loggedToday;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNewEntryBinding = FragmentNewEntryBinding.inflate(inflater, container, false);
        TextView date = mNewEntryBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        TextView plantGrowth = mNewEntryBinding.plantGrowth;
        float[] radii = {50, 50, 50, 50, 50, 50, 50, 50};
        RoundRectShape roundRectShape = new RoundRectShape(radii, null,null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        int color = Color.parseColor("#4DFFD6C7");
        shapeDrawable.getPaint().setColor(color);
        plantGrowth.setBackground(shapeDrawable);

        return mNewEntryBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry using the userId
                mNewEntryBinding.saveButton.setOnClickListener(button -> addEntry(userId));
                mUserViewModel.getCurrentPlantDetails(userId);
                mUserViewModel.addPlantResponseObserver(getViewLifecycleOwner(), this::observeResponsePlantDetails);
                mUserViewModel.getUserProfile(userId);
                mUserViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponseUserProfile);
            }
        });

        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);

        LinearLayout moodLayout = mNewEntryBinding.linearLayout;
        setOnMoodClicks(moodLayout);
        adjustToKeyboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNewEntryBinding = null;
    }

    private void addEntry(int userId) {
        String title = mNewEntryBinding.titleEditText.getText().toString();
        String entry = mNewEntryBinding.entryEditText.getText().toString();
        if (mSelectedMood == null) {
            Toast.makeText(this.getContext(),"Please select a mood", Toast.LENGTH_LONG).show();
        } else if (title.isEmpty() || entry.isEmpty()) {
            Toast.makeText(this.getContext(),"Please enter a title and entry", Toast.LENGTH_LONG).show();
        } else {
            mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
            addGrowth();
        }
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
                Log.i("NewEntryFragment", "Response received, now observing mEntry");
                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
                    if (moodEntry != null) {
                        // Navigate to TodaysEntryFragment with the moodEntry
                        NewEntryFragmentDirections.ActionNewEntryFragmentToTodaysEntryFragment directions =
                                NewEntryFragmentDirections.actionNewEntryFragmentToTodaysEntryFragment(moodEntry);
                        Navigation.findNavController(getView()).navigate(directions);
                    } else {
                        // Log if moodEntry is null
                        Log.e("NewEntryFragment", "Mood entry is null");
                    }
                });
            }

        } else {
            Log.d("JSON Response", "No Response");
        }
    }


    private void setOnMoodClicks(LinearLayout moodLayout) {
        for (int i = 0; i < moodLayout.getChildCount(); i++) {
            View childView = moodLayout.getChildAt(i);
            if (childView instanceof ImageView) {
                childView.setOnClickListener(this::onMoodClicked);
            }
        }
    }

    private void onMoodClicked(View view) {
        if (mNewEntryBinding.moodTextView.getVisibility() == View.GONE) {
            mNewEntryBinding.moodTextView.setVisibility(View.VISIBLE);
        }
        mSelectedMood = view.getTag().toString();
        mNewEntryBinding.moodTextView.setText(mSelectedMood);
    }

    /** All methods below were written by Amanda, due to conflicts Chelsea had to manually cut and paste the code here,
     * resulting in Git showing Chelsea as the author.*/
    private void setTextImage(double currentGrowth, int plantStage, String plantName) {
        TextView date = mNewEntryBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        TextView plantGrowth = mNewEntryBinding.plantGrowth;

        // Update current growth according to user's current plant details
        String growth = "Current Growth: " + currentGrowth + "%";
        plantGrowth.setText(growth);

        ProgressBar progressBar = mNewEntryBinding.progressBar;
        progressBar.setProgress((int) currentGrowth);

        plantPhoto = mNewEntryBinding.plantStage;
        String resourceName = plantName.toLowerCase().replace(" ", "_") + "_stage_" + plantStage;
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
            mUserViewModel.getReset().observe(getViewLifecycleOwner(), reset -> {
                if (reset) {
                    setSaturation(plantPhoto, 0f);
                }
            });
            plantPhoto.setImageDrawable(drawable);
        } else {
            Log.e("HomeFragment", "Drawable resource not found: " + resourceName);
        }
    }

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

    private void addGrowth() {
        loggedToday = false;
        mUserViewModel.getLastEntryLogged().observe(getViewLifecycleOwner(), entry -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                Date lastLoggedDate = dateFormat.parse(entry);
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
        });


        double increaseGrowth;
        if (streak >= 2) {
            increaseGrowth = 5;
        } else {
            increaseGrowth = 2.5;
        }
        if (!loggedToday) {
            // Get current user ID
            mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
                if (userId != null) {
                    mUserViewModel.updateCurrentPlantDetails(userId, increaseGrowth);
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

    private void setSaturation(ImageView imageView, float saturation) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(saturation);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        imageView.setColorFilter(filter);
    }
}