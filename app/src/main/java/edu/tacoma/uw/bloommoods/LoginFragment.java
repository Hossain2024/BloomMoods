package edu.tacoma.uw.bloommoods;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.tacoma.uw.bloommoods.databinding.FragmentLoginBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentRegisterBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private FragmentLoginBinding mBinding;
    private UserViewModel mUserViewModel;

    private static final String TAG = "LoginFragment";

    public int userId = 0;

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
        mUserViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            observeResponse(response);

        });
        mBinding.navToSignUpButton.setOnClickListener(button -> navigateToRegister());
        mBinding.logInButton.setOnClickListener(button-> signin());

    }

    public void signin() {
        String email = String.valueOf(mBinding.emailEdit.getText());
        String pwd = String.valueOf(mBinding.pwdEdit.getText());
        if(email.isEmpty() || pwd.isEmpty()){
            //throw a toast
            Toast.makeText(this.getContext(), "All fields are required ", Toast.LENGTH_LONG).show();
        }else {
            Log.i(TAG, email);
            mUserViewModel.authenticateUser(email, pwd);
        }
    }

    public int getUserId(){
        return userId;
    }

    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("result")) {
                    String result = response.getString("result");
                    if ("failed to login".equals(result)) {
                        // If the result is "failed to login", display the error message to the user
                        String errorMessage = response.optString("message", "Unknown error");
                        Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    } else if ("success".equals(result)) {

                        // If the result is "success", the login is successful
                        Toast.makeText(getContext(), "Login successful", Toast.LENGTH_LONG).show();

                        // Check if the user ID is present in the response
                        if (response.has("user_id")) {
                            userId = response.getInt("user_id");
//                            // Start the HomeActivity
//                            Intent intent = new Intent(getContext(), HomeActivity.class);
//                            startActivity(intent);
                            Activity activity = getActivity();
                            if (activity instanceof MainActivity) {
                                ((MainActivity) activity).showBottomNavigation();
                                ((MainActivity) activity).setupBottomNavigation();
                            }
                            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
                        }


                    } else {
                        // If the result is neither "failed to login" nor "success", log an error
                        Log.e("Login Response", "Unknown result: " + result);
                    }
                } else {
                    // Log a message if the "result" key is missing from the response
                    Log.d("Login Response", "Missing 'result' key in response");
                }
            } catch (JSONException e) {
                // Log any JSON parsing errors
                Log.e("JSON Parse Error", e.getMessage());
            }
        }else{
            Log.e("Login Response", "email/password required");
        }
    }

}

