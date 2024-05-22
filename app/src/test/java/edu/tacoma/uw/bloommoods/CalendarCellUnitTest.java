package edu.tacoma.uw.bloommoods;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class CalendarCellUnitTest {
    private CalendarCell calendarCell;
    private final int testDay = 15;
    private final int testImageResId = 123456;

    @Before
    public void setUp() {
        calendarCell = new CalendarCell(testDay, testImageResId);
    }

    @Test
    public void testGetDay() {
        assertEquals("Day should match the value set in constructor", testDay, calendarCell.getDay());
    }

    @Test
    public void testGetImageResId() {
        assertEquals("Image resource ID should match the value set in constructor", testImageResId, calendarCell.getImageResId());
    }
}
