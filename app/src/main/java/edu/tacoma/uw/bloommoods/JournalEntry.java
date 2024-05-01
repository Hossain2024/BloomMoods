package edu.tacoma.uw.bloommoods;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalEntry {
    private String title;
    private String date;
    private String content;
    private int moodImage;

    public JournalEntry(String entryTitle, String entryDate, String entryContent, int entryMoodImage) {
        this.title = entryTitle;
        this.date = entryDate;
        this.content = entryContent;
        this.moodImage = entryMoodImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMoodImage() {
        return moodImage;
    }

    public void setMoodImage(int moodImage) {
        this.moodImage = moodImage;
    }

    private Calendar parseDate() {
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
        int month = calendar.get(Calendar.MONTH);
        String[] monthAbbr  = {"Jan", "Feb", "Mar", "Apr","May", "Jun",
                 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthAbbr[month];
    }

    public String getYear() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.YEAR)); // January is 0, so add 1
    }
}
