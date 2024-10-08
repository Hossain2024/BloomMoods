package edu.tacoma.uw.bloommoods.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.tacoma.uw.bloommoods.MainActivity;
import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.databinding.FragmentLoginBinding;

/**
 * Fragment for user login.
 *
 * @author Maliha Hossain
 *
 */
public class LoginFragment extends Fragment {
    private FragmentLoginBinding mBinding;
    private UserViewModel mUserViewModel;

    private static final String TAG = "LoginFragment";

    private int userId = 0;

    /**
     * Navigates to the registration screen.
     */
    public void navigateToRegister() {
        Navigation.findNavController(getView())
                .navigate(R.id.action_loginFragment_to_registerFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUserViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        // Inflate the layout for this fragment
        mBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);
        mBinding.navToSignUpButton.setOnClickListener(button -> navigateToRegister());
        mBinding.logInButton.setOnClickListener(button-> signin());

    }

    /**
     * Handles user sign-in by validating input and authenticating the user.
     */
    public void signin() {
        String email = String.valueOf(mBinding.emailEdit.getText());
        String pwd = String.valueOf(mBinding.pwdEdit.getText());
        Account account;
        if(email.isEmpty() || pwd.isEmpty()){
            mBinding.errorLoginTextview.setText("All fields are required");
        }else {
            try {
                account = new Account(email, pwd, false);
            } catch (IllegalArgumentException ie) {
                Log.e(TAG, ie.getMessage());
                Toast.makeText(getContext(), ie.getMessage(), Toast.LENGTH_LONG).show();
                mBinding.errorLoginTextview.setText(ie.getMessage());
                return;
            }
            Log.i(TAG, email);
            mUserViewModel.authenticateUser(account);
        }
    }

    /**
     * Retrieves the user ID of the currently logged-in user.
     *
     * @return The user ID.
     */
    public int getUserId(){
        return userId;
    }

    /**
     * Observes the authentication response and updates the UI accordingly.
     *
     * @param response The JSON response from the authentication request.
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("result")) {
                    String result = response.getString("result");
                    if ("failed to login".equals(result)) {
                        // If the result is "failed to login", display the error message to the user
                        mBinding.errorLoginTextview.setText("Invalid credentials");
                    } else if ("success".equals(result)) {
                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_LONG).show();

                        // Check if the user ID is present in the response
                        if (response.has("user_id")) {
                            userId = response.getInt("user_id");
                            mUserViewModel.setUserId(userId);
                            Log.i("User id logged in:", String.valueOf(userId));
                            Activity activity = getActivity();
                            if (activity instanceof MainActivity) {
                                ((MainActivity) activity).showBottomNavigation();
                                ((MainActivity) activity).setupBottomNavigation();
                            }
                            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
                        }
                        // wrtting credentials to sharedpreferences
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.SignIN_PREFS),
                                Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean(getString(R.string.SignedIN), true).apply();
                        sharedPreferences.edit().putInt("userId", userId).apply();


                    } else {
                        // If the result is neither "failed to login" nor "success", log an error
                        Log.e("Login Response", "Unknown result: " + result);
                    }
                } else {
                    // Log a message if the "result" key is missing from the response
                    Log.d("Login Response", "Missing 'result' key in response");
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
                mBinding.errorLoginTextview.setText("User failed to authenticate");
            }
        }else{
            Log.e("Login Response", "email/password required");
        }
    }

}

