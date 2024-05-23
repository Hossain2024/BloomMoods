package edu.tacoma.uw.bloommoods.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {



    private static final String UPDATE_PROFILE_API_URL = "https://students.washington.edu/nchi22/api/users/update_profile.php";
    private static final String FETCH_PLANT_API_URL = "https://students.washington.edu/nchi22/api/plants/get_plants_unlocked.php?user_id=";
    private static final int COLOR_ERROR = Color.parseColor("#610000");
    private static final int COLOR_SUCCESS = Color.parseColor("#8fb38f");

    private UserViewModel mUserViewModel;
    private FragmentAccountBinding binding;
    private int userID;
    private List<String> unlockedPlants;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        unlockedPlants = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListeners();
        observeUserProfile();
    }

    private void setUpListeners() {
        binding.editNameButton.setOnClickListener(v -> {
            if ("edit".equals(binding.editNameButton.getTag())) {
                switchToUpdateMode(binding.nameEditText, binding.editNameButton, R.mipmap.confirmicon);
            } else {
                updateUserName();
            }
        });

        binding.editPasswordButton.setOnClickListener(v -> {
            if ("edit".equals(binding.editPasswordButton.getTag())) {
                switchToUpdateMode(binding.passwordEditText, binding.editPasswordButton, R.mipmap.confirmicon);
            } else {
                updatePassword();
            }
        });

        binding.logoutButton.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).hideBottomNavigation();

                // Clear the SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.SignIN_PREFS),
                        Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean(getString(R.string.SignedIN), false).apply();
                sharedPreferences.edit().putInt("userId", 0).apply();

                // Navigate to the login fragment
                Navigation.findNavController(getView()).navigate(R.id.loginFragment);
            }
        });

        binding.referButton.setOnClickListener(v -> shareAppLink());

    }






    private void switchToUpdateMode(EditText editText, ImageButton button, int iconResource) {
        editText.setEnabled(true);
        button.setImageResource(iconResource);
        button.setTag("save");
    }

    private void observeUserProfile() {
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                this.userID = userId;
                fetchUserPlants();
                mUserViewModel.getUserProfile(userId);
                mUserViewModel.addResponseObserver(getViewLifecycleOwner(), this::updateUIWithUserProfile);

            }
        });
    }

    private void shareAppLink() {
        StringBuilder plantsMessage = new StringBuilder();
        if (!unlockedPlants.isEmpty()) {
            plantsMessage.append("I've unlocked ");
            for (int i = 0; i < unlockedPlants.size(); i++) {
                plantsMessage.append(unlockedPlants.get(i));
                if (i < unlockedPlants.size() - 1) {
                    plantsMessage.append(" and ");
                }
            }
            plantsMessage.append("! ");
        }

        String shareMessage = plantsMessage + "\nDownload BloomMoods now \uD83C\uDF31 ✨ \nhttps://drive.google.com/file/d/1Atew0-wJrSMIuFRnNVtlPKeCM24weni8/view?usp=drive_link";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void updateUIWithUserProfile(JSONObject response) {
        try {
            if (!response.has("error")) {
                String email = response.getString("email");
                String name = response.getString("name");

                binding.emailEditText.setText(email);
                binding.nameEditText.setText(name);
                binding.nameEditText.setEnabled(false);
                binding.editNameButton.setImageResource(R.mipmap.editicon);
                binding.editNameButton.setTag("edit");
            } else {
                String error = response.getString("error");
                Log.e("AccountFragment", "Error fetching user data: " + error);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("AccountFragment", "JSON parsing error: " + e.getMessage());
            Toast.makeText(getContext(), "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserName() {
        handleUpdate("name", binding.nameEditText, binding.nameUpdateResult, binding.editNameButton);
    }

    private void updatePassword() {
        handleUpdate("password", binding.passwordEditText, binding.passwordUpdateResult, binding.editPasswordButton);
    }

    private void handleUpdate(String field, EditText editText, TextView resultView, ImageButton button) {
        String newValue = editText.getText().toString().trim();
        if (newValue.isEmpty()) {
            resultView.setText(String.format(" : %s must not be empty", field));
            resultView.setTextColor(COLOR_ERROR);
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", userID);
            params.put(field, newValue);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.PUT, UPDATE_PROFILE_API_URL, params,
                    response -> {
                        try {
                            if ("success".equals(response.getString("result"))) {
                                resultView.setText(" : Update successful");
                                resultView.setTextColor(COLOR_SUCCESS);
                                editText.setEnabled(false);
                                button.setImageResource(R.mipmap.editicon);
                                button.setTag("edit");
                            } else if (response.has("error")) {
                                resultView.setText("Error: " + response.getString("error"));
                                resultView.setTextColor(COLOR_ERROR);
                            }
                        } catch (JSONException e) {
                            resultView.setText(" : Parsing error in response");
                            resultView.setTextColor(COLOR_ERROR);
                        }
                    },
                    error -> {
                        resultView.setText(" : Failed to update.");
                        resultView.setTextColor(COLOR_ERROR);
                    }) {
            };

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(requireContext()).add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("AccountFragment", "JSON Exception: " + e.getMessage());
            resultView.setText("Error creating JSON object.");
        }
    }


    private void fetchUserPlants() {
        String url = FETCH_PLANT_API_URL + userID;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            updatePlantsUI(response);
                        }
                    } catch (Exception e) {
                        Log.e("AccountFragment", "Error processing JSON response: " + e.getMessage());
                        Toast.makeText(getContext(), "Error processing plants.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    TextView emptyTextView = binding.emptyTextView;
                    emptyTextView.setVisibility(View.VISIBLE);
                }
        );

        Volley.newRequestQueue(requireContext()).add(jsonArrayRequest);
    }


    private void updatePlantsUI(JSONArray plants) throws JSONException {
        LinearLayout plantsContainer = binding.plantsContainer;
        plantsContainer.removeAllViews();
        unlockedPlants.clear();

        for (int i = 0; i < plants.length(); i++) {
            JSONObject plant = plants.getJSONObject(i);
            int plantOptionId = plant.getInt("plant_option_id");
            String plantName = null;
            ImageView imageView = null;

            switch (plantOptionId) {
                case 1:
                    plantName = "Tranquil Tulip";
                    imageView = createImageView(R.mipmap.tulip_icon);
                    break;
                case 2:
                    plantName = "Serenity Sunflower";
                    imageView = createImageView(R.mipmap.sunflower_icon);
                    break;
                case 3:
                    plantName = "Peaceful Peony";
                    imageView = createImageView(R.mipmap.peony_icon);
                    break;
            }

            if (plantName != null) {
                unlockedPlants.add(plantName);
            }

            if (imageView != null) {
                plantsContainer.addView(imageView);
            }
        }
    }


    private ImageView createImageView(int drawableId) {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                convertDpToPixel(65), // Width
                convertDpToPixel(65)  // Height
        );
        layoutParams.setMargins(convertDpToPixel(16), 0, 0, 0);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(drawableId);
        return imageView;
    }

    private int convertDpToPixel(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}