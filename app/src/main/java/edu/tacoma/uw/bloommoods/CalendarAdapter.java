package edu.tacoma.uw.bloommoods;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CalendarAdapter extends BaseAdapter {

    private Context mContext;
    private List<CalendarCell> mCalendarCells;

    public CalendarAdapter(Context context, List<CalendarCell> calendarCells) {
        mContext = context;
        mCalendarCells = calendarCells;
    }

    @Override
    public int getCount() {
        return mCalendarCells.size();
    }

    @Override
    public Object getItem(int position) {
        return mCalendarCells.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.calendar_cell, null);
        }

        ImageView imageView = view.findViewById(R.id.plantImageView);
        TextView dayTextView = view.findViewById(R.id.dayTextView);

        CalendarCell cell = mCalendarCells.get(position);
        imageView.setImageResource(cell.getImageResId());
        dayTextView.setText(String.valueOf(cell.getDay()));

        return view;
    }
}
