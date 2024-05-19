package edu.tacoma.uw.bloommoods;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.ENGLISH);

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

    public String getDay() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    public String getMonth() {
        Calendar calendar = parseDate();
        return MONTH_FORMAT.format(calendar.getTime()); // Return abbreviated month ("Jan", "Feb", ...)
    }

    public String getYear() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.YEAR)); // January is 0, so add 1
    }

    public Calendar parseDate() {
         try {
            Date parsedDate = DATE_FORMAT.parse(date);
            Calendar calendar = Calendar.getInstance();
             assert parsedDate != null;
             calendar.setTime(parsedDate);
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
