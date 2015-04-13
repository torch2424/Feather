package com.torch2424.feather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.CardScrollView;
import android.support.wearable.view.WatchViewStub;
import android.view.Gravity;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        //Create our view
        //CardScrollView stub =
                //(CardScrollView) findViewById(R.id.card_scroll_view);
        /*
        code from when we were using a watchview stub

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                //Set our textview
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        */

        //Set our textview to the view in our layout
        mTextView = (TextView) findViewById(R.id.text_view);

        //Get our intent, and if there is one, set the title to that, or else the starting text
        Intent intent = getIntent();
        if (intent != null) {
            mTextView.setText(intent.getStringExtra(TITLE));

        }
        else mTextView.setText(getResources().getText(R.string.appstart));

        //Set up our handler
        UIHandler = new Handler(Looper.getMainLooper());

        //set is active to true
        isActive = true;
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

}
