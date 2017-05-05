package com.kushal.gcmdemoapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DisplayMessage extends AppCompatActivity {
    TextView msg ;
    TextView distance_estimate;
    TextView location;
    SeekBar seek ;
    int hotness = 0;
    String latitude;
    String longitude;
    LocationAPI locationService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_message_activity);
        distance_estimate = (TextView)findViewById(R.id.estimatedDistance);
        location = (TextView)findViewById(R.id.location_details);
        locationService = new LocationAPI(this);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        String m = "Free "+bundle.getString("stuff")+" is available!\n ";
        ((TextView)findViewById(R.id.titletext)).setText(m);
        //((TextView)findViewById(R.id.titletext)).setText(bundle.getString("message"));
        hotness = Integer.parseInt(bundle.getString("hotness"));
        Log.d("KUSHAL","hotness = "+hotness);
        seek = (SeekBar)findViewById(R.id.hotness_bar);

        latitude= bundle.getString("latitude");
        longitude= bundle.getString("longitude");

        msg = (TextView)findViewById(R.id.extra_message);
        msg.setText(bundle.getString("message"));
    }

    protected void onStart() {
        if(!locationService.isConnected())
            locationService.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        seek.setProgress(hotness);
        //requires an internet connection
        if(isNetworkAvailable())
            new GetDistanceTask().execute(new String[]{latitude,longitude});
        else
            Toast.makeText(this,"Couldn't load distance and address! Requires internet connection",Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void onStop() {
        if(locationService.isConnected())
            locationService.disconnect();
        super.onStop();
    }

    public void showinMaps(View v) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(Current Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private class GetDistanceTask extends AsyncTask<String, Void, String[]>
    {

        @Override
        protected String[] doInBackground(String... location) {
            HttpsURLConnection urlConnection = null;
            try {
                 double lat = 0,lng=0;
                if(LocationAPI.mLastLocation != null){
                    lat = LocationAPI.mLastLocation.getLatitude();
                    lng = LocationAPI.mLastLocation.getLongitude();
                    }

                    URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + lat + "," + lng + "&destinations=" + location[0] + "," + location[1]);
                    urlConnection = (HttpsURLConnection) url.openConnection();
                    //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String jsonString = new String();

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    jsonString = sb.toString();
                    JSONObject json = new JSONObject(jsonString);
                    String dest = json.getJSONArray("destination_addresses").getString(0);
                    JSONObject element = json.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0); //.getJSONObject("distance");
                    String distance = element.getJSONObject("distance").getString("text");
                    String duration = element.getJSONObject("duration").getString("text");
                    //Log.d("KUSHAL", "-----****Destination = " + dest + "Distance output: " + distance + " ~Time: " + duration);

                String[] results = new String[3];
                    results[0] = distance;
                    results[1] = duration;
                    results[2] = dest;
                    return results;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("KUSHAL", "Exception while fetching distance");
            } finally {
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            //use this results to change the values of text fields
            if(LocationAPI.mLastLocation==null)
            {
                location.setText("Error while fetching current location!");
                distance_estimate.setText("N/A");
            }
            else {
                location.setText("Near... " + results[2]);
                String text = "Approx. Distance:\n\t " + results[0];
                text += "\nApprox Time: \n\t" + results[1];
                distance_estimate.setText(text);
            }
        }
    }
}
