package edu.tacoma.uw.bloommoods;

public class CalendarCell {
    private int day;
    private int imageResId;

    public CalendarCell(int day, int imageResId) {
        this.day = day;
        this.imageResId = imageResId;
    }

    public int getDay() {
        return day;
    }

    public int getImageResId() {
        return imageResId;
    }
}