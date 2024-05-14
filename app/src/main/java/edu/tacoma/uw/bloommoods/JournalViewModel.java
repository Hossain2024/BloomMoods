package edu.tacoma.uw.bloommoods;

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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class JournalViewModel extends AndroidViewModel {
    private final MutableLiveData<JSONObject> mResponse;
    private final MutableLiveData<JournalEntry> mEntry = new MutableLiveData<>();

    public JournalViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
    }

    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

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

    protected void addEntry(int userId, String title, String mood, String entry) {
        String url = "https://students.washington.edu/nchi22/api/log/update_mood_log.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("title", title);
            body.put("mood", mood);
            body.put("journal_entry", entry);
            Log.i("ENTRY BODY", String.valueOf(body));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                mResponse::setValue,
                this::handleError);

        Log.i("JournalViewModel", request.getUrl().toString());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    protected void getTodaysEntry(int userId) {
        String url = "https://students.washington.edu/nchi22/api/log/get_todays_mood_log.php?user_id=" + userId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.i("RESPONSE", String.valueOf(response));
                    if (!response.has("message")) {
                        mEntry.postValue(parseJsonObject(response));
                    }
                },
                this::handleError);
        Log.i("JournalViewModel", request.getUrl().toString());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    public LiveData<JournalEntry> getEntry() {
        return mEntry;
    }

    // Method to parse JSON object and create JournalEntry object
    private JournalEntry parseJsonObject(JSONObject jsonObject) {
        // Parse JSON object and create JournalEntry object
        String timestamp = jsonObject.optString("timestamp");
        String entry = jsonObject.optString("journal_entry");
        String mood = jsonObject.optString("mood");
        String title = jsonObject.optString("title");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date;
        try {
            date = sdf.parse(timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH);
        assert date != null;
        String formattedDate = outputFormat.format(date);

        int moodResourceId = getMipMapForMood(mood);

        return new JournalEntry(title, formattedDate, entry, moodResourceId);
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
}
