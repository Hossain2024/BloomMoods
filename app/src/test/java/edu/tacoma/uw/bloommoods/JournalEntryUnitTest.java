package edu.tacoma.uw.bloommoods;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import edu.tacoma.uw.bloommoods.journal.JournalEntry;

public class JournalEntryUnitTest {
    private JournalEntry entry;
    private final String title = "Test Entry";
    private final String date = "Monday, 1 January 2021";
    private final String content = "Today was a good day!";
    private final int moodImage = 123;

    @Before
    public void setUp() {
        entry = new JournalEntry(title, date, content, moodImage);
    }

    @Test
    public void testJournalEntryConstructor() {
        assertEquals("Title should match", title, entry.getTitle());
        assertEquals("Date should match", date, entry.getDate());
        assertEquals("Content should match", content, entry.getContent());
        assertEquals("Mood image should match", moodImage, entry.getMoodImage());
    }

    @Test
    public void testGetDay() {
        assertEquals("Day should be '1'", "1", entry.getDay());
    }

    @Test
    public void testGetMonth() {
        assertEquals("Month should be 'Jan'", "Jan", entry.getMonth());
    }

    @Test
    public void testGetYear() {
        assertEquals("Year should be '2021'", "2021", entry.getYear());
    }

    @Test
    public void testDateParsingValidDate() {
        Calendar cal = entry.parseDate();
        assertEquals("Day of month should be 1", 1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals("Month should be January", Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals("Year should be 2021", 2021, cal.get(Calendar.YEAR));
    }

    @Test(expected = RuntimeException.class)
    public void testDateParsingInvalidDate() {
        new JournalEntry(title, "invalid date", content, moodImage).parseDate();
    }
}
