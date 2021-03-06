package com.torch2424.feather;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class MainActivity extends Activity
{

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Using our main activity to quit feather, sinc eit is least likely to be used
        super.onCreate(savedInstanceState);
        boolean toQuit = getIntent().getBooleanExtra("QUIT", false);
        if(toQuit)
        {
            //send the path quit
            NotificationActivity.quitApp();
            //Finish opur activity so it is never shown
            finish();
        }
        //else jsut start the activity like normal
        else {
            setContentView(R.layout.activity_main);
            final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
            stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                @Override
                public void onLayoutInflated(WatchViewStub stub) {
                    mTextView = (TextView) stub.findViewById(R.id.text);
                }
            });
        }
    }
}
