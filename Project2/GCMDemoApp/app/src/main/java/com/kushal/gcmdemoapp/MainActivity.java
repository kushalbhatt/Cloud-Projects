package com.kushal.gcmdemoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kushal.myapplication.backend.messaging.Messaging;
import com.example.kushal.myapplication.backend.messaging.model.MessageData;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    LocationAPI locationService;
    Messaging msgService;
    private RadioGroup radioGroup;
    private Spinner eventLocation;
    private Spinner foodType;

    private Button buttonSend;
    private EditText otherText;
    private String locationValue;
    private SeekBar hotBar;
    private EditText customMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        //UI Elements
        radioGroup = (RadioGroup) findViewById(R.id.RGroup);
        buttonSend=(Button) findViewById(R.id.buttonSend);
        eventLocation=(Spinner) findViewById(R.id.eventLocation);
        foodType=(Spinner)findViewById(R.id.foodType);
        hotBar = (SeekBar)findViewById(R.id.hotBar);
        locationService = new LocationAPI(this);
        otherText=(EditText) findViewById(R.id.otherText);
        customMessage=(EditText) findViewById(R.id.customMessage);
        foodType.setEnabled(false);
        otherText.setEnabled(false);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_display_events:
                Intent i = new Intent(this,EventsDisplayActivity.class);
                startActivity(i);
                Log.d("KUSHAL","Launched activity!!!");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStart() {
        //Log.d("KUSHAL","Main onStart() isconnected? = "+locationService.isConnected());
        if(!isNetworkAvailable()){
            //Toast.makeText(this, "Interet connection is not active", Toast.LENGTH_SHORT).show();
            //finish();
            showError();
        }
        if(!locationService.isConnected())
            locationService.connect();
        super.onStart();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("App requires Internet connection to work!")
                .setTitle("Oops! No Internet!");

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                //finishAffinity();
                finish();
            }
        });
        // 3. Get the AlertDialog from create()
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        Log.d("KUSHAL","Main onResume() isconnected? = "+locationService.isConnected());
        if(!locationService.isConnected())
            locationService.connect();
        super.onResume();
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

    public void getLocation() {
        Location mLastLocation = locationService.fetchLatestLocation();
        if (mLastLocation == null && eventLocation.getSelectedItem().toString().trim().equals("Current Location")) {
            Toast.makeText(this,"Error getting location. Try again after few seconds!",Toast.LENGTH_LONG).show();
        }
    }

    public void free_cast_it(View v)
    {
        getLocation();
        new Thread()
        {
            @Override
            public void run() {
                try {
                    String locationString = null;
                    String freeBieValue;
                    int hotBarValue;
                    String latitude,longitude;
                    String message;

                    hotBarValue=hotBar.getProgress();

                    locationValue= eventLocation.getSelectedItem().toString().trim();

                    if(locationValue.equals("Current Location"))
                    {
                        latitude  = String.valueOf(LocationAPI.mLastLocation.getLatitude());
                        longitude = String.valueOf(LocationAPI.mLastLocation.getLongitude());
                    }
                    else  //pre-defined hotspots
                    {
                        switch (locationValue)
                        {
                            case "Memorial Union North":
                                locationString=getResources().getString(R.string.Memorial_Union_North);
                                break;
                            case "Memorial Union 2nd Floor":
                                locationString=getResources().getString(R.string.Memorial_Union_2nd_Floor);
                                break;
                            case "Hayden Lawn":
                                locationString=getResources().getString(R.string.Hayden_Lawn);
                                break;
                            case "College Ave Commons":
                                locationString=getResources().getString(R.string.College_Ave_Commons);
                                break;
                            case "Sundevils Stadium":
                                locationString=getResources().getString(R.string.Sundevils_Stadium);
                                break;
                        }
                        String[] parts = locationString.split(",");
                        latitude=parts[0];
                        longitude=parts[1];
                    }

                    if(radioGroup.getCheckedRadioButtonId()==R.id.other)
                    {
                        freeBieValue=otherText.getText().toString().trim();
                    }
                    else if(radioGroup.getCheckedRadioButtonId()==R.id.swag)
                            {freeBieValue="Swag";}
                    else if(radioGroup.getCheckedRadioButtonId()==R.id.freefood)
                    {
                        freeBieValue=foodType.getSelectedItem().toString().trim();
                    }
                    else if(radioGroup.getCheckedRadioButtonId()==R.id.tshirt)
                            {freeBieValue="T-Shirt";}
                    else
                            freeBieValue="Free Stuff";

                    message = customMessage.getText().toString();

                    //create the message with appropriate data and call endpoints api
                    MessageData payload = new MessageData();
                    payload.setMessage(message);
                    payload.setLatitude(latitude);
                    payload.setLongitude(longitude);
                    payload.setHotness(String.valueOf(hotBarValue));
                    payload.setStuff(freeBieValue);
                    //Log.d("KUSHAL","Payload: "+payload.toString());
                    msgService.messagingEndpoint().sendMessage(payload).execute();
                    Log.d("KUSHAL","====== Sent!");
                } catch (Exception e) {
                    Log.d("KUSHAL","Exception / Error!");
                    e.printStackTrace();
                }
            }
        }.start();
        Toast.makeText(this,"Your message is FreeCasted",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId==R.id.freefood){
            foodType.setEnabled(true);
        }
        else
        if(checkedId==R.id.other){
            otherText.setEnabled(true);
        }
        else{
            foodType.setEnabled(false);
            otherText.setEnabled(false);
        }
    }

    /*
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
*/


}
