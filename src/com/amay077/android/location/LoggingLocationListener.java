package com.amay077.android.location;


import com.amay077.android.logging.Log;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class LoggingLocationListener extends TimeoutableLocationListener {
	private static TimeoutLisener timeoutListener = new TimeoutLisener() {

		public void onTimeouted(LocationListener sender) {
			Log.d("this.getClass().getSimpleName()", "onTimeouted called.");
		}
	};

	public LoggingLocationListener(LocationManager locaMan, long timeOutMS, String logFilePath) {
		super(locaMan, timeOutMS, timeoutListener);
	}

	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);

		StringBuilder builder = new StringBuilder();

		builder.append(location.getProvider());
		builder.append(",");
		builder.append(location.getLongitude());
		builder.append(",");
		builder.append(location.getLatitude());
		builder.append(",");
		builder.append(location.getAltitude());
		builder.append(",");
		builder.append(location.getAccuracy());
		builder.append(",");
		builder.append(location.getTime());

		Log.d("this.getClass().getSimpleName()", "onLocationChanged " + builder.toString());
	}
}
