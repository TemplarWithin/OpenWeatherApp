package com.christiangallegos.openweatherapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "openweatherapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView image_weather = (ImageView)findViewById(R.id.image_weather);
        final TextView temperature_weather = (TextView)findViewById(R.id.temperature_weather);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://localhost/Clima2/?var1=538601";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("weather")) {

                                JSONArray weatherArray = response.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);

                                if (weather.has("icon")) {

                                    String icon = weather.getString("icon");
                                    int identifier = getResources().getIdentifier("images_" + icon, "drawable", getPackageName());
                                    image_weather.setImageDrawable(getResources().getDrawable(identifier, null));

                                }
                            }

                            if (response.has("main")){
                                JSONObject main = response.getJSONObject("main");
                                Double temp = main.getDouble("temp");
                                temperature_weather.setText("" + temp +" \u00b0");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override

                    public void onErrorResponse(VolleyError error){
                        Log.d(TAG,error.getMessage());
                    }
                });
        queue.add(jsonObjectRequest);
    }
}
