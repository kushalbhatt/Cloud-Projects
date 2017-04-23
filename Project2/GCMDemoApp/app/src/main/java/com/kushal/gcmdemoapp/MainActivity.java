package com.kushal.gcmdemoapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kushal.myapplication.backend.messaging.Messaging;
import com.example.kushal.myapplication.backend.messaging.model.MessageData;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity{

    TextView text1;
    EditText input;
    LocationAPI locationService;
    Messaging msgService;
    //public static String BACKEND_URL = "https://gcmdemoapp-63192.appspot.com/_ah/api/";
    //public static String REGISTERED = "is_registered";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text1 = (TextView) findViewById(R.id.hello_text);
        input = (EditText) findViewById(R.id.editText);
        locationService = new LocationAPI(this);

        SharedPreferences settings = getSharedPreferences(getString(R.string.SETTINGS),Context.MODE_PRIVATE);
        boolean is_registered =  settings.getBoolean(getString(R.string.IS_REGISTERED),false);
        if(!is_registered) {
            Log.d("Mainactivity:","Token Registeration intiated!");
            Intent intent= new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        //initialise backend messsaging service object
        Messaging.Builder builder = new Messaging.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null)
                // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                // otherwise they can be skipped
                .setRootUrl(getString(R.string.BACKEND_URL));
                /*.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                            throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });*/
        msgService = builder.build();

    }

    protected void onStart() {
        if(!locationService.isConnected())
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
        if(locationService.isConnected())
            locationService.disconnect();
        super.onStop();
    }

    public void getLocation(View v) {
        Location mLastLocation = locationService.fetchLatestLocation();
        if (mLastLocation != null) {
            text1.setText(mLastLocation.toString());

            //mLastLocation.distanceTo(mLastLocation);
            getDistance();
        }
        else
        {
            //locationService.showError();
            Toast.makeText(this,"Error while getting location. Try few seconds later!",Toast.LENGTH_LONG).show();
        }
    }

    //test button
    public void openMaps(View v)
    {
        /*Intent i = new Intent(this,DisplayMessage.class);

        String lat = String.valueOf(LocationAPI.mLastLocation.getLatitude());
        String lng = String.valueOf(LocationAPI.mLastLocation.getLongitude());
        Bundle b = new Bundle();
        b.putString("message","Bien Venidos!");
        b.putString("latitude",lat);
        b.putString("longitude",lng);
        i.putExtras(b);
        startActivity(i);
        */

        /*Uri gmmIntentUri = Uri.parse("geo:0,0?q="+lat+","+lng+"(Current Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        */
        locationService.showError();
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
                    MessageData payload = new MessageData();//msg.toString(),lat,lng
                    payload.setMessage(msg.toString());
                    payload.setLatitude(lat);
                    payload.setLongitude(lng);
                    payload.setHotness("5");
                    payload.setStuff("Free Knowledge");
                    Log.d("KUSHAL","Payload: "+payload.toString());
                    msgService.messagingEndpoint().sendMessage(payload).execute();
                    Log.d("KUSHAL","====== Sent!");
                } catch (Exception e) {
                    Log.d("KUSHAL","IO Error!");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getDistance()
    {
        new Thread()
        {
            @Override
            public void run() {
                HttpsURLConnection urlConnection = null;
                try {
                    Double lat = LocationAPI.mLastLocation.getLatitude();
                    Double lng = LocationAPI.mLastLocation.getLongitude();
                    URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + lat + "," + lng + "&destinations=Memorial+Union,Tempe");
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
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MAINACTIVITY", "Exception while fetching distance");
                } finally {
                    urlConnection.disconnect();

                }
            }
        }.start();
    }


}
