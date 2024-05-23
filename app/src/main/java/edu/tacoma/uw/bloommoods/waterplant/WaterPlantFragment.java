package edu.tacoma.uw.bloommoods.waterplant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.tacoma.uw.bloommoods.journal.JournalViewModel;
import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.authentication.UserViewModel;
import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A Fragment for the Water Plant page.
 * Directs to either NewEntryFragment of TodaysEntryFragment based on status of current dates entry.
 *
 * @author Chelsea Dacones
 */
public class WaterPlantFragment extends Fragment {
    private FragmentWaterPlantBinding mWaterPlantBinding;
    private JournalViewModel mJournalViewModel;
    private UserViewModel mUserViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mWaterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();

        return mWaterPlantBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe userId from UserViewModel
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                Log.i("WaterPlantFragment", String.valueOf(userId));
                mJournalViewModel.getTodaysEntry(userId); // Fetch today's entry using the userId
            }
        });

        mJournalViewModel.getRequestCompleted().observe(getViewLifecycleOwner(), isCompleted -> {
            if (Boolean.TRUE.equals(isCompleted)) {
                mJournalViewModel.getEntry().observe(getViewLifecycleOwner(), moodEntry -> {
                    Log.i("WaterPlantFragment", String.valueOf(moodEntry));
                    if (moodEntry != null) {
                        Log.i("WaterPlantFragment", "GOING TO TODAY'S ENTRY");
                        WaterPlantFragmentDirections.ActionNavWaterToTodaysEntryFragment directions =
                                WaterPlantFragmentDirections.actionNavWaterToTodaysEntryFragment(moodEntry);
                        Navigation.findNavController(getView()).navigate(directions);
                    } else {
                        Log.i("WaterPlantFragment", "GOING TO NEW ENTRY");
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_nav_water_to_newEntryFragment);

                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWaterPlantBinding = null;
    }
}