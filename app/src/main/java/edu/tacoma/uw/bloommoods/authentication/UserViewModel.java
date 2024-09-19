package edu.tacoma.uw.bloommoods.authentication;

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
import java.util.Objects;

/**
 * ViewModel for handling user related data and operations.
 *
 * @author Maliha Hossain
 * @author Amanda Nguyen
 */
public class UserViewModel extends AndroidViewModel {
    final private MutableLiveData<JSONObject> mResponse;
    final private MutableLiveData<Integer> mUserId;
    final private MutableLiveData<Boolean> resetted;
    final private MutableLiveData<String> lastEntryLogged;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mResponse = new MutableLiveData<>();
        mUserId = new MutableLiveData<>();
        resetted = new MutableLiveData<>();
        lastEntryLogged = new MutableLiveData<>();
        mResponse.setValue(new JSONObject());
        mUserId.setValue(0);
        resetted.setValue(false);
        lastEntryLogged.setValue("");
    }


    public void addResponseObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super JSONObject> observer) {
        mResponse.observe(owner, observer);
    }

    /**
     * Handles errors encountered during a request.
     *
     * @param error The VolleyError encountered during the request.
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
     * Authenticates a user by making a POST request to the server.
     *
     * @param account The account object containing the user's email and password.
     */
    public void authenticateUser(Account account) {
        String url = "https://students.washington.edu/nchi22/api/users/login.php";
        JSONObject body = new JSONObject();
        try {
            body.put("email", account.getEmail());
            body.put("password", account.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body, //no body for this get request
                mResponse::setValue,
                this::handleError);

        Log.i("UserViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }


    /**
     * Fetches the user profile by making a GET request to the server.
     *
     * @param userId The user ID for which to fetch the profile.
     */
    public void getUserProfile(int userId) {
        String url = "https://students.washington.edu/nchi22/api/users/get_profile.php?user_id=" + userId;

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                mResponse::setValue,
                this::handleError);

        Log.i("UserViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }


    /**
     * Resets the user's streak by making a PUT request to the server.
     *
     * @param userId The user ID for which to reset the streak.
     */
    public void resetStreak(int userId) {
        String url = "https://students.washington.edu/nchi22/api/users/reset_streak.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response ->
                    Log.i("Streak", "Streak has been reset"),
                this::handleError);

        Log.i("UserViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Gets the LiveData containing the user ID.
     *
     * @return The LiveData containing the user ID.
     */
    public LiveData<Integer> getUserId() {
        return mUserId;
    }

    /**
     * Sets the user ID.
     *
     * @param id The user ID to set.
     */
    public void setUserId(int id) {
        mUserId.setValue(id);
    }

    /**
     * Gets the LiveData indicating if the streak has been reset.
     *
     * @return The LiveData indicating if the streak has been reset.
     */
    public LiveData<Boolean> getReset() {
        return resetted;
    }

    /**
     * Sets the streak reset status.
     *
     * @param reset The reset status to set.
     */
    public void setReset(boolean reset) {
        resetted.setValue(reset);
    }

    /**
     * Gets the LiveData containing the last entry logged.
     *
     * @return The LiveData containing the last entry logged.
     */
    public MutableLiveData<String> getLastEntryLogged() {
        return lastEntryLogged;
    }

    /**
     * Sets the last entry logged.
     *
     * @param entry The last entry to set.
     */
    public void setLastEntryLogged(String entry) {
        lastEntryLogged.setValue(entry);
    }


}
