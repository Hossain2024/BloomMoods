package edu.tacoma.uw.bloommoods;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentWaterPlantBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaterPlantFragment extends Fragment {

    private static final String ADD_ENTRY_ENDPOINT = "https://students.washington.edu/nchi22/api/log/update_mood_log.php";
    private UserViewModel userViewModel;

    FragmentWaterPlantBinding waterPlantBinding;
    String selectedMood;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        waterPlantBinding = FragmentWaterPlantBinding.inflate(inflater, container, false);
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

        userViewModel = ((MainActivity) requireActivity()).getUserViewModel();

        Button saveButton = waterPlantBinding.saveButton;
        saveButton.setOnClickListener(v -> addEntry());

        LinearLayout moodLayout = waterPlantBinding.linearLayout;

        setOnMoodClicks(moodLayout);

        return waterPlantBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        waterPlantBinding = null;
    }

    private void setOnMoodClicks(LinearLayout moodLayout) {
        for (int i = 0; i < moodLayout.getChildCount(); i++) {
            View childView = moodLayout.getChildAt(i);
            if (childView instanceof ImageView) {
                childView.setOnClickListener(this::onMoodClicked);
            }
        }
    }

    private void addEntry() {
        EditText titleEditText = waterPlantBinding.titleEditText;
        EditText entryEditText = waterPlantBinding.entryEditText;

        String title = titleEditText.getText().toString();
        String entry = entryEditText.getText().toString();

        // Get current user ID
        userViewModel.getUserId().observe(getViewLifecycleOwner(), userId -> {
            if (userId != null) {
                Log.i("Water, User id", String.valueOf(userId));
                try {
                    // Create JSON object with the entry data
                    JSONObject json = new JSONObject();
//                  json.put("title", title);
                    json.put("user_id", userId);
                    json.put("mood", selectedMood);
                    json.put("journal_entry", entry);

                    JsonObjectRequest request = getRequest(json);
                    RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
                    requestQueue.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @NonNull
    private JsonObjectRequest getRequest(JSONObject json) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ADD_ENTRY_ENDPOINT,
                json,
                response -> Toast.makeText(getContext(), "Entry saved successfully", Toast.LENGTH_SHORT).show(),
                Throwable::printStackTrace);

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return request;
    }

    public void onMoodClicked(View view) {
        selectedMood = view.getTag().toString();
        Log.i("Selected Mood", selectedMood);
    }
}