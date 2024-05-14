package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {
    private JournalViewModel mJournalViewModel;
    FragmentWaterPlantBinding mWaterPlantBinding;
    String mSelectedMood;
    int currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mWaterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        TextView date = mWaterPlantBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        TextView plantGrowth = mWaterPlantBinding.plantGrowth;
        float[] radii = {50, 50, 50, 50, 50, 50, 50, 50};
        RoundRectShape roundRectShape = new RoundRectShape(radii, null,null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        int color = Color.parseColor("#4DFFD6C7");
        shapeDrawable.getPaint().setColor(color);
        plantGrowth.setBackground(shapeDrawable);

//        Button saveButton = waterPlantBinding.saveButton;
//        saveButton.setOnClickListener(v -> addEntry());

        return mWaterPlantBinding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UserViewModel mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry using the userId
                currentUser = userId;
                mWaterPlantBinding.saveButton.setOnClickListener(button -> addOrUpdateEntry(userId, mWaterPlantBinding.titleEditText, mWaterPlantBinding.entryEditText));
            }
        });

        mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
            updateLayoutVisibility(moodEntry != null);
            if (moodEntry != null) {
                EditText title = mWaterPlantBinding.todaysTitleEditText;
                EditText entry = mWaterPlantBinding.todaysEntryEditText;
                title.setText(moodEntry.getTitle());
                entry.setText(moodEntry.getContent());
                mWaterPlantBinding.todaysDate.setText(moodEntry.getDate());
                mWaterPlantBinding.editButton.setOnClickListener(button -> setEditable());
                mWaterPlantBinding.updateButton.setOnClickListener(button -> addOrUpdateEntry(currentUser, title, entry));
            }
        });

        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);

        LinearLayout moodLayout = mWaterPlantBinding.linearLayout;
        setOnMoodClicks(moodLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWaterPlantBinding = null;
    }

    private void addOrUpdateEntry(int userId, EditText titleEditText, EditText entryEditText) {
        String title = titleEditText.getText().toString();
        String entry = entryEditText.getText().toString();
//        String mood = mSelectedMood != null ? mSelectedMood :
        mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
    }

    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("error")) {
                try {
                    Toast.makeText(this.getContext(),
                            "Error Adding/Updating entry: " +
                                    response.get("error"), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }

            } else {
                Toast.makeText(this.getContext(),"Entry added/updated", Toast.LENGTH_LONG).show();
                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
                    updateLayoutVisibility(moodEntry != null);
                    if (moodEntry != null) {
                        mWaterPlantBinding.todaysTitleEditText.setText(moodEntry.getTitle());
                        mWaterPlantBinding.todaysEntryEditText.setText(moodEntry.getContent());
                        mWaterPlantBinding.todaysDate.setText(moodEntry.getDate());
                        mWaterPlantBinding.editButton.setOnClickListener(button -> setEditable());
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
        mSelectedMood = view.getTag().toString();
        Log.i("Selected Mood", mSelectedMood);
    }

    private void updateLayoutVisibility(boolean hasEntry) {
        int newEntryVisibility = hasEntry ? View.GONE : View.VISIBLE;
        int hasEntryVisibility = hasEntry ? View.VISIBLE : View.GONE;
        mWaterPlantBinding.newEntryConstraintLayout.setVisibility(newEntryVisibility);
        mWaterPlantBinding.hasEntryConstraintLayout.setVisibility(hasEntryVisibility);
        Log.i("WaterPlantFragment", hasEntry ? "Entry exists for today." : "No entry found for today.");
    }

    private void setEditable() {
        mWaterPlantBinding.todaysTitleEditText.setLongClickable(true);
        mWaterPlantBinding.todaysTitleEditText.setClickable(true);
        mWaterPlantBinding.todaysEntryEditText.setLongClickable(true);
        mWaterPlantBinding.todaysEntryEditText.setClickable(true);
        mWaterPlantBinding.editButton.setVisibility(View.GONE);
        mWaterPlantBinding.updateButton.setVisibility(View.VISIBLE);
    }
}