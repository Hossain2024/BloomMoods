package edu.tacoma.uw.bloommoods;

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

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {
    private final RecyclerViewInterface rvi;
    private final Context context;
    private final ArrayList<JournalEntry> journalEntries;

    public JournalEntryAdapter(Context context, ArrayList<JournalEntry> journalEntries, RecyclerViewInterface rvi) {
        this.context = context;
        this.journalEntries = journalEntries;
        this.rvi = rvi;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dayTextView;
        final TextView monthTextView;
        final TextView yearTextView;
        final TextView titleCardTextView;
        final TextView entryCardTextView;
        final ImageView moodCardImageView;

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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.entry, viewGroup, false);
        return new JournalEntryAdapter.ViewHolder(view, rvi);
    }

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

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }
}
