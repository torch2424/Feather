package com.torch2424.feather;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class NotificationPanel implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    //Private variables neccessary for creating our notification
	private Context context;
	private NotificationManager notifyMan;
	private static RemoteViews contentView;
	private Notification notifier;

    //Notification ID
    private final int NID = 548853;

    //Wearable google api client
    GoogleApiClient client;

    //our "constants" to be defined
    private final String KEY = "Feather";
    private String PATH = "/feather";
    private String PATHDISMISS = "/feather/quit";

	@SuppressLint("NewApi")
	public NotificationPanel(Context parent)
	{
		super();
		context = parent;
        //Get our notification
		String ns = Context.NOTIFICATION_SERVICE;
		notifyMan = (NotificationManager) context.getSystemService(ns);
		CharSequence tickerText = "Welcome To Feather";
		long when = System.currentTimeMillis();

        //Build our client
        client = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                //add our listeners
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //If it is not already connected connect
        if (!client.isConnected()) {
            client.connect();
        }


        //Create our initial notification
		Notification.Builder builder = new Notification.Builder(context);
		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();
		notification.when = when;
		notification.tickerText = tickerText;
		notification.icon = R.drawable.ic_launcher;

		contentView = new RemoteViews(context.getPackageName(),
				R.layout.medianotification);

		// set the button listeners
		setListeners(contentView);

        //Finalize our notification view
		notification.contentView = contentView;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //Send out our notification
		notifyMan.notify(NID, notification);
		notifier = notification;
	}

	public void setListeners(RemoteViews view)
	{
        /*
            Here we are creating the buttons on our notification
            First we get a key event, which sends out a media broadcast (acting as a remote)
            Then we get a pending intent to get ready to send this broadcast,
            finally we attach it to our buttons/views
         */
		// Previous
		Intent prev = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
		KeyEvent prevEvent = new KeyEvent(0, 0, 
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0); 
		prev.setAction(Intent.ACTION_MEDIA_BUTTON);
		prev.putExtra(Intent.EXTRA_KEY_EVENT, prevEvent);
		//prev.putExtra(Intent.ACTION_MEDIA_BUTTON, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
		PendingIntent btn1 = PendingIntent.getBroadcast(context, 0, prev, 0);
		view.setOnClickPendingIntent(R.id.prev, btn1);

		// Play/Pause - WORKS 
		Intent playpause = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent playEvent = new KeyEvent(0, 0, 
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0); 
		playpause.setAction(Intent.ACTION_MEDIA_BUTTON);
		playpause.putExtra(Intent.EXTRA_KEY_EVENT, playEvent);
		PendingIntent btn2 = PendingIntent.getBroadcast(context, 1, playpause, 0);
		view.setOnClickPendingIntent(R.id.playpause, btn2);

		// Next
		Intent next = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		KeyEvent nextEvent = new KeyEvent(0, 0, 
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0); 
		next.setAction(Intent.ACTION_MEDIA_BUTTON);
		next.putExtra(Intent.EXTRA_KEY_EVENT, nextEvent);
		PendingIntent btn3 = PendingIntent.getBroadcast(context, 3, next, 0);
		view.setOnClickPendingIntent(R.id.next, btn3);

        // quit
        Intent quit = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent quitEvent = new KeyEvent(0, 0,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE, 0);
        quit.setAction(Intent.ACTION_MEDIA_BUTTON);
        quit.putExtra(Intent.EXTRA_KEY_EVENT, quitEvent);
        //CANNOT USE 4 FOR SECOND VALUE IN PENDING INTENT FOR SOME WEIRD REASON
        PendingIntent btn4 = PendingIntent.getBroadcast(context, 5, quit, 0);
        view.setOnClickPendingIntent(R.id.quit, btn4);

		// Go Back To App
		Intent notify = new Intent(context, Ui.class);
		PendingIntent btn5 = PendingIntent.getActivity(context, 6, notify, 0);
		view.setOnClickPendingIntent(R.id.notifylayout, btn5);
	}
	
	

    //Close our notification
	public void notificationCancel()
	{
        //Close the wearable and our notification

        if (client.isConnected()) {

            //This is working

            //Set our path of our request, this time path dismiss so we close wearable notification
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATHDISMISS);

            // Add data to the request
            putDataMapRequest.getDataMap().putString(KEY,
                    "");
            putDataMapRequest.getDataMap().
                    putLong("time", new Date().getTime());

            //request our request
            PutDataRequest request = putDataMapRequest.asPutDataRequest();

            //finally disconnect our client
            client.disconnect();
        }
        notifyMan.cancel(NID);
	}


	//Redo our notification
	public void newNotify(String tickerText, String message)
	{
		notifier.tickerText = tickerText;
		contentView.setTextViewText(R.id.message, message);
		notifyMan.notify(NID, notifier);

        //Send this whenever we redo our notification, that way title is same on wear
        if (client.isConnected()) {
            //Set our path of our request
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH);

            // Add data to the request
            putDataMapRequest.getDataMap().putString(KEY,
                    tickerText);
            putDataMapRequest.getDataMap().
                    putLong("time", new Date().getTime());

            //request our request
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
        }
	}
	
	/**
	 * To switch from play to pause
	 * @param bool, true to play, flase to pause
	 */
	public static void playPause(boolean bool)
	{
		if(bool) contentView.setImageViewResource(R.id.playpause, R.drawable.playpauses);
		else contentView.setImageViewResource(R.id.playpause, R.drawable.playpauses);
	}

    //Our implements methods
    @Override
    public void onConnected(Bundle bundle)
    {
        //Since we are now connected, change the title of our notification,
        //so we make sure it is shown on wear
        //Create our data request
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH);

        // Add data to the request
        putDataMapRequest.getDataMap().putString(KEY,
                "Feather Wear");
        putDataMapRequest.getDataMap().
                putLong("time", new Date().getTime());

        //Request our request
        PutDataRequest request = putDataMapRequest.asPutDataRequest();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        //Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        //Do nothing
    }
}