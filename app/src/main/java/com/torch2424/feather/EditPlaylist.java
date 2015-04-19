package com.torch2424.feather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Collections;

public class EditPlaylist extends Activity
{

	// for position in listview
	int itemPosition;
	// for toasts context
	Toasty toast;
	// list view adapter
	ArrayAdapter<String> adapter;
	// listview
	ListView listView;
	// listview Array
	String[] playList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);

		// Get our Toast
		toast = new Toasty(this);

		// Get our wallpaper
		getAppWallpaper();
		
		//Initialize listview(to avoid nullpointer)
		listView = (ListView) findViewById(R.id.listView);

		// create array
		// get size of playlist
		int size = Ui.bgMusic.playList.size();
		playList = new String[size];
		for (int i = 0; i < size; i++)
		{
			playList[i] = Ui.bgMusic.playList.get(i).getName();
		}

		// if playlist is empty show playlist empty text view
		if (size == 0)
		{
			listView.setVisibility(View.GONE);
		}
		else
		{
			TextView tv = (TextView) findViewById(R.id.noPlaylist);
			tv.setVisibility(View.GONE);
		}

		// Set Up adapter
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, playList);
		listView.setAdapter(adapter);

		// Get our clicks
		getItemClick();

	}

	// Getting the system wallpaper and setting it a the apps
		public void getAppWallpaper()
		{
			// Gotten from stack to set background as system background
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
			Drawable wallpaperDrawable = (Drawable) wallpaperManager.getDrawable();
			
			//Do cropping of the wallpaper so it doesn't look smushed
			Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
			
			//Get our display size
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			int height = size.y;
			
			//crop from center to screensize
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
			
			//Set wallpaper to bitmap
			wallpaperDrawable = new BitmapDrawable(getResources(),bitmap);
			
			// Darken
			wallpaperDrawable.setColorFilter(
					getResources().getColor(R.color.transparent),
					PorterDuff.Mode.SRC_ATOP);
			getWindow().setBackgroundDrawable(wallpaperDrawable);
		}

	// method for refreshing listview
	public void refresh()
	{
		// refresh playlist
		// create array
		// get size of playlist
		int size = Ui.bgMusic.playList.size();
		String[] playList = new String[size];
		for (int i = 0; i < size; i++)
		{
			playList[i] = Ui.bgMusic.playList.get(i).getName();
		}
		// dont sort, causes the playlist to display wrong
		// Arrays.sort(playList);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, playList);
	}

	private void getItemClick()
	{
		// alert dialog for long click
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// creating array for multiple items
		final String[] longOptions = { "Play", "Move Up", "Move Down", "Delete" };

		// listener for when someone clicks a file
		OnItemClickListener listclick = new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id)
			{
				// position starts at one
				itemPosition = position;
				builder.setTitle("What would you like to do?").setItems(
						longOptions, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int which)
							{
								// play
								if (which == 0)
								{
									// Change index to postion, and then play
									Ui.bgMusic.index = itemPosition;
									try
									{
										Ui.bgMusic.easyNext();
									}
									catch (IllegalStateException | IOException e)
									{
										//
										e.printStackTrace();
									}
								}
								// move up
								else if (which == 1)
								{
									if (itemPosition == 0)
									{
										toast.show("Cannot go any higher!");
									}
									else
									{
										// swapping files from one above
										Collections.swap(Ui.bgMusic.playList,
												itemPosition - 1, itemPosition);

										// playlist index need to change only if
										// playing
										if (Ui.bgMusic.index == itemPosition)
										{
											Ui.bgMusic.index--;
										}

										// refresh listview
										refresh();
										listView.setAdapter(adapter);
										listView.invalidateViews();
									}
								}
								// move down
								else if (which == 2)
								{
									if (itemPosition == Ui.bgMusic.playList
											.size() - 1)
									{
										toast.show("Cannot go any lower!");
									}
									else
									{
										// swapping files from one above
										Collections.swap(Ui.bgMusic.playList,
												itemPosition + 1, itemPosition);

										// playlist index need to change only if
										// playing
										if (Ui.bgMusic.index == itemPosition)
										{
											Ui.bgMusic.index++;
										}

										// refresh listview
										refresh();
										listView.setAdapter(adapter);
										listView.invalidateViews();
									}
								}
								// delete
								else
								{
									// check if it is last item in playlist
									if (Ui.bgMusic.playList.size() == 1)
									{
										toast.show("Cannot delete last item in playlist!");
									}
									else
									{

										// check if it is the file currently
										// playing, if it is, next
										if (Ui.bgMusic.index == itemPosition)
										{
											// in case they try to delete the
											// last file in playlist
											// while its playing
											if (Ui.bgMusic.index == Ui.bgMusic.playList
													.size() - 1)
											{
												// Previous
												--Ui.bgMusic.index;
												try
												{
													Ui.bgMusic.easyNext();
												}
												catch (IllegalStateException
														| IOException e)
												{
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											}
											else
											{
												// Next
												++Ui.bgMusic.index;
												try
												{
													Ui.bgMusic.easyNext();
												}
												catch (IllegalStateException
														| IOException e)
												{
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											}
										}

										// remove item and change listfile size
										Ui.bgMusic.playList
												.remove(itemPosition);
										Ui.bgMusic.index--;

										// refresh listview
										refresh();
										listView.setAdapter(adapter);
										listView.invalidateViews();

									}

								}
							}
						});
				// Create the Alert and return true for the case
				builder.create();
				builder.show();
			}

		};

		// set up the listener
		listView.setOnItemClickListener(listclick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_playlist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		// int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}
}
