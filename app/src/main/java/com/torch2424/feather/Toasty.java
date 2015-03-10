package com.torch2424.feather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class Toasty
{
	private Toast toast;
	
	public Toasty(Context context)
	{
		toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
	}
	
	public void show(String string)
	{
		toast.setText(string);
		toast.show();
	}
}
