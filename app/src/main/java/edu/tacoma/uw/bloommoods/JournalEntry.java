package edu.tacoma.uw.bloommoods;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalEntry implements Serializable {
    private final String title;
    private final String date;
    private final String content;
    private final int moodImage;

    public JournalEntry(String entryTitle, String entryDate, String entryContent, int entryMoodImage) {
        this.title = entryTitle;
        this.date = entryDate;
        this.content = entryContent;
        this.moodImage = entryMoodImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public int getMoodImage() {
        return moodImage;
    }

    public Calendar parseDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
            Date parsedDate = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDay() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getMonth() {
        Calendar calendar = parseDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM", Locale.ENGLISH);
        return dateFormat.format(calendar.getTime());
    }

    public String getYear() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.YEAR)); // January is 0, so add 1
    }
}
