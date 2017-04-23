package com.kushal.gcmdemoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DisplayMessage extends AppCompatActivity {
    TextView msg ;
    String latitude;
    String longitude;
    LocationAPI locationService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        locationService = new LocationAPI(this);
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        String m = bundle.getString("message");
        m+= bundle.getString("hotness") +"   "+bundle.getString("stuff");
        latitude= bundle.getString("latitude");
        longitude= bundle.getString("longitude");
        new GetDistanceTask().execute(new String[]{latitude,longitude});
        msg = (TextView)findViewById(R.id.msgtext);
        msg.setText(m);
    }

    protected void onStart() {
        if(!locationService.isConnected())
            locationService.connect();
        super.onStart();
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
                    Double lat = LocationAPI.mLastLocation.getLatitude();
                    Double lng = LocationAPI.mLastLocation.getLongitude();
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
                    Log.d("KUSHAL", "-----****Destination = " + dest + "Distance output: " + distance + " ~Time: " + duration);

                String[] results = new String[3];
                    results[0] = distance;
                    results[1] = duration;
                    results[2] = dest;
                    return results;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("KUSHAL", "Exception while fetching distance");
            } finally {
                urlConnection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            //use this results to change the values of text fields
            String text = msg.getText().toString();
            text+= results[0]+"        "+results[1]+"       "+results[2];
            msg.setText(text);
        }
    }
}
