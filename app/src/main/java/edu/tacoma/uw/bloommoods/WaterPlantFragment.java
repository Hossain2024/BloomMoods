package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding waterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
        TextView date = waterPlantBinding.dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        date.setText(currentDate);

        TextView plantGrowth = waterPlantBinding.plantGrowth;
        float[] radii = {50, 50, 50, 50, 50, 50, 50, 50};
        RoundRectShape roundRectShape = new RoundRectShape(radii, null,null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        int color = Color.parseColor("#4DFFD6C7");
        shapeDrawable.getPaint().setColor(color);
        plantGrowth.setBackground(shapeDrawable);

        return waterPlantBinding.getRoot();
    }
}