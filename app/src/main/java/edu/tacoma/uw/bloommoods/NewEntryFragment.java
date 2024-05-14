package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentNewEntryBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEntryFragment extends Fragment {

    private JournalViewModel mJournalViewModel;
    FragmentNewEntryBinding mNewEntryBinding;
    private String mSelectedMood;

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
        UserViewModel mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry using the userId
                mNewEntryBinding.saveButton.setOnClickListener(button -> addEntry(userId));
            }
        });

        mJournalViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);

        LinearLayout moodLayout = mNewEntryBinding.linearLayout;
        setOnMoodClicks(moodLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNewEntryBinding = null;
    }

    private void addEntry(int userId) {
        String title = mNewEntryBinding.titleEditText.getText().toString();
        String entry = mNewEntryBinding.entryEditText.getText().toString();
        mJournalViewModel.addEntry(userId, title, mSelectedMood, entry);
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
                    NewEntryFragmentDirections.ActionNewEntryFragmentToTodaysEntryFragment directions =
                            NewEntryFragmentDirections.actionNewEntryFragmentToTodaysEntryFragment(moodEntry);
                    Navigation.findNavController(getView())
                            .navigate(directions);
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
}