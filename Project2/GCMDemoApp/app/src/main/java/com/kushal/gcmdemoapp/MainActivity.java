package com.kushal.gcmdemoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kushal.myapplication.backend.messaging.Messaging;
import com.google.android.gms.location.LocationRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

public class MainActivity extends Activity{

    TextView text1;
    EditText input;
    LocationAPI locationService;
    Messaging msgService;
    public static String BACKEND_URL = "http://192.168.0.18:8080/_ah/api/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.hello_text);
        input = (EditText) findViewById(R.id.editText);
        locationService = new LocationAPI(this);
        //Log.d("Mainactivity:","intent thrown");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        //initialise backend messsaging service object
        Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                // otherwise they can be skipped
                .setRootUrl(BACKEND_URL)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                            throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });
        msgService = builder.build();
    }

    protected void onStart() {
        locationService.connect();
        super.onStart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onStop() {
        locationService.disconnect();
        super.onStop();
    }

    public void getLocation(View v) {
        Location mLastLocation = locationService.fetchLatestLocation();
        if (mLastLocation != null) {
            text1.setText(mLastLocation.toString());
        }
    }

    public void openMaps(View v)
    {
        String lat = String.valueOf(LocationAPI.mLastLocation.getLatitude());
        String lng = String.valueOf(LocationAPI.mLastLocation.getLongitude());
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"(Current Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void broadcast(View v)
    {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    Editable msg = input.getText();
                    String lat = String.valueOf(LocationAPI.mLastLocation.getLatitude());
                    String lng = String.valueOf(LocationAPI.mLastLocation.getLongitude());
                    msgService.messagingEndpoint().sendMessage(msg.toString(),lat,lng).execute();
                    Log.d("KUSHAL","====== Sent!");
                } catch (Exception e) {
                    Log.d("KUSHAL","IO Error!");
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
