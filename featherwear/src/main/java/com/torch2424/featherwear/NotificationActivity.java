package com.torch2424.featherwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.Asset;

public class NotificationActivity extends Activity {

    private TextView mTextView;
    public static String TITLE = "Feather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }
}
