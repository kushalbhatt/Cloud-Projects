package com.kushal.gcmdemoapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by KUSHAL on 03-Apr-17.
 */

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "GCMListener";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String lat = data.getString("latitude");
        String lng = data.getString("longitude");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message+"Latitude : "+lat+"\tLong: "+lng);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        //show notification to user
        sendNotification(data);
    }

    void sendNotification(Bundle data)
    {
        Intent intent = new Intent(this, DisplayMessage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = data;
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle("GCM Message")
                .setContentText(data.getString("message"))
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        Log.d(TAG,"Notification SHown!");
    }
}
