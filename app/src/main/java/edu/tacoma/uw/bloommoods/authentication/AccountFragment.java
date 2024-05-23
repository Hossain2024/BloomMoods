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
import androidx.core.content.ContextCompat;
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
import edu.tacoma.uw.bloommoods.waterplant.PlantViewModel;

/**
 * Fragment for managing user account settings.
 *
 * @author Rainie Chi
 */
public class AccountFragment extends Fragment {
    /**
     * API URL for updating user profile.
     */
    private static final String UPDATE_PROFILE_API_URL = "https://students.washington.edu/nchi22/api/users/update_profile.php";
    private int COLOR_ERROR;
    private int COLOR_SUCCESS;

    private UserViewModel mUserViewModel;
    private PlantViewModel mPlantViewModel;
    private FragmentAccountBinding binding;
    private int userID;
    private List<String> unlockedPlants;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        COLOR_ERROR = ContextCompat.getColor(getContext(), R.color.red);
        COLOR_SUCCESS = ContextCompat.getColor(getContext(), R.color.dark_green);
        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        mPlantViewModel = new ViewModelProvider(requireActivity()).get(PlantViewModel.class);
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

    /**
     * Sets up listeners for UI interactions.
     */
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


    /**
     * Switches the UI to update mode for editing text fields.
     *
     * @param editText The EditText to be enabled for editing.
     * @param button The button associated with the edit action.
     * @param iconResource The resource ID of the icon to be set for the button.
     */
    private void switchToUpdateMode(EditText editText, ImageButton button, int iconResource) {
        editText.setEnabled(true);
        button.setImageResource(iconResource);
        button.setTag("save");
    }

    /**
     * Observes the user profile data from the ViewModel.
     */
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

    /**
     * Shares the app link with a message about unlocked plants.
     */
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

    /**
     * Updates the UI with the user profile data.
     *
     * @param response The JSONObject containing user profile data.
     */
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

    /**
     * Updates the user name on the server.
     */
    private void updateUserName() {
        handleUpdate("name", binding.nameEditText, binding.nameUpdateResult, binding.editNameButton);
    }

    /**
     * Updates the user password on the server.
     */
    private void updatePassword() {
        handleUpdate("password", binding.passwordEditText, binding.passwordUpdateResult, binding.editPasswordButton);
    }

    /**
     * Handles the update request for the specified field.
     *
     * @param field The field to be updated.
     * @param editText The EditText containing the new value.
     * @param resultView The TextView to display the result message.
     * @param button The button associated with the update action.
     */
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


    /**
     * Fetches the unlocked plants for the user from the server.
     */
    private void fetchUserPlants() {
        mPlantViewModel.getUnlockedPlants(userID);
        mPlantViewModel.addUnlockedPlantResponseObserver(getViewLifecycleOwner(), response -> {
            try {
                if (response.length() > 0) {
                    updatePlantsUI(response);
                }
            } catch (JSONException e) {
                Log.e("AccountFragment", "Error processing JSON response: " + e.getMessage());
                Toast.makeText(getContext(), "Error processing plants.", Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Updates the UI with the unlocked plants data.
     *
     * @param plants The JSONArray containing the unlocked plants data.
     * @throws JSONException If there is an error parsing the JSON data.
     */
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


    /**
     * Creates an ImageView for a plant icon.
     *
     * @param drawableId The resource ID of the drawable to be set for the ImageView.
     * @return The created ImageView.
     */
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

    /**
     * Converts a value in dp (density-independent pixels) to pixels.
     *
     * @param dp The value in dp to be converted.
     * @return The converted value in pixels.
     */
    private int convertDpToPixel(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}