package com.torch2424.feather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class NotificationActivity extends Activity
{

    private static TextView mTextView;
    public static String TITLE = "Feather";
    //Our handler to run on the main ui thread
    private static Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mTextView = (TextView) findViewById(R.id.text_view);

        Intent intent = getIntent();
        if (intent != null) {
            mTextView.setText(intent.getStringExtra(TITLE));

        }

        //Set up our handler
        UIHandler = new Handler(Looper.getMainLooper());
    }

    public static void changeText(final String title)
    {

        final Runnable r = new Runnable() {
            public void run() {
                mTextView.setText(title);
            }
        };

        //Run our handler, no need for a second thread
        UIHandler.post(r);
    }

}
