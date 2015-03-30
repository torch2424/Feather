package com.torch2424.feather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import com.torch2424.feather.BGMusic.MusicBinder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Ui extends Activity implements OnSeekBarChangeListener, Runnable
{
	// List view adapter
	ArrayAdapter<String> adapter;
	
	// Music Service
	static BGMusic bgMusic;
	Intent musicIntent;
	
	// Our Layout views
	static TextView playing;
	static TextView filePath;
	static TextView noVideo;
	static VideoView player;
	static RelativeLayout videoLayout;
	static Button playPause;
	
	// getting seekbar
	static SeekBar seekBar;
	static Handler seekHandler;
	static TextView currentDur;
	static TextView maxDur;
	static SimpleDateFormat duration;
	
	// Listview to display files
	ListView listView;
	
	// Files, browsedir = current dir/current file, directory we are browsing
	File browseDir;
	
	// to control volume for when someone long press volumes
	AudioManager audio;
	ComponentName musicControl;

    //Boolean and passed file for when app gives our app a file
    boolean passedFileBool;
    File passedFile;
	
	// Boolean for exiting app
	boolean exit;
	
	// Our toast
	public static Toasty toast;

    //Preferences for settings and stuff
    SharedPreferences prefs;

    //Context for service access
    //Handler used in bgmusic
    //Thread for loading dialogs
    static Context context;
    static Handler uiHandler;
    static Thread mThread;

	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);
		
		/*
		 * NOTES Service is controlling Manlyager, since it will outLive this
		 * activity Wakelock done by xml android:Keep Screen on, wakelocks only
		 * when video view is visible!
		 */

        // Our Toast
        toast = new Toasty(getApplicationContext());

        // Connect to our music service
        startMusic();

        // Initializing Our Views (and hiding some of them)
        playing = (TextView) findViewById(R.id.fileText);
        filePath = (TextView) findViewById(R.id.filePath);
        noVideo = (TextView) findViewById(R.id.noVideo);
        noVideo.setVisibility(View.GONE);
        player = (VideoView) findViewById(R.id.VideoView);
        videoLayout = (RelativeLayout) findViewById(R.id.VideoLayout);
        playPause = (Button) findViewById(R.id.playpause);
        videoLayout.setVisibility(View.GONE);

        // set up exit
        exit = false;

        // Is image and is apk done in Manly

        // ListFileBoolean?

        // setting up seekbar
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(false);
        seekHandler = new Handler();
        currentDur = (TextView) findViewById(R.id.currentDur);
        maxDur = (TextView) findViewById(R.id.maxDur);
        duration = new SimpleDateFormat("m:ss");

        // Create our listview
        listView = (ListView) findViewById(R.id.browserList);

        //Get default folder now user can choose
        prefs = this.getApplicationContext().getSharedPreferences("MyPrefs", 0);
        // default user folder, path to sd folder, root if not found
        String startPath;
        //Set a safe word "False" if user has not chosen a default folder yet
        String defaultPath = prefs.getString("DEFAULTPATH", "FALSE");
        //If it ids not the default safe word then they have their own path
        if (!defaultPath.contentEquals("FALSE")) {
            startPath = defaultPath;
        } else if (Environment.getExternalStorageDirectory().exists()) {
            startPath = (String) Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        } else {
            startPath = (String) Environment.getRootDirectory()
                    .getAbsolutePath();
        }
        // Set our Current directory to the external root
        browseDir = new File(startPath);

        // Assigning file path
        filePath.setText(browseDir.getAbsolutePath());

        // MAY NEED STATIC CONTEXT
        // assinging context for things that require it to be static
        // MainActivity.contextStatic = getApplicationContext();
        // regular context for loading dialog in our service
        context = this;
        //Handler for service to runonuithread in BGMUSIC
        uiHandler = new Handler();
        mThread = new Thread();

        // to help start long press on volume keys
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // register all media playback buttons to this app
        // Start listening for button presses
        // need to unregister this in quit
        musicControl = new ComponentName(getApplicationContext(),
                MusicControl.class);
        // Deprecated in Lollipop
        audio.registerMediaButtonEventReceiver(musicControl);

        // Video Wake Lock, get from TIH/ Going without Audio Focus

        // Make sure only affect media playback not ringer
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get UI Wallpaper
        getAppWallpaper();

        // Do Notification in service

        // Start/Refesh Listview
        // Set our adapter
        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1,
                Manly.getDirectoryArray(browseDir));
        listView.setAdapter(adapter);
        getItemClick();
        getLongClick();


        //Lastly check/prepare app for when a file is passed to the app
        passedFileBool = false;
        passedFile = null;
        Intent passed = getIntent();
        //If we did get an intent, and the data of it is not null
        if (passed != null && passed.getData() != null) {
            //Get the real file path from our uri
            //Simply make the file and play it!
            File playFile = null;
            if(passed.getData().getPath() != null)
            {
                try {
                    String realPath = getRealPath(passed.getData(), getApplicationContext().getContentResolver());
                    playFile = new File(realPath);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    toast.show("Could not get the file path from the app...");
                }
            }
            //If it is a file we can handle it, and if it is music or video we can handle it
            if (Manly.isMusic(playFile) || Manly.isVideo(playFile))
            {
                //Set our boolean and file, so that once music is bounded, it will play!
                passedFileBool = true;
                passedFile = playFile;
            }
            //Else we cannot handle it
            else
            {
                toast.show("Sorry, but Feather cannot play this...");
            }
        }
    }

        //Helpwer function to get real file path from uri, modified from stack
        private String getRealPath(Uri selectedVideoUri,
            ContentResolver contentResolver) throws UnsupportedEncodingException

        {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        String result = java.net.URLDecoder.decode(filePath, "UTF-8");
        return result;
        }
	
	// connect to the service
	ServiceConnection musicConnection = new ServiceConnection()
	{
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			MusicBinder binder = (MusicBinder) service;
			// get service
			bgMusic = binder.getService();

            //Check to see if we were passed file, if we were play it once music is binded
            if(passedFileBool)
            {
                // Clear the Playlist
                bgMusic.playList.clear();
                //Add the file
                bgMusic.playList.add(passedFile);
                // Set the playlist index
                bgMusic.index = 0;

                // Start the music or video
                try {
                    bgMusic.startMedia();
                } catch (IllegalArgumentException | SecurityException
                        | IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
                startSeekBar();
            }
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			
		}
	};
	
	public void startMusic()
	{
		// make sure only affect media playback not ringer
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// for pausing
		musicIntent = new Intent(getApplicationContext(), BGMusic.class);
		bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
		getApplicationContext().startService(musicIntent);
	}
	
	// Getting the system wallpaper and setting it a the apps
	public void getAppWallpaper()
	{
		// Gotten from stack to set background as system background
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		Drawable wallpaperDrawable = (Drawable) wallpaperManager.getDrawable();
		
		// Do cropping of the wallpaper so it doesn't look smushed
		Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
		
		// Get our display size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		
		// crop from center to screensize
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
		
		// Set wallpaper to bitmap
		wallpaperDrawable = new BitmapDrawable(getResources(), bitmap);
		
		// Darken
		wallpaperDrawable.setColorFilter(
				getResources().getColor(R.color.transparent),
				PorterDuff.Mode.SRC_ATOP);
		getWindow().setBackgroundDrawable(wallpaperDrawable);
	}
	
	// We call this to get our itemClick
	public void getItemClick()
	{
		// Get context here for zip Dialog
		final Context context = this;
		
		// listener for when someone clicks a file
		OnItemClickListener listclick = new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id)
			{
				// Get the file
				String selectedFile = (String) listView
						.getItemAtPosition(position);
				final File currentFile = new File(browseDir.getAbsolutePath()
						+ "/" + selectedFile);
				
				// If it is a directory
				if (currentFile.isDirectory())
				{
					traverseDirectory(currentFile);
				}
				// If it is a music or video
				else if (Manly.isMusic(currentFile)
						|| Manly.isVideo(currentFile))
				{
					
					// Auto create playlists should be segregated into music and
					// video
					boolean music;
					if (Manly.isMusic(currentFile)) music = true;
					else music = false;
					
					// Clear the Playlist
					bgMusic.playList.clear();
					
					// listing the files inside the directory
					String[] directory = browseDir.list();
					
					// Creating an int of how many files we add to the playlist,
					// and which one was the file
					// selected in this index
					int added = 0;
					int which = 0;
					
					// adding every file inside the folder
					for (int i = 0; i < directory.length; ++i)
					{
						File testFile = new File(browseDir.getAbsolutePath()
								+ "/" + directory[i]);
						if (!testFile.isDirectory())
						{
							// Music
							if (Manly.isMusic(testFile) && music)
							{
								// checking to see if it is the file selected
								if (testFile.getAbsolutePath().contentEquals(
										currentFile.getAbsolutePath()))
								{
									which = added;
								}
								
								added++;
								
								bgMusic.playList.add(testFile);
							}
							// Video
							else if (Manly.isVideo(testFile) && !music)
							{
								// checking to see if it is the file selected
								if (testFile.getAbsolutePath().contentEquals(
										currentFile.getAbsolutePath()))
								{
									which = added;
								}
								
								added++;
								
								bgMusic.playList.add(testFile);
							}
							
						}
					}
					
					// Set the playlist index
					bgMusic.index = which;
					
					// Start the music
					try
					{
						bgMusic.startMedia();
					}
					catch (IllegalArgumentException | SecurityException
							| IllegalStateException | IOException e)
					{
						e.printStackTrace();
					}
					startSeekBar();
				}
				// if it is an apk
				else if (Manly.isApk(currentFile))
				{
					// install apk with package installer
					Intent promptInstall = new Intent(Intent.ACTION_VIEW)
							.setDataAndType(
									Uri.parse("file://"
											+ currentFile.getAbsolutePath()),
									"application/vnd.android.package-archive");
					startActivity(promptInstall);
				}
				// if it is an image
				else if (Manly.isImage(currentFile))
				{
					// Open Image in a gallery app
					Intent promptOpen = new Intent(Intent.ACTION_VIEW)
							.setDataAndType(
									Uri.parse("file://"
											+ currentFile.getAbsolutePath()),
									"image/*");
					startActivity(promptOpen);
				}
				// If it is a zip
				else if (currentFile.getName().endsWith(".zip"))
				{
					AlertDialog.Builder delete = new AlertDialog.Builder(
							context);
					delete.setMessage(
							"Would You Like To Unzip this file? If a folder exists with the file name, it may"
									+ " overwrite files of the same name inside of the .zip file!")
							.setNegativeButton("No",
									new DialogInterface.OnClickListener()
									{
										public void onClick(
												DialogInterface dialog, int id)
										{
											// Cancelled
										}
									})
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener()
									{
										public void onClick(
												DialogInterface dialog, int id)
										{
											// Show Loading
											final ProgressDialog loading = ProgressDialog
													.show(Ui.this,
															"",
															"Extracting "
																	+ currentFile
																			.getName()
																	+ ", this may take a "
																	+ "while depending on .zip size."
																	+ " Please wait...",
															true);
											// Unzip to a folder of the same
											// name on a new thread
											
											final String extractDir = currentFile
													.getAbsolutePath().replace(
															".zip", "");
											mThread = new Thread()
											{
												@Override
												public void run()
												{
													try
													{
														Manly.unZipFile(
																currentFile,
																new File(
																		extractDir));
													}
													catch (IOException e)
													{
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}
													
													// Need to do these things
													// after we finish unzipping
													runOnUiThread(new Runnable()
													{
														public void run()
														{
															// Reload
															traverseDirectory(browseDir);
															loading.dismiss();
															
															// Toast that it was
															// extracted, and
															// give directory
															toast.show("Finished extracting! Extracted to: "
																	+ extractDir);
														}
													});
												}
											};
											mThread.start();
										}
									});
					// Create the AlertDialog object and return
					// it
					delete.create();
					delete.show();
				}
				// Else we cant handle that type of file
				else
				{
					toast.show("Cannot play the selected file type...");
				}
			}
		};
		
		// set up the listener
		listView.setOnItemClickListener(listclick);
	}
	
	// Function to get our long click actions
	public void getLongClick()
	{
		// Get context here for Dialog
		final Context context = this;
		OnItemLongClickListener longClick = new OnItemLongClickListener()
		{
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id)
			{
				// Get our files for our actions
				String selectedFile = (String) listView
						.getItemAtPosition(position);
				final File folderDir = new File(browseDir.getAbsolutePath()
						+ "/" + selectedFile);
				
				// Get our alert dialogs
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				String[] longOptions = { "Play Music in Folder",
						"Play Video in Folder", "Add to Playlist", "Set Default Directory", "Delete" };
				
				builder.setTitle("What would you like to do?").setItems(
						longOptions, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int which)
							{
								// The 'which' argument contains the index
								// position
								// of the selected item
								
								// play music or video
								if (which == 0 || which == 1)
								{
									if (folderDir.isDirectory())
									{
										// making playlist fresh
										bgMusic.playList.clear();
										
										// Get our playlist depending on music
										// or video
										// 0 is music, else it is 1(video)
										if (which == 0) bgMusic.getPlaylist(
												folderDir, bgMusic.playList,
												true);
										else bgMusic.getPlaylist(folderDir,
												bgMusic.playList, false);
										
										//Sorting done in bgMusic
										
										// Resetting index
										bgMusic.index = 0;
										
										// playing playlist, only if it isn't
										// empty
										if (!bgMusic.playList.isEmpty())
										{
											try
											{
												bgMusic.startMedia();
											}
											catch (IllegalArgumentException
													| SecurityException
													| IllegalStateException
													| IOException e)
											{
												e.printStackTrace();
											}
											startSeekBar();
										}
										else
										{
											if (which == 0) toast
													.show("No music found...");
											else toast
													.show("No video found...");
										}
										
									}
									else
									{
										toast.show("This is not a folder!");
									}
								}
								// add to playlist
								else if (which == 2)
								{
									if (folderDir.isDirectory())
									{
										ArrayList<File> tempArray;
										// get the playlist, deending if it is a
										// music or video playlist
										if (Manly.isMusic(bgMusic.playList
												.get(0)))
										{
											tempArray = bgMusic
													.getPlaylist(
															folderDir,
															new ArrayList<File>(),
															true);
										}
										else
										{
											tempArray = bgMusic.getPlaylist(
													folderDir,
													new ArrayList<File>(),
													false);
										}
										
										//Sorting and shuffling done in bg music

										// Finally add to the end of our current
										// playlist
										bgMusic.playList.addAll(tempArray);
										
										// Toast
										toast.show("Added to playlist!");
									}
									else
									{
										if (Manly.isMusic(folderDir)
												|| Manly.isVideo(folderDir))
										{
											bgMusic.playList.add(folderDir);
											toast.show("Added to playlist!");
										}
										else
										{
											toast.show("This file can not be added to the playlist.");
										}
									}
									
									// if nothing is playing while you add to
									// playlist, you cant play it
									// so we gotta play if nothing is playing
									// if you ad stuff to playlist while music
									// isn't playing
									// it never plays, so need to play if not
									// playing
									if (bgMusic.isPlaying())
									{
										
									}
									else if (player.isShown())
									{
										
									}
									else if (bgMusic.playList.size() > 0)
									{
										try
										{
											bgMusic.startMedia();
										}
										catch (IllegalArgumentException
												| SecurityException
												| IllegalStateException
												| IOException e)
										{
											//
											e.printStackTrace();
										}
									}
								}
                                // set default folder
                                else if (which == 3)
                                {
                                    // not using this for context since was
                                    // throwing error
                                    AlertDialog.Builder delete = new AlertDialog.Builder(
                                            context);
                                    delete.setMessage(
                                            "This will set the default directory for when you open the app, are you sure?")
                                            .setNegativeButton(
                                                    "Yes",
                                                    new DialogInterface.OnClickListener()
                                                    {
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int id)
                                                        {
                                                            //If it is a folder, then set it as default
                                                            if (folderDir
                                                                    .isDirectory())
                                                            {
                                                                //Save the path
                                                                SharedPreferences.Editor editor = prefs.edit();
                                                                editor.putString("DEFAULTPATH", folderDir.getAbsolutePath());
                                                                editor.commit();
                                                                toast.show("Default folder successfully set!");

                                                            }
                                                            else
                                                            {
                                                                //This is not a folder, cannot set as directory
                                                                toast.show("This is not a folder, cannot be set as a directory!");
                                                            }
                                                        }
                                                    })
                                            .setPositiveButton(
                                                    "Cancel",
                                                    new DialogInterface.OnClickListener()
                                                    {
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int id)
                                                        {
                                                            // cancelled
                                                        }
                                                    });
                                    // Create the AlertDialog object and return
                                    // it
                                    delete.create();
                                    delete.show();
                                }
								// delete
								else if (which == 4)
								{
									// not using this for context since was
									// throwing error
									AlertDialog.Builder delete = new AlertDialog.Builder(
											context);
									delete.setMessage(
											"Are you sure? If it is a folder, it will "
													+ "delete its contents as well!")
											.setNegativeButton(
													"Delete",
													new DialogInterface.OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int id)
														{
															// since you need to
															// clear a folder
															// before deleting
															// have to call
															// function to
															// delete everything
															// inside
															if (folderDir
																	.isDirectory())
															{
																Manly.deleteDirectory(folderDir);
																folderDir
																		.delete();
																traverseDirectory(browseDir);
															}
															else
															{
																// delete folder
																// And reset
																folderDir
																		.delete();
																traverseDirectory(browseDir);
															}
														}
													})
											.setPositiveButton(
													"Cancel",
													new DialogInterface.OnClickListener()
													{
														public void onClick(
																DialogInterface dialog,
																int id)
														{
															// cancelled
														}
													});
									// Create the AlertDialog object and return
									// it
									delete.create();
									delete.show();
								}
								
							}
						});
				// Create the Alert and return true for the case
				builder.create();
				builder.show();
				return true;
			}
			
		};
		listView.setOnItemLongClickListener(longClick);
	}
	
	public void traverseDirectory(File directory)
	{
		// Null check
		if (Manly.getDirectoryArray(directory) != null)
		{
			// Go one File Deeper
			browseDir = directory;
			filePath.setText(directory.getAbsolutePath());
            /* very slow right now, can implement later in settings



            //Check if we want the files sorted by track number or alpahbetically
            if(prefs.getBoolean("MUSICSORT", false))
            {
                //track number
                //Get our array of files
                File[] mediaArray = Manly.getUnsortedFileArray(browseDir);
                //Sort them
                Arrays.sort(mediaArray, new SongComparator());

                //Now convert to string array
                String[] stringArray = new String[mediaArray.length];
                for(int i = 0; i < mediaArray.length; ++i)
                {
                    stringArray[i] = mediaArray[i].getName();
                }

                //Finally set the adapter
                adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        stringArray);

            }
            else
            {
                //alphabetically
                adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1,
                        Manly.getDirectoryArray(browseDir));
            }

            */

            //alphabetically
            adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1,
                    Manly.getDirectoryArray(browseDir));
			listView.setAdapter(adapter);
			listView.invalidateViews();
		}
		else
		{
			// inform user we cant go in there
			toast.show("Cannot access the directory...");
		}
	}
	
	public void startSeekBar()
	{
		seekHandler.removeCallbacks(this);
		seekBar.setEnabled(true);
		seekHandler.postDelayed(this, 1000);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser)
	{
		try
		{
			if (player.isShown())
			{
				if (fromUser)
				{
					// seek to that time in textview
					Date seekTime = new Date(progress);
					currentDur.setText(duration.format(seekTime));
					
					player.seekTo(progress);
					
				}
			}
			else if (bgMusic.isPlaying() || bgMusic != null)
			{
				if (fromUser)
				{
					// seek to that time in text view
					Date seekTime = new Date(progress);
					currentDur.setText(duration.format(seekTime));
					
					bgMusic.seek(progress);
				}
			}
			
			else
			{
				seekBar.setProgress(0);
			}
		}
		catch (Exception e)
		{
			
		}
		
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		//
		
	}
	
	// runnable for seekbar
	// http://android-mantra.blogspot.com/2013/09/seekbar-for-music-player.html
	@Override
	public void run()
	{
		int currentPosition = 0;
		int total = 0;
		
		if (bgMusic.isPlaying())
		{
			currentPosition = bgMusic.getPosition();
			total = bgMusic.getDuration();
			
			Date curDur = new Date(currentPosition);
			Date totalDur = new Date(total);
			currentDur.setText(duration.format(curDur));
			maxDur.setText(duration.format(totalDur));
			
			seekBar.setMax(total);
			seekBar.setProgress(currentPosition);
		}
		else if (player.isPlaying())
		{
			currentPosition = player.getCurrentPosition();
			total = player.getDuration();
			
			Date curDur = new Date(currentPosition);
			Date totalDur = new Date(total);
			currentDur.setText(duration.format(curDur));
			maxDur.setText(duration.format(totalDur));
			
			seekBar.setMax(total);
			seekBar.setProgress(currentPosition);
		}
		
		if (seekBar.isEnabled())
		{
			seekHandler.postDelayed(this, 1000);
		}
		else
		{
			seekBar.setProgress(total);
			seekHandler.removeCallbacks(this);
		}
	}
	
	/*
	 * BUTTONS, Static for Background reciever
	 */
	
	public static void next(View view)
	{
		
		// Did the playlist end?
		if (bgMusic.index + 1 >= bgMusic.playList.size())
		{
			toast.show("No next File!");
		}
		else
		{
			if (bgMusic.isLooping())
			; // Do Nothing?
			else ++bgMusic.index; // increment index
			
			// Open Next File
			try
			{
				bgMusic.easyNext();
			}
			catch (IllegalStateException | IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void prev(View view)
	{
		
		// Did the playlist end?
		if (bgMusic.index <= 0)
		{
			toast.show("No previous File!");
		}
		else
		{
			if (bgMusic.isLooping())
			; // Do Nothing?
			else --bgMusic.index; // increment index
			
			// Open Next File
			try
			{
				bgMusic.easyNext();
			}
			catch (IllegalStateException | IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void playPause(View view)
	{
		// Music and then video
		// Switch lay pause to switch Buttons
		if (bgMusic.isPlaying())
		{
			bgMusic.pauseSong();
			// bgMusic.switchPlayPause(false);
		}
		else if (bgMusic.isPaused())
		{
			bgMusic.playSong();
			// bgMusic.switchPlayPause(true);
		}
		else if (player.isPlaying())
		{
			player.pause();
			// bgMusic.switchPlayPause(false);
		}
		else if (player.getVisibility() == View.VISIBLE && !player.isPlaying())
		{
			player.start();
			// bgMusic.switchPlayPause(true);
		}
		// no File is selected
		else
		{
			toast.show("No file selected!");
		}
		
	}
	
	// Dont Think Media buttons are needed in activity, since background recever
	// handles them
	
	/*
	 * CONFIGURATION CHANGE
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		ListView list = (ListView) findViewById(R.id.browserList);
		LinearLayout buttons = (LinearLayout) findViewById(R.id.Buttons);
		videoLayout = (RelativeLayout) findViewById(R.id.VideoLayout);
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			// hide everything else
			list.setVisibility(View.GONE);
			buttons.setVisibility(View.GONE);
			filePath.setVisibility(View.GONE);
			
			// now make fullscreen, hide action bar
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getActionBar().hide();
			
			if (player.isShown() == false)
			{
				noVideo.setVisibility(View.VISIBLE);
			}
			else
			{
				// First Get the Height and width of video for better portrait
				// full screen
				MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
				metaRetriever.setDataSource(bgMusic.getCurrentSong()
						.getAbsolutePath());
				String height = metaRetriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
				String width = metaRetriever
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
				// Gets the layout params that will allow you to resize the
				// layout
				LayoutParams params = videoLayout.getLayoutParams();
				RelativeLayout.LayoutParams video = (android.widget.RelativeLayout.LayoutParams) player
						.getLayoutParams();
				// Changes the height and width to the specified *pixels*
				params.height = LayoutParams.MATCH_PARENT;
				video.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				video.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				// Only stretch left and right if landscape video
				if (Integer.valueOf(width) >= Integer.valueOf(height))
				{
					video.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					video.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				}
				videoLayout.setLayoutParams(params);
				player.setLayoutParams(video);
			}
			
			// setContentView(R.layout.activity_main);
			// requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		else
		{
			if (noVideo.isShown())
			{
				noVideo.setVisibility(View.GONE);
			}
			else
			{
				// Gets the layout params that will allow you to resize the
				// layout
				LayoutParams params = videoLayout.getLayoutParams();
				RelativeLayout.LayoutParams video = (android.widget.RelativeLayout.LayoutParams) player
						.getLayoutParams();
				// Changes the height and width to the specified *pixels*
				params.height = 200;
				// to remove the rule, just comma then zero
				video.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
				video.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
				video.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
				video.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
				videoLayout.setLayoutParams(params);
				player.setLayoutParams(video);
			}
			// restore everything
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			list.setVisibility(View.VISIBLE);
			buttons.setVisibility(View.VISIBLE);
			filePath.setVisibility(View.VISIBLE);
			
			// Show action bar
			getActionBar().show();
		}
	}
	
	/*
	 * MENU/SETTINGS
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ui, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.playlistActivity)
		{
			Intent listIntent = new Intent(this, EditPlaylist.class);
			startActivity(listIntent);
			return true;
		}
        else if (id == R.id.settings)
        {
            Intent listIntent = new Intent(this, FeatherSettings.class);
            startActivity(listIntent);
            return true;
        }
		else if (id == R.id.quit)
		{
			quit();
			return true;
		}
		else if (id == R.id.shuffle)
		{
			if (bgMusic.shuffleBool)
			{
				toast.show("Shuffle is off!");
				bgMusic.shuffleBool = false;
				bgMusic.sort(bgMusic.getCurrentSong());
			}
			else
			{
				toast.show("Shuffle is on!");
				bgMusic.shuffleBool = true;
				bgMusic.shuffle(bgMusic.getCurrentSong());
			}
		}
		else if (id == R.id.loop)
		{
			if (bgMusic.isLooping())
			{
				toast.show("Loop is off!");
				bgMusic.changeLooping();
			}
			else
			{
				toast.show("Loop is on!");
				bgMusic.changeLooping();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	// @override overrides what the back button normally does on back pressed is
	// the button function
	@Override
	public void onBackPressed()
	{
		// your code when back button pressed
		// If no parent directory
		if (browseDir.getParentFile() == null)
		{
			// use boolean to implement press back twice to exit
			if (exit)
			{
				quit();
			}
			else if (exit == false)
			{
				exit = true;
				toast.show("Press back again to exit");
			}
		}
		else
		{
			// get the file name, then remove it from the array list
			browseDir = new File(browseDir.getParentFile().getAbsolutePath());
			filePath.setText(browseDir.getAbsolutePath());
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1,
					Manly.getDirectoryArray(browseDir));
			listView.setAdapter(adapter);
			listView.invalidateViews();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void quit()
	{
		// Stop the seekbar and toast
		seekHandler.removeCallbacks(this);
		toast.show("Thank you for using Feather!");
		
		seekHandler.removeCallbacks(this);
		if (bgMusic.isPlaying())
		{
			bgMusic.stopSong();
		}
		else if (player.isPlaying())
		{
			player.stopPlayback();
		}
		
		// quit the service
		stopService(musicIntent);
		
		// Not doing audio focus
		// audio.abandonAudioFocus(afChangeListener);
		
		// Kill Background receiver
		audio.unregisterMediaButtonEventReceiver(musicControl);
		finish();
	}
	
	// stop the app
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		quit();
	}
}
