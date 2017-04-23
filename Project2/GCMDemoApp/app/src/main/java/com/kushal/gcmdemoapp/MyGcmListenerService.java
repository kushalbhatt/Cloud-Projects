package com.kushal.gcmdemoapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

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
        //sendNotification(data);
        sendCustomNotification(data);
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GCM Message")
                .setContentText(data.getString("message"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        Log.d(TAG,"Notification Shown!");
    }

    void sendCustomNotification(Bundle data)
    {
        //normal view
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_layout);
        String latitude = data.getString("latitude");
        String longitude = data.getString("longitude");
        String message = data.getString("message");
        String stuff = data.getString("stuff");
        // Title  could be " Free  <category> available @"
        //remoteViews.setTextViewText(R.id.title,bundle.getString(""));
        remoteViews.setTextViewText(R.id.text,message);
        remoteViews.setTextViewText(R.id.title,"Free "+stuff);
        //Big View   we need to set category and other info as well
        RemoteViews bigView = new RemoteViews(getPackageName(),
                R.layout.big_notification);
        remoteViews.setTextViewText(R.id.big_text,message);

        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(Event Location)");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.setPackage("com.google.android.apps.maps");

        PendingIntent button_intent = PendingIntent.getActivity(this, 0, mapIntent, PendingIntent.FLAG_ONE_SHOT);

        remoteViews.setOnClickPendingIntent(R.id.notification_button,button_intent);
        bigView.setOnClickPendingIntent(R.id.big_button,button_intent);

        // Open NotificationView Class on Notification Click
        Intent intent = new Intent(this, DisplayMessage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = data;
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContent(remoteViews)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCustomBigContentView(bigView);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //for allowing multiple notifications unique id is needed
        Random random = new Random();
        int m = random.nextInt(9999);
        // Build Notification with Notification Manager
        notificationmanager.notify(m, builder.build());
        Log.d(TAG,"Custom Notification SHown!");
    }
}
