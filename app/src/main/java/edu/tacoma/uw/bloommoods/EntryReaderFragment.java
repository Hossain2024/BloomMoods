package edu.tacoma.uw.bloommoods;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.tacoma.uw.bloommoods.databinding.FragmentEntryReaderBinding;

public class EntryReaderFragment extends Fragment {

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView entryTextView;
    private ImageView moodImageView;
    private Intent intent;
    private FragmentEntryReaderBinding entryReaderBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        entryReaderBinding = FragmentEntryReaderBinding.inflate(inflater, container, false);


        // Initialize views
        titleTextView = entryReaderBinding.titleTextView;
        dateTextView = entryReaderBinding.dateTextView;
        entryTextView = entryReaderBinding.entryTextView;
        moodImageView = entryReaderBinding.moodImageView;

        //Get a reference to the SafeArgs object
        EntryReaderFragmentArgs args = EntryReaderFragmentArgs.fromBundle(getArguments());

        //Set the text color of the label. NOTE no need to cast
        titleTextView.setText(args.getTitle());
        dateTextView.setText(args.getDate());
        entryTextView.setText(args.getEntry());
        moodImageView.setImageResource(args.getMood());

        Button backButton = entryReaderBinding.exitReadEntryButton;
        backButton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_entryReaderFragment_to_journalFragment));
        return entryReaderBinding.getRoot();
    }
}