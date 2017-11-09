package com.christiangallegos.openweatherapp;

import android.support.v4.app.ActivityCompat;
import android.Manifest;

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

import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "openweatherapp";
    private static final int REQUEST_LOCATION = 22;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creamos una instancia del Google Api Client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            //Aqui ya nos conectamos al Servicio del Api de Google
                            //Podemos solicitar la ubicacion, este metodo esta definido abajo
                            getLocation();
                        }
                        @Override
                        public void onConnectionSuspended(int i) {
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /*Solicita la ubicacion mediante GPS. Primero se tiene que verificar que el usuario otorgue los permisos.*/
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //La primera vez que se ejecuta la actividad, se solicitan permisos
            //Si el usuario selecciono ok, o cancel en la ventana de permisos se mandara el resultado a onRequestPermissionsResult. Este metodo
            //se define abajo
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        //Aqui, ya tenemos permisos
        //Iniciamos
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        makeHttpRequest(newLocation);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            //Tenemos permisos
            getLocation();
        } else {
            // Permission was denied. Display an error message.
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void makeHttpRequest(LatLng newLocation){

        final ImageView image_weather = (ImageView)findViewById(R.id.image_weather);
        final TextView place_weather = (TextView)findViewById(R.id.place_weather);
        final TextView status_weather = (TextView)findViewById(R.id.status_weather);
        final TextView temperature_weather = (TextView)findViewById(R.id.temperature_weather);
        final TextView latitude_weather = (TextView)findViewById(R.id.latitude_weather);
        final TextView longitude_weather = (TextView)findViewById(R.id.longitude_weather);

        longitude_weather.setText("" + newLocation.longitude +" \u00b0");
        latitude_weather.setText("" + newLocation.latitude +" \u00b0");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://10.10.5.72/Clima2/?var1="+newLocation.latitude+"&var2="+newLocation.longitude;
        
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

                                if (weather.has("description")) {

                                    String description = weather.getString("description");
                                    status_weather.setText("Current Weather: " + description);

                                }
                            }

                            if (response.has("name")) {
                                String name = response.getString("name");
                                place_weather.setText("Current City: " + name);
                            }

                            if (response.has("main")){
                                JSONObject main = response.getJSONObject("main");
                                Double temp = main.getDouble("temp");
                                temperature_weather.setText("Current Temperature: " + temp +" \u00b0");
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
