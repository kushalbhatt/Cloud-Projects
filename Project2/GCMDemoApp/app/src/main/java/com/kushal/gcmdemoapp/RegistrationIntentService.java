package com.kushal.gcmdemoapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kushal.myapplication.backend.registration.Registration;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

/**
 * Created by KUSHAL on 03-Apr-17.
 */

public class RegistrationIntentService extends IntentService {

    static String TAG = "RegIntentService";
    public RegistrationIntentService()
    {
        super("RegIntentService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            String senderid = "913755038494" ;
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(senderid,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            sendRegistrationToServer(token);

            //Update SharedPreferences:
            SharedPreferences settings = getSharedPreferences(getString(R.string.SETTINGS),Context.MODE_PRIVATE);
            boolean is_registered =  settings.getBoolean(getString(R.string.IS_REGISTERED),false);
            SharedPreferences.Editor edit = settings.edit();
            edit.putBoolean(getString(R.string.IS_REGISTERED), true);
            edit.apply();

        }
        catch(Exception e)
        {
            Log.e(TAG,"Exception Occured: ",e);
        }

    }

    private void sendRegistrationToServer(String token) throws IOException {
        Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
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
        Registration regService = builder.build();
        regService.register(token).execute();
        Log.d("KUSHAL","======Registered!");
        //Toast.makeText(getApplicationContext(),"Registred App!",Toast.LENGTH_LONG);
    }
}
