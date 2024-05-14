package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWaterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
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
            }
        });

        mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
            if (moodEntry != null) {
                WaterPlantFragmentDirections.ActionNavWaterToTodaysEntryFragment directions =
                        WaterPlantFragmentDirections.actionNavWaterToTodaysEntryFragment(moodEntry);
                Navigation.findNavController(getView())
                        .navigate(directions);
            } else {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_nav_water_to_newEntryFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWaterPlantBinding = null;
    }
}