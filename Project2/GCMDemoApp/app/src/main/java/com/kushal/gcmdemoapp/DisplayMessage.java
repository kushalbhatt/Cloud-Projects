package com.kushal.gcmdemoapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DisplayMessage extends AppCompatActivity {
    TextView msg ;
    String latitude;
    String longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        String meg = bundle.getString("message");
        latitude= bundle.getString("latitude");
        longitude= bundle.getString("longitude");
        msg = (TextView)findViewById(R.id.msgtext);
        msg.setText(meg);
    }


    public void showinMaps(View v) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(Current Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
