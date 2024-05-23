package edu.tacoma.uw.bloommoods.journal;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.tacoma.uw.bloommoods.R;

/**
 * A RecyclerView Adapter that binds a list of JournalEntry objects
 * to views displayed within a RecyclerView.
 * @author Chelsea Dacones
 */
public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {
    private final RecyclerViewInterface rvi;
    private final Context context;
    private final ArrayList<JournalEntry> journalEntries;

    /**
     * Constructs a new JournalEntryAdapter.
     *
     * @param context       The context in which the adapter is running.
     * @param journalEntries The list of journal entries to display.
     * @param rvi           The interface for handling item clicks.
     */
    public JournalEntryAdapter(Context context, ArrayList<JournalEntry> journalEntries, RecyclerViewInterface rvi) {
        this.context = context;
        this.journalEntries = journalEntries;
        this.rvi = rvi;
    }

    /**
     * Describes an item view within the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dayTextView;
        final TextView monthTextView;
        final TextView yearTextView;
        final TextView titleCardTextView;
        final TextView entryCardTextView;
        final ImageView moodCardImageView;

        /**
         * Constructs a new ViewHolder.
         *
         * @param itemView The view to hold.
         * @param rvi      The interface for handling item clicks.
         */
        public ViewHolder(View itemView, RecyclerViewInterface rvi) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            yearTextView = itemView.findViewById(R.id.yearTextView);
            titleCardTextView = itemView.findViewById(R.id.titleCardTextView);
            entryCardTextView = itemView.findViewById(R.id.entryCardTextView);
            moodCardImageView = itemView.findViewById(R.id.moodCardImageView);

            itemView.setOnClickListener(v -> {
                if (rvi != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        rvi.onItemClick(position);
                    }
                }
            });
        }
    }

    /**
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.entry, viewGroup, false);
        return new JournalEntryAdapter.ViewHolder(view, rvi);
    }

    /**
     * @param viewHolder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull JournalEntryAdapter.ViewHolder viewHolder, final int position) {
        Log.i(TAG, journalEntries.get(position).getMonth());
        viewHolder.dayTextView.setText(journalEntries.get(position).getDay());
        viewHolder.monthTextView.setText(journalEntries.get(position).getMonth());
        viewHolder.yearTextView.setText(journalEntries.get(position).getYear());
        viewHolder.titleCardTextView.setText(journalEntries.get(position).getTitle());
        viewHolder.entryCardTextView.setText(journalEntries.get(position).getContent());
        viewHolder.moodCardImageView.setImageResource(journalEntries.get(position).getMoodImage());
    }

    /**
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return journalEntries.size();
    }
}
