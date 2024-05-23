package edu.tacoma.uw.bloommoods.journal;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.waterplant.PlantViewModel;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.FragmentTodaysEntryBinding;

/**
 * Fragment for displaying and editing today's journal entry.
 * It also displays the user's current plant.
 *
 * @author Chelsea Dacones
 * @author Amanda Nguyen
 */
public class TodaysEntryFragment extends Fragment {
    private FragmentTodaysEntryBinding mTodaysEntryBinding;
    private JournalViewModel mJournalViewModel;
    private PlantViewModel mPlantViewModel;
    private UserViewModel mUserViewModel;
    private String mSelectedMood;
    private int mCurrentUser;
    private static final Map<Integer, String> moodMap = new HashMap<>();

    // Populate moodMap with mood resource IDs and corresponding mood strings
    static {
        moodMap.put(R.mipmap.excited, "Excited");
        moodMap.put(R.mipmap.happy, "Happy");
        moodMap.put(R.mipmap.neutral, "Neutral");
        moodMap.put(R.mipmap.sad, "Sad");
        moodMap.put(R.mipmap.anxious, "Anxious");
        moodMap.put(R.mipmap.angry, "Angry");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTodaysEntryBinding = FragmentTodaysEntryBinding.inflate(inflater, container, false);
        mPlantViewModel = ((MainActivity) requireActivity()).getPlantViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();
        mUserViewModel =  ((MainActivity) requireActivity()).getUserViewModel();
        return mTodaysEntryBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mCurrentUser = userId;
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry
            }
        });

        // Observe the entry LiveData
        mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), this::updateUI);

        // Set click listeners for edit and update buttons
        mTodaysEntryBinding.editButton.setOnClickListener(button -> setEditable(true));
        mTodaysEntryBinding.updateButton.setOnClickListener(button -> {
            updateEntry(mCurrentUser);
            setEditable(false);
        });

        // Fetch and observe plant details for the current user
        mPlantViewModel.getCurrentPlantDetails(mCurrentUser);
        mPlantViewModel.addPlantResponseObserver(getViewLifecycleOwner(), this::observePlantResponse);

        // Set click listeners for mood selection
        setOnMoodClicks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTodaysEntryBinding = null;
    }

    /**
     * Sets the UI to editable or non-editable state.
     *
     * @param isEditing true to set the UI to editable state, false to set it to non-editable state.
     */
    private void setEditable(boolean isEditing) {
        int editButtonVisibility = isEditing ? View.GONE : View.VISIBLE;
        int updateButtonVisibility = isEditing ? View.VISIBLE : View.GONE;
        mTodaysEntryBinding.todaysTitleEditText.setLongClickable(isEditing);
        mTodaysEntryBinding.todaysTitleEditText.setClickable(isEditing);
        mTodaysEntryBinding.todaysEntryEditText.setLongClickable(isEditing);
        mTodaysEntryBinding.todaysEntryEditText.setClickable(isEditing);
        mTodaysEntryBinding.editButton.setVisibility(editButtonVisibility);
        mTodaysEntryBinding.updateButton.setVisibility(updateButtonVisibility);
        mTodaysEntryBinding.todaysMoodImageView.setVisibility(editButtonVisibility);
        mTodaysEntryBinding.linearLayout.setVisibility(updateButtonVisibility);
        mTodaysEntryBinding.moodTextView.setVisibility(updateButtonVisibility);
    }

    /**
     * Updates the journal entry with the current user's input.
     *
     * @param userId the current user ID.
     */
    private void updateEntry(int userId) {
        String title = mTodaysEntryBinding.todaysTitleEditText.getText().toString();
        String entry = mTodaysEntryBinding.todaysEntryEditText.getText().toString();
        mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
    }

    /**
     * Updates the UI with the provided journal entry.
     *
     * @param entry the journal entry to display.
     */
    private void updateUI(JournalEntry entry) {
        if (entry != null) {
            mSelectedMood = moodMap.get(entry.getMoodImage());
            mTodaysEntryBinding.moodTextView.setText(mSelectedMood);
            mTodaysEntryBinding.todaysDate.setText(entry.getDate());
            mTodaysEntryBinding.todaysTitleEditText.setText(entry.getTitle());
            mTodaysEntryBinding.todaysEntryEditText.setText(entry.getContent());
            mTodaysEntryBinding.todaysMoodImageView.setImageResource(entry.getMoodImage());
        }
    }

    /**
     * Sets click listeners for mood selection buttons.
     */
    private void setOnMoodClicks() {
        mTodaysEntryBinding.anxiousImageView.setOnClickListener(this::onMoodClicked);
        mTodaysEntryBinding.excitedImageView.setOnClickListener(this::onMoodClicked);
        mTodaysEntryBinding.happyImageView.setOnClickListener(this::onMoodClicked);
        mTodaysEntryBinding.sadImageView.setOnClickListener(this::onMoodClicked);
        mTodaysEntryBinding.neutralImageView.setOnClickListener(this::onMoodClicked);
        mTodaysEntryBinding.angryImageView.setOnClickListener(this::onMoodClicked);
    }

    /**
     * Handles mood button clicks and updates the selected mood.
     *
     * @param view the clicked mood button view.
     */
    private void onMoodClicked(View view) {
        mSelectedMood = view.getTag().toString();
        mTodaysEntryBinding.moodTextView.setText(mSelectedMood);
    }

    /**
     * Observes the response from the journal entry update request and fetches the updated entry.
     *
     * @param response the JSON response from the server.
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("error")) {
                try {
                    Toast.makeText(this.getContext(), "Error updating entry: " + response.get("error"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            } else {
                Toast.makeText(this.getContext(),"Entry updated", Toast.LENGTH_LONG).show();
                mJournalViewModel.getTodaysEntry(mCurrentUser);
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }

    /**
     * Observes the response from the plant details request and updates the plant image.
     * NOTE: Written by Amanda.
     * @param response the JSON response from the server.
     */
    private void observePlantResponse(final JSONObject response) {
        if (response.length() > 0) {
            try {
                int stage = response.getInt("stage");
                String activePlantName = response.getString("name");
                String resourceName = activePlantName.toLowerCase().replace(" ", "_") + "_stage_" + stage;
                int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());
                if (resourceId != 0) {
                    Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
                    mTodaysEntryBinding.plantImageView.setImageDrawable(drawable);
                } else {
                    Log.e("TodaysEntryFragment", "Drawable resource not found: " + resourceName);
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }
}