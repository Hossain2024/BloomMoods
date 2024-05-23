package edu.tacoma.uw.bloommoods.journal;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Represents a single journal entry with a title, date, entry content, and mood image.
 * Implements Serializable to allow passing between fragments.
 * @author Chelsea Dacones
 */
public class JournalEntry implements Serializable {
    private final String title;
    private final String date;
    private final String content;
    private final int moodImage;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.ENGLISH);

    /**
     * Constructs a new JournalEntry.
     *
     * @param entryTitle     The title of the journal entry.
     * @param entryDate      The date of the journal entry.
     * @param entryContent   The content of the journal entry.
     * @param entryMoodImage The resource ID of the mood image associated with the journal entry.
     */
    public JournalEntry(String entryTitle, String entryDate, String entryContent, int entryMoodImage) {
        this.title = entryTitle;
        this.date = entryDate;
        this.content = entryContent;
        this.moodImage = entryMoodImage;
    }

    /**
     * Gets the title of the journal entry.
     *
     * @return The title of the journal entry.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the date of the journal entry.
     *
     * @return The date of the journal entry.
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the content of the journal entry.
     *
     * @return The content of the journal entry.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the resource ID of the mood image associated with the journal entry.
     *
     * @return The resource ID of the mood image.
     */
    public int getMoodImage() {
        return moodImage;
    }

    /**
     * Gets the day of the month from the journal entry date.
     *
     * @return The day of the month as a String.
     */
    public String getDay() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Gets the abbreviated month (e.g., "Jan", "Feb") from the journal entry date.
     *
     * @return The abbreviated month as a String.
     */
    public String getMonth() {
        Calendar calendar = parseDate();
        return MONTH_FORMAT.format(calendar.getTime());
    }

    /**
     * Gets the year from the journal entry date.
     *
     * @return The year as a String.
     */
    public String getYear() {
        Calendar calendar = parseDate();
        return String.valueOf(calendar.get(Calendar.YEAR)); // January is 0, so add 1
    }

    /**
     * Parses the date string into a Calendar object.
     *
     * @return A Calendar object representing the parsed date.
     * @throws RuntimeException if the date cannot be parsed.
     */
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
