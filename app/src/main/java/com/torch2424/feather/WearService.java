package com.torch2424.feather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
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

public class WearService extends WearableListenerService
{

    //Our google api clinet
    private GoogleApiClient GoogClient;

    //our "constants" to be defined
    private final String KEY = "Feather";
    private final int NID = 548853;
    private String PATHNEXT = "/feather/next";
    private String PATHPREV= "/feather/previous";
    private String PATHPLAY = "/feather/play";
    private String PATHQUIT = "/feather/quitfeather";

    public WearService()
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

        //Show that we've created our service
        Log.d("Feather", "Create the phone listener");

        //Dont change is active here since on create is called  multiple times
    }

    @Override
    //Whenever data is changed, this is launched
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Get all of our evnets and put into a list
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        Log.d("FEATHER", "WE IN HERE");

        //If our client has lost connection, try reconnecting, if we cant, return and leave
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
                //Put our path if else statements here
                if(path.equals(PATHNEXT))
                {
                    Ui.next(Ui.filePath);
                }
                else
                {
                    //The path is not recognized
                }
            }
        }
    }
}
