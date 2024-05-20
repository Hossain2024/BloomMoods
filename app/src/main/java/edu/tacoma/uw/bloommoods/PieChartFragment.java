package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import edu.tacoma.uw.bloommoods.databinding.FragmentPieChartBinding;
import edu.tacoma.uw.bloommoods.databinding.FragmentReportBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class PieChartFragment extends Fragment {

    //TextView tvR, tvPython, tvCPP, tvJava;
    ImageView excitedImageView, anxiousImageView, angryImageView,  neutralImageView, sadImageView;

    PieChart pieChart;
    private FragmentPieChartBinding mpiechartBinding;

    private UserViewModel mUserViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        mpiechartBinding =   FragmentPieChartBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        // Initialize the ImageViews
        excitedImageView = mpiechartBinding.excitedImageView;
        anxiousImageView = mpiechartBinding.anxiousImageView;
        angryImageView = mpiechartBinding.angryImageView;
        neutralImageView = mpiechartBinding.neutralImageView;
        sadImageView = mpiechartBinding.sadImageView;

        // Initialize the PieChart
        pieChart = mpiechartBinding.piechart;

        // Set the data for the PieChart
        setData();



        // Set OnClickListener for the button
        mpiechartBinding.calenderbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDirections action = PieChartFragmentDirections.actionPieChartFragmentToNavReport();
                Navigation.findNavController(v).navigate(action);
            }
        });
        return mpiechartBinding.getRoot();
    }

    private void setData() {
        // Dummy data for the moods (replace with your actual data)
        int happyCount = 20;
        int anxiousCount = 15;
        int angryCount = 10;
        int neutralCount = 30;
        int sadCount = 25;

        // Add data to the PieChart
        pieChart.addPieSlice(new PieModel("Happy", happyCount, Color.parseColor("#FFA726")));
        pieChart.addPieSlice(new PieModel("Anxious", anxiousCount, Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(new PieModel("Angry", angryCount, Color.parseColor("#EF5350")));
        pieChart.addPieSlice(new PieModel("Neutral", neutralCount, Color.parseColor("#29B6F6")));
        pieChart.addPieSlice(new PieModel("Sad", sadCount, Color.parseColor("#AB47BC")));

        // Start the animation
        pieChart.startAnimation();
    }

}
