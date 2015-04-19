package com.torch2424.feather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

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

        //Show that we've created our service
        Log.d("Feather", "Create the wearable listener");

        //Dont change is active here since on create is called  multiple times
    }
    
    @Override
    //Whenever data is changed, this is launched
    public void onDataChanged(DataEventBuffer dataEvents) {
        //Get all of our evnets and put into a list
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

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
                    if(NotificationActivity.isActive) NotificationActivity.changeText(title);
                    else Log.d("Feather", "We're not active :'(");

                    //We only build our notification when it is started
                }
                //We get a notification with path dissmiss
                else if(path.equals(PATHDISMISS))
                {
                    Log.d("Feahter", "Questlove is in the house");
                    //Close the noticiation
                    NotificationManagerCompat notifyCompat = NotificationManagerCompat.from(this);
                    notifyCompat.cancel(NID);

                    //is Activer is now false
                    NotificationActivity.isActive = false;
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

                    //create a bitmap we want as our background
                    //We get it and and just fill it with our blue
                    Bitmap image = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                    //Feather blue color
                    image.eraseColor(Color.argb(255, 0, 153, 204));

                    // quit for action button
                    Intent quit = new Intent(this, MainActivity.class);
                    quit.putExtra("QUIT", true);
                    PendingIntent pendingQuit = PendingIntent.getActivity(this, 0, quit, 0);

                    // Create the ongoing notification
                    Notification.Builder notificationBuilder =
                            new Notification.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setOngoing(true)
                                    //This may have to be "" but trying to test a bug fix
                                    .setTicker("Feather Wear")
                                    //Need to create a new pending intent for app
                                    .addAction(R.drawable.quit,
                                            getString(R.string.quit), pendingQuit)
                                    .extend(new Notification.WearableExtender()
                                            .setDisplayIntent(notificationPendingIntent)
                                            //setting the background of the actual notification
                                            .setBackground(image)
                                            //Setting our action button
                                            );

                    // Build the notification and show it
                    NotificationManager notificationManager =
                            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(
                            NID, notificationBuilder.build());

                    //Set our wear variable is active to true
                    NotificationActivity.isActive = true;
                }
                else
                {
                    //The path is not recognized
                }
            }
        }
    }
}
