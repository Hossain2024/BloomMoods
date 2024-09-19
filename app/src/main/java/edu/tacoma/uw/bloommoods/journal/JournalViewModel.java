package edu.tacoma.uw.bloommoods.journal;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.tacoma.uw.bloommoods.R;

/**
 * ViewModel for handling journal-related data and operations.
 * @author Chelsea Dacones
 */
public class JournalViewModel extends AndroidViewModel {
    private final MutableLiveData<JSONObject> mResponse;
    private final MutableLiveData<JournalEntry> mEntry;
    private final MutableLiveData<String> mDateEntries;
    private final MutableLiveData<Boolean> mRequestCompleted;

    /**
     * Constructs a new JournalViewModel.
     *
     * @param application the application context.
     */
    public JournalViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mEntry = new MutableLiveData<>();
        mDateEntries = new MutableLiveData<>();
        mRequestCompleted = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
    }

    /**
     * Adds an observer for adding/updating journal.
     *
     * @param owner the lifecycle owner.
     * @param observer the observer for the response.
     */
    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Handles errors from Volley request.
     *
     * @param error the VolleyError received.
     */
    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            try {
                mResponse.setValue(new JSONObject("{" +
                        "error:\"" + error.getMessage() +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        } else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset())
                    .replace('\"', '\'');
            try {
                mResponse.setValue(new JSONObject("{" +
                        "code:" + error.networkResponse.statusCode +
                        ", data:\"" + data +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }

    /**
     * Adds a journal entry for the current user.
     *
     * @param userId the current user ID.
     * @param title the entry title.
     * @param mood the entry mood.
     * @param entry the content of the entry.
     */
    public void addEntry(int userId, String title, String mood, String entry) {
        String url = "https://students.washington.edu/nchi22/api/log/update_mood_log.php";
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
        JSONObject body = new JSONObject();

        try {
            body.put("user_id", userId);
            body.put("title", title);
            body.put("mood", mood);
            body.put("journal_entry", entry);
        } catch (JSONException e) {
            Log.e("JournalViewModel", "JSON Error in addEntry", e);
        }

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.POST, url, body,
            response -> {
                // On successful response, update the LiveData and create a new JournalEntry object
                mResponse.setValue(response);
                JournalEntry newEntry = new JournalEntry(title, currentDate, entry, getMipMapForMood(mood));
                mEntry.postValue(newEntry);
            },
            this::handleError);

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    /**
     * Retrieve's today's journal entry for the current user.
     *
     * @param userId the current user ID.
     */
    public void getTodaysEntry(int userId) {
        mRequestCompleted.setValue(false); // Indicate that the request is not completed yet
        JsonObjectRequest request = getTodaysEntryRequest(userId); // Create the request
        Log.i("JournalViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext()).add(request);
    }

    /**
     * Creates a request to get today's journal entry for the current user.
     *
     * @param userId the current user ID.
     * @return the JSON object request.
     */
    @NonNull
    private JsonObjectRequest getTodaysEntryRequest(int userId) {
        String url = "https://students.washington.edu/nchi22/api/log/get_todays_mood_log.php?user_id=" + userId;
        return new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                // If the response has a "message" field, set the entry to null (i.e. no entry for today)
                if (response.has("message")) {
                    mEntry.postValue(null);
                } else {
                    // Otherwise, parse the response and set it as the entry
                    mEntry.postValue(parseJsonObject(response));
                }
                mRequestCompleted.postValue(true); // Indicate that the request is completed
            },
            error -> {
                handleError(error);
                mRequestCompleted.postValue(true); // Indicate that the request is completed
            });
    }

    /**
     * Retrieves journal entries by date for the current user.
     *
     * @param userId the current user ID.
     * @param month the month.
     * @param year the year.
     */
    public void getEntriesByDate(int userId, int month, int year) {
        mRequestCompleted.setValue(false);  // Indicate that the request is not completed yet
        Log.i("JournalViewModel getEntriesByDate", String.valueOf(mRequestCompleted.getValue()));
        String url = "https://students.washington.edu/nchi22/api/log/get_mood_logs_by_month.php";
        Log.i("JournalViewModel", "getEntriesByDate " + userId + ", " + month + ", " + year);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            response -> {
                // On successful response, update the LiveData and indicate request completion
                mDateEntries.postValue(response);
                mRequestCompleted.postValue(true);
                Log.i("JournalViewModel getEntriesByDate", String.valueOf(mRequestCompleted.getValue()));
            },
            error -> {
                // If a 404 error occurs, indicate no entries were found
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    mDateEntries.postValue("No entries found");
                    mRequestCompleted.postValue(true);
                }
            }) {

            @Override
            public byte[] getBody() {
                // Create the request body JSON object
                JSONObject requestBodyJson = new JSONObject();
                try {
                    requestBodyJson.put("user_id", userId);
                    requestBodyJson.put("year", year);
                    requestBodyJson.put("month", month);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return requestBodyJson.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        Volley.newRequestQueue(getApplication().getApplicationContext()).add(stringRequest);
    }

    /**
     * Returns LiveData for today's journal entry.
     *
     * @return LiveData for today's journal entry.
     */
    public LiveData<JournalEntry> getEntry() {
        return mEntry;
    }

    /**
     * Returns LiveData for the given date's (month, year) entries.
     *
     * @return LiveData for the entries of a given date.
     */
    public LiveData<String> getDateEntries() {
        return mDateEntries;
    }

    /**
     * Returns LiveData indicating completion status of a request.
     *
     * @return LiveData indicating request completion.
     */
    public LiveData<Boolean> getRequestCompleted() {
        return mRequestCompleted;
    }

    /**
     * Parses a JSON object to create a JournalEntry object.
     *
     * @param jsonObject the JSON object to parse.
     * @return a JournalEntry object.
     */
    public JournalEntry parseJsonObject(JSONObject jsonObject) {
        // Parse JSON object and create JournalEntry object
        String timestamp = jsonObject.optString("timestamp");
        String entry = jsonObject.optString("journal_entry");
        String mood = jsonObject.optString("mood");
        String title = jsonObject.optString("title");

        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        String formattedDate = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH).format(date);
        int moodResourceId = getMipMapForMood(mood);

        return new JournalEntry(title, formattedDate, entry, moodResourceId);
    }

    /**
     * Returns the mipmap resource ID for the given mood.
     *
     * @param mood the mood string.
     * @return the mipmap resource ID.
     */
    private int getMipMapForMood(String mood) {
        switch (mood) {
            case "Excited": return R.mipmap.excited;
            case "Happy": return R.mipmap.happy;
            case "Neutral": return R.mipmap.neutral;
            case "Sad": return R.mipmap.sad;
            case "Anxious": return R.mipmap.anxious;
            case "Angry": return R.mipmap.angry;
            default: return R.mipmap.neutral;
        }
    }
}
