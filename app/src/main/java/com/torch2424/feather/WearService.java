package com.torch2424.feather;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
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

    //Our handler to change songs and things
    Handler handler;

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

        //Initialize our handler
        handler = new Handler(Looper.getMainLooper());

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
                    handler.post(new Runnable() {
                        public void run() {
                           Ui.next(Ui.filePath);
                        }
                    });
                }
                else if(path.equals(PATHPLAY))
                {
                    handler.post(new Runnable() {
                        public void run() {
                            Ui.playPause(Ui.filePath);
                        }
                    });
                }
                else if(path.equals(PATHPREV))
                {
                    handler.post(new Runnable() {
                        public void run() {
                            Ui.prev(Ui.filePath);
                        }
                    });
                }
                else if(path.equals(PATHQUIT))
                {
                    // send a quit broadcast
                    Intent quit = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    KeyEvent quitEvent = new KeyEvent(0, 0,
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0);
                    quit.setAction(Intent.ACTION_MEDIA_BUTTON);
                    quit.putExtra(Intent.EXTRA_KEY_EVENT, quitEvent);
                    sendBroadcast(quit);
                }
                else
                {
                    //The path is not recognized
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        // to make sure it isnt recreated
        // (It is for a little, but once app fully closes it is closed fully again)
        stopSelf();
    }
}
