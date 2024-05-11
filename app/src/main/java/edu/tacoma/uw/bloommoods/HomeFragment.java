package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import edu.tacoma.uw.bloommoods.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private String userName;
    private int userStreak;
    private int plantGrowth = 0;
    private int userEntries;
    private UserViewModel mUserViewModel;
    private TextView editText;  // Declare editText here
    private TextView usernameText;
    private TextView entriesText;
    private FragmentHomeBinding homeBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return homeBinding.getRoot();
    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                // Call the method in ViewModel to perform the API operation
                mUserViewModel.getUserProfile(userId);
                Log.i("Home", String.valueOf(userId));
            }
        });
        mUserViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            observeResponse(response);
        });

    }


    @Override
    public void onDestroyView() {
        Log.d("FragmentManager", "HomeFragment destroyed.");
        super.onDestroyView();
        homeBinding = null;
    }

    private void observeResponse(final JSONObject response) {
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
                    if (response.has("name") && response.has("streak") && response.has("total_entries")) {
                        userName = response.getString("name");
                        Log.i("Username", userName);
                        userStreak = response.getInt("streak");
                        Log.i("User streak", String.valueOf(userStreak));
                        userEntries = response.getInt("total_entries");
                        setEditText();
                    }
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("User profile response", "Could not obtain profile data");
        }
        Log.i("HomeFragment", "FINISHED OBSERVE RESPONSE");
    }



    private void setEditText() {
        usernameText = homeBinding.textUserName;
        String name = "Hey, " + userName + "!";

        usernameText.setText(name);

        // Initialize EditText after setContentView
        editText = homeBinding.textStreak;

        String text = "Streak\n " + userStreak + "  days";
        SpannableString spannableString = new SpannableString(text);


        // Apply a size span to "Big Text"
        RelativeSizeSpan daysSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableString.setSpan(daysSpan, text.indexOf(String.valueOf(userStreak)), text.indexOf(String.valueOf(userStreak)) + String.valueOf(userStreak).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan smallTextSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableString.setSpan(smallTextSpan, text.indexOf("days"), text.indexOf("days") + "days".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        editText.setText(spannableString);

        entriesText = homeBinding.textEntries;
        String totalentries = "Total Entries\n " + userEntries + "  entries";
        SpannableString spannableStringEntries = new SpannableString(totalentries);

        // Apply a size span to "Big Text"
        RelativeSizeSpan entriesSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableStringEntries.setSpan(entriesSpan, totalentries.indexOf(String.valueOf(userEntries)), totalentries.indexOf(String.valueOf(userEntries)) + String.valueOf(userEntries).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan entSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableStringEntries.setSpan(entSpan, totalentries.indexOf("entries"), totalentries.indexOf("entries") + "entries".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        entriesText.setText(spannableStringEntries);
    }
}
