package com.torch2424.feather;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;

public class FeatherSettings extends Activity
{

    // for toasts context
    Toasty toast;

    //initialize checkboxes and edittexts and booleans
    CheckBox checkSort;
    CheckBox checkHeadset;
    CheckBox checkList;
    boolean sortBool;
    boolean headsetBool;
    boolean listBool;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fsettings);

        // Get our Toast
        toast = new Toasty(this);

        // Get our wallpaper
        getAppWallpaper();

        //Do stuff from stats monitor to initialize our settings page and stuff
        //initialize checkboxes
        checkSort = (CheckBox) findViewById(R.id.musicSort);
        checkHeadset = (CheckBox) findViewById(R.id.checkHeadset);
        checkList = (CheckBox) findViewById(R.id.loopList);

        //get preferences for each box to set checked or not
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("MyPrefs", 0);
        sortBool = prefs.getBoolean("MUSICSORT", false);
        headsetBool = prefs.getBoolean("HEADSET", true);
        listBool = prefs.getBoolean("LOOPLIST", false);

        //Set the checkboxes to true or false
        if(sortBool) checkSort.setChecked(true);
        else checkSort.setChecked(false);
        if(headsetBool) checkHeadset.setChecked(true);
        else checkHeadset.setChecked(false);
        if(listBool) checkList.setChecked(true);
        else checkList.setChecked(false);
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

    //Function to save settings
    public void saveConfig(View view)
    {
        //getting every config and putting it in an boolean
        sortBool = checkSort.isChecked();
        headsetBool = checkHeadset.isChecked();
        listBool = checkList.isChecked();

        //used shared preferences to transfer data
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences("MyPrefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("MUSICSORT", sortBool);
        editor.putBoolean("HEADSET", headsetBool);
        editor.putBoolean("LOOPLIST", listBool);
        //Save changes
        editor.commit();

        //Toast and finish
        toast.show("Seetings saved!");
        finish();
    }


    /* DOnt need menu stuff, Settings Activity does not need settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fsettings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Dont need menu inside of settings page
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }/

        return super.onOptionsItemSelected(item);
    }
        */
}
