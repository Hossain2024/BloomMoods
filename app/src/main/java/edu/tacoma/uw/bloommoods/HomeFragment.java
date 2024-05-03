package edu.tacoma.uw.bloommoods;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.tacoma.uw.bloommoods.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private int plantGrowth = 0;

    private TextView editText;  // Declare editText here
    private TextView entriesText;
    private FragmentHomeBinding homeBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        setEditText();
        homeBinding.navJournalBtn.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_homeFragment_to_journalFragment));
        homeBinding.navAboutBtn.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_homeFragment_to_aboutFragment));
        return homeBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        homeBinding = null;
    }

    private void setEditText() {
        int days = 15;
        // Initialize EditText after setContentView
        editText = homeBinding.textStreak;

        String text = "Streak\n " + days + "  days";
        SpannableString spannableString = new SpannableString(text);


        // Apply a size span to "Big Text"
        RelativeSizeSpan daysSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableString.setSpan(daysSpan, text.indexOf(String.valueOf(days)), text.indexOf(String.valueOf(days)) + String.valueOf(days).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan smallTextSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableString.setSpan(smallTextSpan, text.indexOf("days"), text.indexOf("days") + "days".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        editText.setText(spannableString);

        int entries = 40;
        entriesText = homeBinding.textEntries;
        String totalentries = "Total Entries\n " + entries + "  entries";
        SpannableString spannableStringEntries = new SpannableString(totalentries);

        // Apply a size span to "Big Text"
        RelativeSizeSpan entriesSpan = new RelativeSizeSpan(2.8f); // 150% larger size
        spannableStringEntries.setSpan(entriesSpan, totalentries.indexOf(String.valueOf(entries)), totalentries.indexOf(String.valueOf(entries)) + String.valueOf(entries).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply a size span to "Small Text"
        RelativeSizeSpan entSpan = new RelativeSizeSpan(0.75f); // 75% smaller size
        spannableStringEntries.setSpan(entSpan, totalentries.indexOf("entries"), totalentries.indexOf("entries") + "entries".length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        entriesText.setText(spannableStringEntries);
    }
}
