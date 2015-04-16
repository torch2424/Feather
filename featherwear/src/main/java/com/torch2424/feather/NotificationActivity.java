package com.torch2424.feather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.CardScrollView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class NotificationActivity extends Activity
{

    //Our textview that will display song titles
    private static TextView mTextView;
    //Our "Constant"
    public static String TITLE = "Feather";
    //Our handler to run on the main ui thread
    private static Handler UIHandler;

    //Our boolean to state that we have been created
    public static boolean isActive;

    //Our google api clinet
    private GoogleApiClient GoogClient;

    //our "constants" to be defined
    private final String KEY = "Feather";
    private final int NID = 548853;
    private String PATHNEXT = "/feather/next";
    private String PATHPREV= "/feather/previous";
    private String PATHPLAY = "/feather/play";
    private String PATHQUIT = "/feather/quitfeather";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //Set our textview to the view in our layout
        mTextView = (TextView) findViewById(R.id.cardtext);

        //Get our intent, and if there is one, set the title to that, or else the starting text
        Intent intent = getIntent();
        if (intent != null) {
            mTextView.setText(intent.getStringExtra(TITLE));
        }

        //Set up our handler
        UIHandler = new Handler(Looper.getMainLooper());

        //set is active to true
        isActive = true;

        //Build our client
        GoogClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        GoogClient.connect();
    }

    //Function to change our textview ticker text
    public static void changeText(final String title)
    {
        //Crate our runnable to be run in our handler on the UI thread
        final Runnable r = new Runnable() {
            public void run() {
                mTextView.setText(title);
            }
        };

        //Run our handler, no need for a second thread
        UIHandler.post(r);
    }

    //Function that is called when our media buttons are clicked
    public void mediaButton(View view)
    {
        //Send this whenever we redo our notification, that way title is same on wear
        if (GoogClient.isConnected())
        {
            //Set our path of our request, depending on our button
            String button = "";
            if(view.getId() == R.id.next) button = PATHNEXT;
            else if(view.getId() == R.id.playpause) button = PATHPLAY;
            else button = PATHPREV;
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(button);

            // Add data to the request (Just so it has changed and it is recieved
            putDataMapRequest.getDataMap().
                    putLong("time", new Date().getTime());

            //request our request
            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            //Send to wearable
            Wearable.DataApi.putDataItem(GoogClient, request);
        }
    }

}
