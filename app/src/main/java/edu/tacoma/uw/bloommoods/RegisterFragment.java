package edu.tacoma.uw.bloommoods;

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

import edu.tacoma.uw.bloommoods.databinding.FragmentLoginBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentRegisterBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding mBinding;
    private UserViewModel mUserViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mUserViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        // Instantiate the Binding object and Inflate the layout for this fragment
        mBinding = FragmentRegisterBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }
    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserViewModel.addResponseObserver(getViewLifecycleOwner(), response -> {
            observeResponse(response);

        });


        mBinding.registerButton.setOnClickListener(button -> signup());
        mBinding.navToSignInButton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_registerFragment_to_loginFragment));

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public void signup() {
        String email = String.valueOf(mBinding.emailEdit.getText());
        String pwd = String.valueOf(mBinding.pwdEdit.getText());
        String name = String.valueOf(mBinding.nameEdit.getText());
        if(email.isEmpty() || pwd.isEmpty() || name.isEmpty()){
            //throw a toast
            Toast.makeText(this.getContext(), "All fields are required ", Toast.LENGTH_LONG).show();
        }else {
            Log.i(TAG, email);
            mUserViewModel.addUser(email, pwd, name);
        }
    }

    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            try {
                if (response.has("result")) {
                    String result = response.getString("result");
                    if ("failed".equals(result)) {
                        String errorMessage = response.getString("message");
                        Toast.makeText(this.getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this.getContext(), "User added", Toast.LENGTH_LONG).show();
                        Navigation.findNavController(getView()).popBackStack();
                    }
                } else {
                    Log.d("JSON Response", "Missing 'result' key in response");
                }
            } catch (JSONException e) {
                Log.e("JSON Parse Error", e.getMessage());
            }
        }
    }
}


