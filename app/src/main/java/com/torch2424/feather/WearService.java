package com.torch2424.feather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

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
}
