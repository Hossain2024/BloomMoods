package edu.tacoma.uw.bloommoods;

import android.app.Activity;
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
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {
    private JournalViewModel mJournalViewModel;
    FragmentWaterPlantBinding mWaterPlantBinding;
    private UserViewModel mUserViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("WaterPlantFragment", "CREATED VIEW");
        mWaterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mJournalViewModel = ((MainActivity) requireActivity()).getJournalViewModel();
//        mJournalViewModel = new ViewModelProvider(getActivity()).get(JournalViewModel.class);

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
                    Log.i("WaterPlantFragment", "OBSERVING ENTRY");
                    Log.i("WaterPlantFragment", String.valueOf(moodEntry));
                    if (moodEntry != null) {
                        Log.i("WaterPlantFragment", "GOING TO TODAYS ENTRY");
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