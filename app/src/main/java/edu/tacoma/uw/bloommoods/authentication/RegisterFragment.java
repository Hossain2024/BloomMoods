package edu.tacoma.uw.bloommoods.authentication;

import static android.content.ContentValues.TAG;

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

import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.databinding.FragmentRegisterBinding;

/**
 * Fragment for user registration.
 *
 * @author Maliha Hossain
 */
public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding mBinding;
    private RegisterViewModel mRegisterUserViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //chaged to use registerviewmodl
        mRegisterUserViewModel = new ViewModelProvider(getActivity()).get(RegisterViewModel.class);
        // Instantiate the Binding object and Inflate the layout for this fragment
        mBinding = FragmentRegisterBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRegisterUserViewModel.addResponseObserver(getViewLifecycleOwner(), this::observeResponse);


        mBinding.registerButton.setOnClickListener(button -> signup());
        mBinding.navToSignInButton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_registerFragment_to_loginFragment));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    /**
     * Handles user sign-up by validating input and registering the user.
     */
    public void signup() {
        String email = String.valueOf(mBinding.emailEdit.getText());
        String pwd = String.valueOf(mBinding.pwdEdit.getText());
        String name = String.valueOf(mBinding.nameEdit.getText());
        if (email.isEmpty() || pwd.isEmpty() || name.isEmpty()) {
            mBinding.textError.setText("All fields are required");

        } else {
            try {
                Account account = new Account(email, pwd, true);
                Log.i(TAG, email);
                mRegisterUserViewModel.addUser(account, name);
            } catch (IllegalArgumentException ie) {
                Log.e(TAG, ie.getMessage());
                mBinding.textError.setText(ie.getMessage());
            }
        }
    }

    /**
     * Observes the registration response and updates the UI accordingly.
     *
     * @param response The JSON response from the registration request.
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("error_code")) {
                    int errorCode = response.getInt("error_code");
                    if (errorCode == 1062) {
                        String errorMessage = "Email already exists.";
                        mBinding.textError.setText(errorMessage);
                    }
                } else {
                    Toast.makeText(this.getContext(), "User added", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(getView()).popBackStack();

                }
            }catch(JSONException ie){
                    Log.e("JSON Parse Error", ie.getMessage());
                    mBinding.textError.setText(ie.getMessage());
                }
            }
        }


    }



