package edu.tacoma.uw.bloommoods;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {

    Context context;
    ArrayList<JournalEntry> journalEntries;

    public JournalEntryAdapter(Context context, ArrayList<JournalEntry> journalEntries) {
        this.context = context;
        this.journalEntries = journalEntries;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView, monthTextView, yearTextView, titleCardTextView, entryCardTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View

            dayTextView = itemView.findViewById(R.id.dayTextView);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            yearTextView = itemView.findViewById(R.id.yearTextView);
            titleCardTextView = itemView.findViewById(R.id.titleCardTextView);
            entryCardTextView = itemView.findViewById(R.id.entryCardTextView);
        }
    }


    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.entry, viewGroup, false);
        return new JournalEntryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryAdapter.ViewHolder viewHolder, final int position) {
        Log.i(TAG, journalEntries.get(position).getMonth());
        viewHolder.dayTextView.setText(journalEntries.get(position).getDay());
        viewHolder.monthTextView.setText(journalEntries.get(position).getMonth());
        viewHolder.yearTextView.setText(journalEntries.get(position).getYear());
        viewHolder.titleCardTextView.setText(journalEntries.get(position).getTitle());
        viewHolder.entryCardTextView.setText(journalEntries.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }
}
