package edu.tacoma.uw.bloommoods;

import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.tacoma.uw.bloommoods.databinding.FragmentTodaysEntryBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class TodaysEntryFragment extends Fragment {

    private JournalViewModel mJournalViewModel;
    FragmentTodaysEntryBinding mTodaysEntryBinding;
    private String mSelectedMood;
    private int currentUser;
    private static final Map<Integer, String> moodMap = new HashMap<>();

    static {
        moodMap.put(R.mipmap.excited, "Excited");
        moodMap.put(R.mipmap.happy, "Happy");
        moodMap.put(R.mipmap.neutral, "Neutral");
        moodMap.put(R.mipmap.sad, "Sad");
        moodMap.put(R.mipmap.anxious, "Anxious");
        moodMap.put(R.mipmap.angry, "Angry");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        mTodaysEntryBinding = FragmentTodaysEntryBinding.inflate(inflater, container, false);
        return mTodaysEntryBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UserViewModel mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                currentUser = userId;
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry
            }
        });

        // Observe the entry LiveData
        mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), this::updateUI);

//        TodaysEntryFragmentArgs args = TodaysEntryFragmentArgs.fromBundle(getArguments());
//        JournalEntry entry = args.getEntry();
//        updateUI(entry);

        mTodaysEntryBinding.editButton.setOnClickListener(button -> setEditable(true));
        mTodaysEntryBinding.updateButton.setOnClickListener(button -> {
            updateEntry(currentUser);
            setEditable(false);
        });

        mUserViewModel.getCurrentPlantDetails(currentUser);
        mUserViewModel.addPlantResponseObserver(getViewLifecycleOwner(), response -> {
            if (response.has("stage") && response.has("growthLevel") && response.has("name")) {
                int stage;
                ImageView plantStage = mTodaysEntryBinding.plantImageView;
                String activePlantName = null;
                try {
                    activePlantName = response.getString(("name"));
                    stage = response.getInt("stage");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                String resourceName = activePlantName.toLowerCase().replace(" ", "_") + "_stage_" + stage;
                int resourceId = getResources().getIdentifier(resourceName, "drawable", requireActivity().getPackageName());
                if (resourceId != 0) {
                    Drawable drawable = ContextCompat.getDrawable(requireContext(), resourceId);
                    plantStage.setImageDrawable(drawable);
                } else {
                    Log.e("TodaysEntryFragment", "Drawable resource not found: " + resourceName);
                }
            }
        });

//        // Observe userId from UserViewModel
//        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
//            if (userId != null) {
//                currentUser = userId;
//            }
//        });
//
//        TodaysEntryFragmentArgs args = TodaysEntryFragmentArgs.fromBundle(getArguments());
//        JournalEntry entry = (JournalEntry) args.getEntry();
//        mSelectedMood = moodMap.get(entry.getMoodImage());
//        mTodaysEntryBinding.todaysDate.setText(entry.getDate());
//        mTodaysEntryBinding.todaysTitleEditText.setText(entry.getTitle());
//        mTodaysEntryBinding.todaysEntryEditText.setText(entry.getContent());
//        mTodaysEntryBinding.todaysMoodImageView.setImageResource(entry.getMoodImage());
//        mTodaysEntryBinding.editButton.setOnClickListener(button -> setEditable(true));
//        mTodaysEntryBinding.updateButton.setOnClickListener(button -> {updateEntry(currentUser); setEditable(false);});

//
        LinearLayout moodLayout = mTodaysEntryBinding.linearLayout;
        setOnMoodClicks(moodLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mJournalViewModel = null;
    }

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

    private void updateEntry(int userId) {
        String title = mTodaysEntryBinding.todaysTitleEditText.getText().toString();
        String entry = mTodaysEntryBinding.todaysEntryEditText.getText().toString();
        mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
    }

    private void updateUI(JournalEntry entry) {
        mSelectedMood = moodMap.get(entry.getMoodImage());
        mTodaysEntryBinding.moodTextView.setText(mSelectedMood);
        mTodaysEntryBinding.todaysDate.setText(entry.getDate());
        mTodaysEntryBinding.todaysTitleEditText.setText(entry.getTitle());
        mTodaysEntryBinding.todaysEntryEditText.setText(entry.getContent());
        mTodaysEntryBinding.todaysMoodImageView.setImageResource(entry.getMoodImage());
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
        mSelectedMood = view.getTag().toString();
        mTodaysEntryBinding.moodTextView.setText(mSelectedMood);
    }

    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("error")) {
                try {
                    Toast.makeText(this.getContext(),
                            "Error updating entry: " +
                                    response.get("error"), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }

            } else {
                Toast.makeText(this.getContext(),"Entry updated", Toast.LENGTH_LONG).show();
                mJournalViewModel.getTodaysEntry(currentUser); // Fetch today's entry
//                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), this::updateUI);
            }

        } else {
            Log.d("JSON Response", "No Response");
        }
    }
}