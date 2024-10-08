package edu.tacoma.uw.bloommoods.journal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.tacoma.uw.bloommoods.R;
import edu.tacoma.uw.bloommoods.databinding.FragmentEntryReaderBinding;

/**
 * A Fragment that displays the details of a journal entry.
 * @author Chelsea Dacones
 */
public class EntryReaderFragment extends Fragment {
    private FragmentEntryReaderBinding mEntryReaderBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mEntryReaderBinding = FragmentEntryReaderBinding.inflate(inflater, container, false);

        // Set a click listener on the exit button to navigate back to the journal fragment
        mEntryReaderBinding.exitReadEntryButton.setOnClickListener(button -> Navigation.findNavController(getView())
                .navigate(R.id.action_entryReaderFragment_to_journalFragment));

        return mEntryReaderBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEntryReaderBinding = null;
    }

    /**
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve arguments passed to this fragment and extract the journal entry
        EntryReaderFragmentArgs args = EntryReaderFragmentArgs.fromBundle(getArguments());
        JournalEntry entry = args.getEntry();

        // Populate the UI with the journal entry details
        mEntryReaderBinding.titleTextView.setText(entry.getTitle());
        mEntryReaderBinding.dateTextView.setText(entry.getDate());
        mEntryReaderBinding.entryTextView.setText(entry.getContent());
        mEntryReaderBinding.moodImageView.setImageResource(entry.getMoodImage());
    }
}