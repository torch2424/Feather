package com.torch2424.feather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

public class OngoingService extends WearableListenerService
{
    //Our google api clinet
    private GoogleApiClient GoogClient;

    //our constants to be defined
    private final String KEY = "Feather";
    private final int NID = 548853;
    private String PATH = "/feather";

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

    }
    
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!GoogClient.isConnected()) {
            ConnectionResult connectionResult = GoogClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                //Could not connect to the api
                return;
            }
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (PATH.equals(path)) {
                    // Get the data out of the event
                    DataMapItem dataMapItem =
                            DataMapItem.fromDataItem(event.getDataItem());
                    final String title = dataMapItem.getDataMap().getString(KEY);

                    // Build the intent to display our custom notification
                    Intent notificationIntent =
                            new Intent(this, NotificationActivity.class);
                    notificationIntent.putExtra(
                            NotificationActivity.TITLE, title);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity
                            (
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create the ongoing notification
                    Notification.Builder notificationBuilder =
                            new Notification.Builder(this)
                                    //Need to make these feather icons
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(
                                            getResources(), R.mipmap.ic_launcher))
                                    .setOngoing(true)
                                    .extend(new Notification.WearableExtender()
                                            .setDisplayIntent(notificationPendingIntent));

                    // Build the notification and show it
                    NotificationManager notificationManager =
                            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(
                            NID, notificationBuilder.build());
                } else
                {
                    //The path is not recognized
                }
            }
        }
    }

}
