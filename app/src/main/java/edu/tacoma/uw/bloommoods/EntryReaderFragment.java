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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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


        Button backButton = entryReaderBinding.exitReadEntryButton;
        backButton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_entryReaderFragment_to_journalFragment));

        return entryReaderBinding.getRoot();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        entryReaderBinding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EntryReaderFragmentArgs args = EntryReaderFragmentArgs.fromBundle(getArguments());

        JournalEntry entry = (JournalEntry) args.getEntry();

        titleTextView.setText(entry.getTitle());
        dateTextView.setText(entry.getDate());
        entryTextView.setText(entry.getContent());
        moodImageView.setImageResource(entry.getMoodImage());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
        String currentDate = dateFormat.format(new Date());
    }
}