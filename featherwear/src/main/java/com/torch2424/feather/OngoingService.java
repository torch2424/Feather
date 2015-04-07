package com.torch2424.feather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class OngoingService extends WearableListenerService {
    //Our google api clinet
    private GoogleApiClient GoogClient;

    //our "constants" to be defined
    private final String KEY = "Feather";
    private final int NID = 548853;
    private String PATH = "/feather";
    private String PATHSTART = "/feather/start";
    private String PATHDISMISS = "/feather/quit";

    //Our notification manager so that we can close and open
    private NotificationManager notificationManager;

    //Boolean to store if notification is active or not
    private boolean isActive;


    public OngoingService()
    {

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        GoogClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        GoogClient.connect();
        isActive = false;

        //Show that we've created our service
        Log.d("Feather", "Create the wearable listener");
    }
    
    @Override
    //Whenever data is changed, this is launched
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Get all of our evnets and put into a list
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        //FIf our client has lost connection, try reconnecting, if we cant, return and leave
        if (!GoogClient.isConnected()) {
            Log.d("Feather", "Client is not connected");
            ConnectionResult connectionResult = GoogClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                //Could not connect to the api
                return;
            }
        }

        //For all of our data events that we receive, do this
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                //If it is feather's path
                if (PATH.equals(path))
                {
                    Log.d("Feather", "Changing text");
                    // Get the data out of the event
                    DataMapItem dataMapItem =
                            DataMapItem.fromDataItem(event.getDataItem());
                    final String title = dataMapItem.getDataMap().getString(KEY);

                    //Change our ticker text to the specified string
                    //Check with is active to make sure we dont crash
                    if(isActive) NotificationActivity.changeText(title);

                    //We only build our notification when it is started
                }
                //We get a notification with path dissmiss
                else if(path.equals(PATHDISMISS))
                {
                    Log.d("Feahter", "Questlove is in the house");
                    //Close the noticiation
                    notificationManager.cancel(NID);

                    //is Activer is now false
                    isActive = false;
                }
                else if(path.equals(PATHSTART))
                {
                    //Create our notification
                    Log.d("Feather", "Creating notifyication");
                    // Build the intent to display our custom notification
                    Intent notificationIntent =
                            new Intent(this, NotificationActivity.class);
                    notificationIntent.putExtra(
                            NotificationActivity.TITLE, "");
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity
                            (
                                    this,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create the ongoing notification
                    Notification.Builder notificationBuilder =
                            new Notification.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(
                                            getResources(), R.mipmap.ic_launcher))
                                    .setOngoing(true)
                                    .setTicker("")
                                    .extend(new Notification.WearableExtender()
                                            .setDisplayIntent(notificationPendingIntent));

                    // Build the notification and show it
                    notificationManager =
                            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(
                            NID, notificationBuilder.build());

                    //Set our variable is active to true
                    isActive = true;
                }
                else
                {
                    //The path is not recognized
                }
            }
        }
    }
}
