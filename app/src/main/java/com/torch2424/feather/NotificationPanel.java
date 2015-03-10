package com.torch2424.feather;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.RemoteViews;

public class NotificationPanel
{

	private Context context;
	private NotificationManager notifyMan;
	private static RemoteViews contentView;
	private Notification notifier;

	@SuppressLint("NewApi")
	public NotificationPanel(Context parent)
	{
		super();
		context = parent;
		String ns = Context.NOTIFICATION_SERVICE;
		notifyMan = (NotificationManager) context.getSystemService(ns);
		CharSequence tickerText = "Welcome To Feather";
		long when = System.currentTimeMillis();
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

		notification.contentView = contentView;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		// CharSequence contentTitle = "From Shortcuts";
		notifyMan.notify(548853, notification);
		notifier = notification;
	}

	public void setListeners(RemoteViews view)
	{
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

		// Go Back To App
		Intent notify = new Intent(context, Ui.class);
		PendingIntent btn4 = PendingIntent.getActivity(context, 5, notify, 0);
		view.setOnClickPendingIntent(R.id.notifylayout, btn4);
	}
	
	

	public void notificationCancel()
	{
		notifyMan.cancel(548853);
	}
	
	public void newNotify(String tickerText, String message)
	{
		notifier.tickerText = tickerText;
		contentView.setTextViewText(R.id.message, message);
		notifyMan.notify(548853, notifier);
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
}