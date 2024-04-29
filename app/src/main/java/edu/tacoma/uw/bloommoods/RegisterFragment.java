package edu.tacoma.uw.bloommoods;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.tacoma.uw.bloommoods.databinding.FragmentRegisterBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding mBinding;



    public void navigateToLogin() {
        Navigation.findNavController(getView())
                .navigate(R.id.action_registerFragment_to_loginFragment23);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Instantiate the Binding object and Inflate the layout for this fragment
        mBinding = FragmentRegisterBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

@Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    mBinding.loginTextview.setOnClickListener(button -> navigateToLogin());


    }

}