package com.torch2424.feather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class MusicControl extends BroadcastReceiver
{
	// we are intercepting media button controls here
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// random view to pass through class
		TextView view = new TextView(context);
		// creating class object to call functions
		// Do Stuff Here
		AudioManager manager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
		{
			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			int keyCode = event.getKeyCode();
			int keyAction = event.getAction();
			// if play is pressed
			// && key down used since each click sends an intent for pressing
			// down, and pressing up
			if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode
					&& KeyEvent.ACTION_DOWN == keyAction)
			{
				if (manager.isMusicActive())
				{

				}
				else
				{
					Ui.playPause(view);
				}
			}

			// if play/pause is pressed
			else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
					&& KeyEvent.ACTION_DOWN == keyAction)
			{
				if (manager.isMusicActive())
				{
					// Play
					Ui.playPause(view);
				}
				else
				{
					// Pause
					Ui.playPause(view);
				}
			}

			// if pause is pressed
			else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
					&& KeyEvent.ACTION_DOWN == keyAction)
			{
				if (manager.isMusicActive())
				{

				}
				else
				{
					// Pause
					Ui.playPause(view);
				}
			}

			// if next is pressed
			else if ((keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
					&& KeyEvent.ACTION_DOWN == keyAction)
			{
				// stop everything that is playing
				if (manager.isMusicActive())
				{
					// this is to avoid double skipping
					int currentDuration = 5000;

					// If music playing
					if (BGMusic.bgmusic.isPlaying())
					{
						// get current position
						currentDuration = BGMusic.bgmusic.getDuration();
					}
					// if video playing
					else if (Ui.player.isPlaying())
					{
						// Get current position
					}

					// only skip songs if its been playing for a second and a
					// half
					if (currentDuration > 1500)
					{
						// Next
						Ui.next(view);
					}
				}
			}

			// if previous is pressed
			else if ((keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND)
					&& KeyEvent.ACTION_DOWN == keyAction)
			{

				// this is to avoid double skipping
				int currentDuration = 5000;
				// If music is playing
				if (BGMusic.bgmusic.isPlaying())
				{
					// get current position
					currentDuration = BGMusic.bgmusic.getDuration();
				}
				// If video is playing
				else if (Ui.player.isPlaying())
				{
					// Get current position
				}

				// only skip songs if its been [;aying for a second and a half

				if (currentDuration > 1500)
				{
					// Previous
					Ui.prev(view);
				}
			}
            // if quit is pressed
            else if (keyCode == KeyEvent.KEYCODE_ESCAPE)
            {
                //Quit the application, do this by finishing quit
                Ui.activity.finish();
            }

		}
        //For catching headset unplugged and plugged
        else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()))
        {
            //First get if we want to respond to this
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences("MyPrefs", 0);
            //If our headset boolean is true, and our bg music is not null
            if(prefs.getBoolean("HEADSET", true) && Ui.bgMusic != null)
            {
                //Get the extra that contains the headset state
                int state = intent.getIntExtra("state", -1);
                //if the state is 0 for unplugged and music is playing
                //Stop
                if(state == 0 && Ui.bgMusic.isPlaying())
                {
                    // Pause without a fade
                    Ui.bgMusic.pauseSongNoFade();
                }
            }
        }

	}
}
