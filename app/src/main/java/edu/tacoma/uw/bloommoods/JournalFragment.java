package edu.tacoma.uw.bloommoods;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.tacoma.uw.bloommoods.databinding.FragmentJournalBinding;

public class JournalFragment extends Fragment implements RecyclerViewInterface {
    private static final String GET_MONTH_ENTRIES_ENDPOINT = "https://students.washington.edu/nchi22/api/log/get_mood_logs_by_month.php";
    private final ArrayList<JournalEntry> journalEntries = new ArrayList<>();
    private final Calendar calender = Calendar.getInstance();
    private final int currentYear = calender.get(Calendar.YEAR);
    private final int currentMonth = calender.get(Calendar.MONTH) + 1; // Jan starts at 0
    private final ArrayList<JournalEntry> monthJournalEntries = new ArrayList<>();
    private MonthYearPicker myp;
    private FragmentJournalBinding journalBinding;
    private UserViewModel mUserViewModel;
    private RecyclerView recyclerView;
    private Integer userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        journalBinding = FragmentJournalBinding.inflate(inflater, container, false);
        mUserViewModel = ((MainActivity) requireActivity()).getUserViewModel();
        mUserViewModel.getUserId().observe(getViewLifecycleOwner(), currentUser -> {
            if (currentUser != null) {
               userId = currentUser;
               Log.i("Current user:", String.valueOf(userId));
               getEntries(userId, currentMonth);
            }
        });
        myp = new MonthYearPicker(getActivity(), journalBinding.monthYearTextView);
        openDateDialog();

        recyclerView = journalBinding.entriesRecyclerView;
//        monthJournalEntries = getCurrentDatesEntries(currentYear, currentMonth);
//
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        journalBinding.allEntriesButton.setOnClickListener(button -> updateRecyclerView(journalEntries));

        return journalBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        journalBinding = null;
    }


    private void getEntries(int userId, int month) {
        journalEntries.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_MONTH_ENTRIES_ENDPOINT,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        Log.i("JOURNAL RESPONSE", response);
                        Log.i("JOURNAL ARRAY", jsonArray.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JournalEntry entry = parseJsonObject(jsonObject);
                            journalEntries.add(entry);
                        }
                        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), journalEntries, JournalFragment.this);
                        recyclerView.setAdapter(adapter);
                    } catch (JSONException | ParseException e) {
                        // Handle JSON exception
                    }
                },
                error -> {
                    // Handle error
                    Log.i("JOURNAL ERROR", String.valueOf(error));
                }) {

            @Override
            public byte[] getBody() {
                // Create the request body JSON object
                JSONObject requestBodyJson = new JSONObject();
                try {
                    requestBodyJson.put("user_id", userId);
                    requestBodyJson.put("year", 2024);
                    requestBodyJson.put("month", month);
                } catch (JSONException e) {
                    // Handle JSON exception
                }
                // Convert the JSON object to byte array
                return requestBodyJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);

        // Sort entries by most to least recent
        journalEntries.sort((o1, o2) -> (o2.parseDate()).compareTo(o1.parseDate()));
    }



//    private ArrayList<JournalEntry> getCurrentDatesEntries(int year, int month) {
//        ArrayList<JournalEntry> entries = new ArrayList<>();
//        Log.i("Getting Month", String.valueOf(month));
//        Log.i("Getting Year", String.valueOf(year));
//
//        // Create JSON object with the entry data
//        JSONObject json = new JSONObject();
//
//        // Get current user ID
//        LiveData<Integer> userIdLiveData = mUserViewModel.getUserId();
//        Integer userId = userIdLiveData.getValue();
//        Log.i("User id", String.valueOf(userId));
//
//        try {
//            json.put("user_id", 77);
//            json.put("year", 2024);
//            json.put("month", 5);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            // Handle JSON exception (e.g., log error, show error message)
//            return entries; // Return empty list in case of error
//        }
//
//        Log.i("Request Parameters", json.toString()); // Log request parameters
//
//        JsonObjectRequest request = getJournalEntries(json);
//        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
//
//        // Log before adding request to queue
//        Log.i("Adding Request to Queue", "Request: " + request.toString());
//
//        requestQueue.add(new JsonArrayRequest(
//                request.getMethod(),
//                request.getUrl(),
//                null,
//                response -> {
//                    try {
//                        // Log response received from the server
//                        Log.i("Response", response.toString());
//
//                        for (int i = 0; i < response.length(); i++) {
//                            JSONObject jsonObject = response.getJSONObject(i);
//                            // Parse JSON object and create JournalEntry object
//                            JournalEntry entry = parseJsonObject(jsonObject);
//                            entries.add(entry);
//                        }
//                    } catch (JSONException | ParseException e) {
//                        e.printStackTrace();
//                        // Handle parsing exception (e.g., log error, show error message)
//                    }
//                },
//                error -> {
//                    error.printStackTrace();
//                    // Handle Volley error (e.g., log error, show error message)
//                }));
//
//        entries.sort(new Comparator<JournalEntry>() { // Sort entries by most to least recent
//            @Override
//            public int compare(JournalEntry o1, JournalEntry o2) {
//                return (o2.parseDate()).compareTo(o1.parseDate());
//            }
//        });
//        return entries;
//    }

    // Method to parse JSON object and create JournalEntry object
    private JournalEntry parseJsonObject(JSONObject jsonObject) throws JSONException, ParseException {
        // Parse JSON object and create JournalEntry object
        String timestamp = jsonObject.getString("timestamp");
        String entry = jsonObject.getString("journal_entry");
        String mood = jsonObject.getString("mood");
        Log.i("PARSING", timestamp);
        Log.i("PARSING", entry);
        Log.i("PARSING", mood);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date = sdf.parse(timestamp);

        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
        String formattedDate = outputFormat.format(date);

        int moodResourceId = getMipMapForMood(mood);

        return new JournalEntry("", formattedDate, entry, moodResourceId);
    }

    private int getMipMapForMood(String mood) {
        switch (mood) {
            case "Excited":
                return R.mipmap.excited;
            case "Happy":
                return R.mipmap.happy;
            case "Neutral":
                return R.mipmap.neutral;
            case "Sad":
                return R.mipmap.sad;
            case "Anxious":
                return R.mipmap.anxious;
            case "Angry":
                return R.mipmap.angry;
            default:
                return R.mipmap.neutral;
        }
    }

    private void openDateDialog() {
        // Retrieves and sets current date
        // TO-DO: Create date dialog to select year
//        Calendar calender = Calendar.getInstance();
        String month = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calender.getTime());
        int year = calender.get(Calendar.YEAR);
        TextView monthYear = journalBinding.monthYearTextView;
        monthYear.setText(String.format(Locale.ENGLISH, "%s %d    ⓥ", month, year));
        monthYear.setOnClickListener(v -> {
            myp.showAsDropDown(monthYear);
        });

        monthYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When the text (month and year) changes, fetch the corresponding journal entries
                String selectedMonthYear = journalBinding.monthYearTextView.getText().toString();
                String[] parts = selectedMonthYear.split(" ");
                String selectedMonth = parts[0];
                int monthInt = getMonthInt(selectedMonth);
                getEntries(userId, monthInt);
//                        // Update the RecyclerView with the new entries
//                        updateRecyclerView(monthJournalEntries);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private int getMonthInt(String month) {
        String[] months = calender.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).split(",");
        for (int i = 0; i < months.length; i++) {
            if (months[i].trim().equalsIgnoreCase(month)) {
                return i + 1;
            }
        }
        return -1; // invalid month
    }

//    private JsonObjectRequest getJournalEntries(JSONObject json) {
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.POST,
//                GET_MONTH_ENTRIES_ENDPOINT,
//                json,
//                response -> {
//                    // Handle successful response (if needed)
//                    Log.i("Response", response.toString());
//                },
//                error -> {
//                    // Handle Volley error
//                    if (error.networkResponse != null) {
//                        Log.e("Volley Error", "HTTP Status Code: " + error.networkResponse.statusCode);
//                        if (error.networkResponse.data != null) {
//                            String errorResponse = new String(error.networkResponse.data);
//                            Log.e("Volley Error Response", errorResponse);
//                        }
//                    } else {
//                        Log.e("Volley Error", "Unknown error occurred");
//                    }
//                    error.printStackTrace();
//                }
//        );
//
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                10_000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        return request;
//    }

    private void updateRecyclerView(ArrayList<JournalEntry> entries) {
        JournalEntryAdapter adapter = new JournalEntryAdapter(getActivity(), entries, this);
        journalBinding.entriesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position) {
        JournalEntry selectedEntry = journalEntries.get(position);
        JournalFragmentDirections.ActionJournalFragmentToEntryReaderFragment directions =
                JournalFragmentDirections.actionJournalFragmentToEntryReaderFragment(selectedEntry);
        Navigation.findNavController(getView())
                .navigate(directions);
    }
}