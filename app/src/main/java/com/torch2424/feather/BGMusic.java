package com.torch2424.feather;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class BGMusic extends Service implements OnCompletionListener
{
	// mediaplayer and binder
	static MediaPlayer bgmusic;
	IBinder musicBind = new MusicBinder();
	// for the fade in and out
	float volume;
	// RATE is how fast or slow you want fade 0,0.1,0.2 etc...
	public static final float RATE = (float) 0.08;

	// to fix conflict with music fading out and then not playing next song, and
	// pausing
	private boolean fading;
	private boolean isPaused;
	
	// Our Playlist
	public ArrayList<File> playList;
	public int index;

	// fading timers, using here to stop start song errors
	Timer timer;

	// For Shuffling and looping
	public boolean shuffleBool;
	public boolean loopBool;

	// Our notification
	NotificationPanel notification;

    //Preferences for sorting by track
    SharedPreferences prefs;

    //Boolean to use the helper when we are sorting by track
    boolean stopHelper;

	@Override
	public IBinder onBind(Intent arg0)
	{
		return musicBind;
	}

	// binder for the music
	public class MusicBinder extends Binder
	{
		public BGMusic getService()
		{
			return BGMusic.this;
		}
	}

	// No unbind, since we wnat service running even when unbinded
	@Override
	public void onDestroy()
	{
		// done with fade out
		bgmusic.stop();
		bgmusic.reset();
		bgmusic.release();
		// to make sure it is recreated
		stopSelf();

		// quit notification
		notification.notificationCancel();
	}

	@Override
	public void onCreate()
	{
		// create the service
		super.onCreate();
		// create mediaplayer, set wake lock and
		bgmusic = new MediaPlayer();
		bgmusic.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// No Wakelock might need it if music is dropping, dont forget to re-add
		// permission
		// bgmusic.setWakeMode(this.getApplicationContext(),
		// PowerManager.PARTIAL_WAKE_LOCK);
		playList = new ArrayList<File>();
		index = 0;

		volume = 1;
		fading = false;
		isPaused = false;
		loopBool = false;
		notification = new NotificationPanel(getApplicationContext());

        //Preparing our preferences for our settings
        prefs = this.getApplicationContext().getSharedPreferences("MyPrefs", 0);
	}

	public void FadeIn()
	{
		fading = true;
		volume = 0;
		bgmusic.setVolume(volume, volume);
		// do this every half second so we need timer
		bgmusic.start();
		timer = new Timer(true);
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				// hopefully fixes music crashes
				if (bgmusic != null)
				{
					bgmusic.setVolume(volume, volume);
				}
				else
				{
					timer.cancel();
					timer.purge();
				}
				volume = (float) (volume + RATE);
				if (volume > 1)
				{
					// adding set music of max volume here in case of odd
					// numbered RATEs
					volume = 1;
					bgmusic.setVolume(volume, volume);
					timer.cancel();
					timer.purge();
					fading = false;
				}
			}
		};

		timer.schedule(timerTask, 0, 50);

	}

	public void FadeOut()
	{
		// do this every half second so we need timer
		fading = true;
		timer = new Timer(true);
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				// hopefully fixes music crashes
				if (bgmusic != null)
				{
					bgmusic.setVolume(volume, volume);
				}
				else
				{
					timer.cancel();
					timer.purge();
				}
				volume = (float) (volume - RATE);
				if (volume < 0)
				{
					volume = 0;
					bgmusic.setVolume(volume, volume);
					timer.cancel();
					timer.purge();
					bgmusic.pause();
					fading = false;
				}
			}
		};

		timer.schedule(timerTask, 0, 50);
	}

	public void cancelFade()
	{
		// reset timers
		timer.cancel();
		timer.purge();

		// Pause music
		bgmusic.stop();

		// reset volumes
		volume = 0;
		bgmusic.setVolume(volume, volume);

		// Set fading to false
		fading = false;
	}

	/**
	 * Starts to play the playlist
	 * 
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void startMedia() throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException
	{
		/*
		 * INITIALIZE PLAYLIST
		 */
		// get our file we shall be using
		File selectedFile = playList.get(index);

		// Need to know if we need to randomize or sort playlist
		if (shuffleBool)
		{
			shuffle(selectedFile);
		}
		else
		{
			sort(selectedFile);
		}

		/*
		 * RESET EVERYTHING
		 */
		if (fading)
		{
			cancelFade();
		}
		if (bgmusic != null)
		{
			bgmusic.reset();
		}
		// Set the volume back to max
		bgmusic.setVolume((float) 1.0, (float) 1.0);

		if (Ui.player.isPlaying())
		{
			Ui.player.stopPlayback();
			Ui.videoLayout.setVisibility(View.GONE);
		}
		else if (Ui.player.isShown())
		{
			Ui.videoLayout.setVisibility(View.GONE);
		}

		/*
		 * LISTNERS AND USER NOTIFICATION
		 */
		// Set the onCompletionListeners
		bgmusic.setOnCompletionListener(this);
		Ui.player.setOnCompletionListener(this);

        //Doing this because we are sorting by music or alphabetically
        //This will stop the helper from being run if it is going to be executed by the background thread
        //inside the shuffle function, therefore music isn't played before we are done sorting
        if(!stopHelper)
        {
            startMediaHelper(selectedFile);
        }
	}

    //Function to fnish start media
    private void startMediaHelper(File selectedFile) throws IllegalArgumentException,
            SecurityException, IllegalStateException, IOException
    {
        // Do Notification Stuff Here
        notification.newNotify(selectedFile.getName(), selectedFile.getName());

        // Seekbar done by UI

        Ui.playing.setText(selectedFile.getName());

		/*
		 * PLAY MEDIA DEPEDING ON VIDEO OR MUSIC
		 */
        if (Manly.isMusic(selectedFile))
        {
            bgmusic.setDataSource(selectedFile.getAbsolutePath());
            bgmusic.prepare();
            // Play the Song
            playSong();
        }
        else
        {
            Ui.videoLayout.setVisibility(View.VISIBLE);
            Ui.player.setVideoPath(selectedFile.getAbsolutePath());
            //play the video
            Ui.player.start();
        }
    }
	
	/**
	 * Return an array of files in a foler as a playlist, returns only music files or video files
	 * @param directory, Directory of which we want to extract from
	 * @param array, Array we want to add to
	 * @param musicOrVideo true for music, false fo video
	 * @return the final array
	 */
	public ArrayList<File> getPlaylist(File directory, ArrayList<File> array, boolean musicOrVideo)
	{
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList)
		{
			if (file.isFile())
			{
				if (Manly.isMusic(file) && musicOrVideo)
				{
					array.add(file);
				}
				else if(Manly.isVideo(file) && !musicOrVideo)
				{
					array.add(file);
				}
			}
			else if (file.isDirectory())
			{
				getPlaylist(file, array, musicOrVideo);
			}
		}

		return array;
	}

	/**
	 * Shuffles our playlist for us while keeping the current playlist
	 */
	public void shuffle(File currentFile)
	{
		// Need to make sure the first file we play is the one we wanted
		// Save the file, remove, shuffle, put file back in front

		// Need to sure playlist isn't null or empty
		if (playList != null && !playList.isEmpty())
		{
			playList.remove(index);
			Collections.shuffle(playList);
			playList.add(0, currentFile);
			index = 0;
		}
	}

	/**
	 * Sorts our current playlist while keeping the current playlist
	 */
	public void sort(final File currentFile)
	{
		// Need to sure playlist isn't null or empty
		if (playList != null && !playList.isEmpty() && currentFile != null)
		{
			// Make file copy, sort, and find where it's at, set index
			final File tempFile = currentFile;

            //Now supporting sorting by track index
            //Checking if they want it sorted by track index, and it is music
            //But sort by alphabet anyways incase of errorreds with metadatasort
            if(prefs.getBoolean("MUSICSORT", false) && Manly.isMusic(playList.get(0)))
            {
                stopHelper = true;
                //Show a loading screen since it takes a while to sort
                // Show Loading
                final ProgressDialog loading = ProgressDialog
                        .show(Ui.context,
                                "",
                                "Sorting your files by track number"
                                        + ", this may take a "
                                        + "while depending on playlist size."
                                        + " Please wait...",
                                true);
                //Sort our stuff on a new thread, and close our loading dialog there
                Ui.mThread = new Thread()
                {
                    @Override
                    public void run()
                    {
                        //Sort with our song metadata comparator
                        Collections.sort(playList, new SongComparator());

                        //Find the song we originally clicked
                        for (int i = 0; tempFile != null; ++i)
                        {
                            if (tempFile == playList.get(i))
                            {
                                index = i;
                                //break to save process time
                                break;

                            }
                        }

                        runOnUiThread(new Runnable()
                        {
                            public void run() {
                                //Dismiss the loading pop-up
                                loading.dismiss();

                                //Now finish playing the playlist
                                try {
                                    startMediaHelper(currentFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                };

                //Start the thread
                Ui.mThread.start();
            }
            else
            {
                //Sort normally
                Collections.sort(playList);

                //Find the song we originally clicked
                for (int i = 0; tempFile != null; ++i)
                {
                    if (tempFile == playList.get(i))
                    {
                        index = i;git 
                        //break to save process time
                        break;

                    }
                }
            }

            //Testing playlist
            for (int i = 0; i < playList.size(); ++i)
            {
                Log.d("Feather playlist", playList.get(i).getName());
            }
            //now log index
            Log.d("Feather index", Integer.toString(index));
		}

	}

    private void runOnUiThread(Runnable runnable)
    {
        Ui.uiHandler.post(runnable);
    }

    /**
	 * Plays(Continues) song with fading
	 */
	public void playSong()
	{
		if (fading)
		{
			cancelFade();
		}
		else
		{
			isPaused = false;
			FadeIn();
		}
	}

	/**
	 * pauses song with fading
	 */
	public void pauseSong()
	{
		// need to check if it is playing duh
		// checking will help with closing app
		if (bgmusic != null)
		{
			if (fading)
			{
				cancelFade();
			}
			else
			{
				isPaused = true;
				FadeOut();
			}
		}
	}

    /**
     * pauses song without fading
     */
    public void pauseSongNoFade()
    {
        // need to check if it is playing duh
        // checking will help with closing app
        if (bgmusic != null)
        {
            if (fading)
            {
                cancelFade();
            }
            else
            {
                isPaused = true;
                bgmusic.pause();
            }
        }
    }
	
	/**
	 * This switches the play logo to the pause logo and vice versa
	 * 
	 * @param play true to switch to play, false to switch to pause
	 */
	@SuppressLint("NewApi")
	public void switchPlayPause(boolean play)
	{
		//Switch notification playpause
		//NotificationPanel.playPause(play);
		//Deprecated to API 16
		//switch Ui playpause
		//if(play) Ui.playPause.setBackgroundDrawable(getResources().getDrawable(R.drawable.plays));
		//else Ui.playPause.setBackgroundDrawable(getResources().getDrawable(R.drawable.pauses));
	}

	/**
	 * Stops the currently playing song
	 */
	public void stopSong()
	{
		// to stop errors
		if (bgmusic != null)
		{
			bgmusic.stop();
		}
	}

	/**
	 * Is the music player playing?
	 * 
	 * @return true if yes, false if no
	 */
	public boolean isPlaying()
	{
		if (bgmusic != null && bgmusic.isPlaying())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Is the music player paused?
	 * 
	 * @return true if yes, false if no
	 */
	public boolean isPaused()
	{
		if (isPaused)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Is the music player looping?
	 * 
	 * @return true if yes, false if no
	 */
	public boolean isLooping()
	{
		if (bgmusic != null && loopBool)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Enables music to looping it if isn't, and vice versa
	 */
	public void changeLooping()
	{
		if (bgmusic != null && loopBool)
		{
			loopBool = false;
		}
		else
		{
			loopBool = true;
		}
	}

	/**
	 * Returns the currently playing song, Null if playlist is empty
	 */
	public File getCurrentSong()
	{
		if (!playList.isEmpty())
			return playList.get(index);
		else
			return null;
	}

	/**
	 * Seeks to the specified section of song
	 * 
	 * @param progress
	 *            , to time to seek to
	 */
	public void seek(int progress)
	{
		bgmusic.seekTo(progress);
	}

	/**
	 * Returns the position of the music playing (int)
	 */
	public int getPosition()
	{
		return bgmusic.getCurrentPosition();
	}

	/**
	 * Returns the duration of the music playing (int)
	 */
	public int getDuration()
	{
		return bgmusic.getDuration();
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		// Only need to check if looping, playlists are already shuffled
		if (loopBool)
			; // Do Nothing?
		else
			++index; // increment index

		// Did the playlist end?
		if (index == playList.size())
		{
			// commented out these lines to allow users to go
			// to previous songs if playlist has ended
			Ui.playing.setText("");
			Ui.videoLayout.setVisibility(View.GONE);

			// Do Notification Stuff
			notification.newNotify("Playlist Ended!", "Playlist Ended!");

			// Disable seekbar to stop running it
			Ui.seekBar.setEnabled(false);
		}
		else
		{
			// Open Next File
			try
			{
				easyNext();
			}
			catch (IllegalStateException | IOException e)
			{
				// TODO Auto-geneRATEd catch block
				e.printStackTrace();
			}
		}

	}

	public void easyNext() throws IllegalStateException, IOException
	{
		// stop everything that is playing
		if (bgmusic.isPlaying())
		{
			bgmusic.reset();
		}
		if (Ui.player.isPlaying())
		{
			Ui.player.pause();
		}

		// Stop Video and release lock
		Ui.videoLayout.setVisibility(View.GONE);

		// Getting once here to save resources and Notify
		File tempFile = playList.get(index);

		notification.newNotify(tempFile.getName(), tempFile.getName());

		if (Manly.isMusic(tempFile))
		{
			// Set up Music
			bgmusic.reset();
			bgmusic.setDataSource(tempFile.getAbsolutePath());
			bgmusic.prepare();

			// Do Notification Stuff

			Ui.playing.setText(tempFile.getName());
			bgmusic.start();
		}
		// Then it must be a video if it got into a playlist
		else
		{
			Ui.videoLayout.setVisibility(View.VISIBLE);
			Ui.player.setVideoPath(tempFile.getAbsolutePath());
			Ui.playing.setText(tempFile.getName());
			Ui.player.start();
		}

	}

}
