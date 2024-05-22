package edu.tacoma.uw.bloommoods;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<Date> mDates;
    private final List<JournalEntry> mJournalEntries;
    private final SimpleDateFormat journalDateFormat;
    private final SimpleDateFormat dateFormat;
    private static final Map<String, Integer> moodMap = new HashMap<>();
    private static final int DEFAULT_IMAGE = R.mipmap.calendar_cell;


    static {
        moodMap.put("Excited", R.mipmap.excited);
        moodMap.put("Happy", R.mipmap.happy);
        moodMap.put("Neutral", R.mipmap.neutral);
        moodMap.put("Sad", R.mipmap.sad);
        moodMap.put("Anxious", R.mipmap.anxious);
        moodMap.put("Angry", R.mipmap.angry);
    }

    public CalendarAdapter(Context context, List<Date> dates, List<JournalEntry> journalEntries) {
        this.mContext = context;
        this.mJournalEntries = journalEntries;
        this.mDates = dates;
        this.journalDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @Override
    public int getCount() {
        return mDates.size();
    }

    @Override
    public Object getItem(int position) {
        return mDates.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calendar_cell, null);
        }

        ImageView imageView = convertView.findViewById(R.id.calendarMoodImageView);
        TextView dayTextView = convertView.findViewById(R.id.dayTextView);

        Date date = mDates.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Set day number
        dayTextView.setText(String.valueOf(day));

        // Set default image
        imageView.setImageResource(DEFAULT_IMAGE);

        // Set image based on mood if a journal entry exists for date
        journalDateFormat.format(date);
        for (JournalEntry entry : mJournalEntries) {
            try {
                Date entryDate = journalDateFormat.parse(entry.getDate());
                if (entryDate != null && dateFormat.format(date).equals(dateFormat.format(entryDate))) {
                    Integer imageResId = entry.getMoodImage();
                    imageView.setImageResource(imageResId);
                    break;
                }
            } catch (ParseException e) {
                Log.e("CalendarAdapter", "Date parsing error", e);
            }
        }

        return convertView;
    }
}
