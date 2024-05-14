package edu.tacoma.uw.bloommoods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
            }
        });

        TodaysEntryFragmentArgs args = TodaysEntryFragmentArgs.fromBundle(getArguments());
        JournalEntry entry = (JournalEntry) args.getEntry();
        mSelectedMood = moodMap.get(entry.getMoodImage());
        mTodaysEntryBinding.todaysDate.setText(entry.getDate());
        mTodaysEntryBinding.todaysTitleEditText.setText(entry.getTitle());
        mTodaysEntryBinding.todaysEntryEditText.setText(entry.getContent());
        mTodaysEntryBinding.todaysMoodImageView.setImageResource(entry.getMoodImage());
        mTodaysEntryBinding.editButton.setOnClickListener(button -> setEditable(true));
        mTodaysEntryBinding.updateButton.setOnClickListener(button -> {updateEntry(currentUser); setEditable(false);});

//        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
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
    }

    private void updateEntry(int userId) {
        String title = mTodaysEntryBinding.todaysTitleEditText.getText().toString();
        String entry = mTodaysEntryBinding.todaysEntryEditText.getText().toString();
        mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
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
        Log.i("Selected Mood", mSelectedMood);
    }
}