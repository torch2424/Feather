package com.torch2424.feather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity
{

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Using our main activity to quit feather, sinc eit is least likely to be used
        boolean toQuit = getIntent().getBooleanExtra("QUIT", false);
        Log.d("FEATHEREXTRA", String.valueOf(toQuit));
        if(toQuit)
        {
            //send the path quit
            NotificationActivity.quitApp();
        }
        //else jsut start the activity like normal
        else {
            super.onCreate(savedInstanceState);
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
