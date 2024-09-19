package edu.tacoma.uw.bloommoods.waterplant;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * ViewModel for handling plant growth mechanics and operations.
 *
 * @author Amanda Nguyen
 */
public class PlantViewModel extends AndroidViewModel {
    final private MutableLiveData<JSONObject> plantResponse;
    final private MutableLiveData<JSONObject> updatePlantResponse;
    final private MutableLiveData<JSONObject> resetPlantResponse;
    final private MutableLiveData<JSONObject> updatePlantDetailsResponse;
    final private MutableLiveData<JSONArray> unlockedPlantResponse;
    private Observer<? super JSONObject> currentObserver;

    public PlantViewModel(@NonNull Application application) {
        super(application);
        plantResponse = new MutableLiveData<>();
        resetPlantResponse = new MutableLiveData<>();
        unlockedPlantResponse = new MutableLiveData<>();
        updatePlantResponse = new MutableLiveData<>();
        updatePlantResponse.setValue(new JSONObject());
        updatePlantDetailsResponse = new MutableLiveData<>();
        updatePlantDetailsResponse.setValue(new JSONObject());
        resetPlantResponse.setValue(new JSONObject());
        plantResponse.setValue(new JSONObject());
        unlockedPlantResponse.setValue(new JSONArray());
    }

    /**
     * Adds an observer to the plant response LiveData.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the response updates.
     */
    public void addPlantResponseObserver(@NonNull LifecycleOwner owner,
                                         @NonNull Observer<? super JSONObject> observer) {
        plantResponse.observe(owner, observer);
    }
    /**
     * Adds an observer to the reset plant response LiveData.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the response updates.
     */
    public void addResetPlantResponseObserver(@NonNull LifecycleOwner owner,
                                         @NonNull Observer<? super JSONObject> observer) {
        resetPlantResponse.observe(owner, observer);
    }

    /**
     * Adds an observer to the unlocked plant response LiveData.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the response updates.
     */
    public void addUnlockedPlantResponseObserver(@NonNull LifecycleOwner owner,
                                                 @NonNull Observer<? super JSONArray> observer) {
        unlockedPlantResponse.observe(owner, observer);
    }

    /**
     * Adds an observer to the plant details response LiveData.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the response updates.
     */
    public void addPlantDetailResponseObserver(@NonNull LifecycleOwner owner,
                                                 @NonNull Observer<? super JSONObject> observer) {
        updatePlantDetailsResponse.observe(owner, observer);
    }

    /**
     * Adds an observer to the updated plant response LiveData.
     *
     * @param owner    The LifecycleOwner which controls the observer.
     * @param observer The observer that will receive the response updates.
     */
    public void addUpdatedPlantResponseObserver(@NonNull LifecycleOwner owner,
                                                @NonNull Observer<? super JSONObject> observer) {
        // Remove the current observer if it exists
        if (currentObserver != null) {
            updatePlantResponse.removeObserver(currentObserver);
        }

        // Add the new observer and update the current observer reference
        updatePlantResponse.observe(owner, observer);
        currentObserver = observer;
    }


    /**
     * Handles errors encountered during a Volley request.
     *
     * @param error The VolleyError encountered during the request.
     */
    private void handleError(final VolleyError error) {
        if (Objects.isNull(error.networkResponse)) {
            try {
                plantResponse.setValue(new JSONObject("{" +
                        "error:\"" + error.getMessage() +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        } else {
            String data = new String(error.networkResponse.data, Charset.defaultCharset())
                    .replace('\"', '\'');
            try {
                plantResponse.setValue(new JSONObject("{" +
                        "code:" + error.networkResponse.statusCode +
                        ", data:\"" + data +
                        "\"}"));
            } catch (JSONException e) {
                Log.e("JSON PARSE", "JSON Parse Error in handleError");
            }
        }
    }

    /**
     * Retrieves the current plant details for a user.
     *
     * @param userId The user ID.
     */
    public void getCurrentPlantDetails(int userId) {
        String url = "https://students.washington.edu/nchi22/api/plants/get_current_plant_details.php?user_id=" + userId;

        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                plantResponse::setValue,
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Updates the current plant details for a user.
     *
     * @param userId    The user ID.
     * @param newGrowth The new growth level.
     */
    protected void updateCurrentPlantDetails(int userId, double newGrowth) {
        String url = "https://students.washington.edu/nchi22/api/plants/update_current_plant_details.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("growthLevel", newGrowth);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                updatePlantDetailsResponse::setValue,
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }


    /**
     * Updates the current plant for a user.
     *
     * @param userId       The user ID.
     * @param plantOptionId The plant option ID.
     */
    public void updateCurrentPlant(int userId, int plantOptionId) {
        String url = "https://students.washington.edu/nchi22/api/plants/update_current_plant.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("plant_option_id", plantOptionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                updatePlantResponse::setValue,
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Retrieves the unlocked plants for a user.
     *
     * @param userId The user ID.
     */
    public void getUnlockedPlants(int userId) {
        String url = "https://students.washington.edu/nchi22/api/plants/get_plants_unlocked.php?user_id=" + userId;
        Request<JSONArray> request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Handle the response
                    unlockedPlantResponse.setValue(response);
                    Log.d("Success", "Response: " + response.toString());
                },
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Resets the current plant stage for a user.
     *
     * @param userId The user ID.
     */
    protected void resetCurrentPlantStage(int userId) {
        String url = "https://students.washington.edu/nchi22/api/plants/reset_current_plant_stage.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response ->
                        Log.i("Growth Level Updated", response.toString()),
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Resets the current plant for a user.
     *
     * @param userId  The user ID.
     * @param plantId The plant ID.
     */
    protected void resetCurrentPlant(int userId, int plantId) {
        String url = "https://students.washington.edu/nchi22/api/plants/reset_plant.php";
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("plant_option_id", plantId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request<JSONObject> request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                resetPlantResponse::setValue,
                this::handleError);

        Log.i("PlantViewModel", request.getUrl());
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }
}
