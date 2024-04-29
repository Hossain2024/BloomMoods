package edu.tacoma.uw.bloommoods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.tacoma.uw.bloommoods.databinding.FragmentLoginBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentRegisterBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {


    private FragmentLoginBinding mBinding;

    public void navigateToRegister() {
        Navigation.findNavController(getView())
                .navigate(R.id.action_loginFragment2_to_registerFragment);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.registerTextview.setOnClickListener(button -> navigateToRegister());

    }
}