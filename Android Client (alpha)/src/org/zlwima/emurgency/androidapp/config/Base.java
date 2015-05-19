package org.zlwima.emurgency.androidapp.config;

import android.util.Log;

public class Base{
	
	public static final String TAG = "EMR";
	
	/**
	 * Log messages in debug mode. Skip in production
	 * @param msg Message to log
	 */
	public static void Log(String msg) {
		Log.d(TAG, msg);
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, msg);
		}
	}
	
}
