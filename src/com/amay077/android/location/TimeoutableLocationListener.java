package com.amay077.android.location;

import java.util.Timer;
import java.util.TimerTask;

import com.amay077.android.logging.Log;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class TimeoutableLocationListener implements LocationListener {
	protected Timer timerTimeout = new Timer();
	protected LocationManager locaMan = null;

	public TimeoutableLocationListener(LocationManager locaMan, long timeOutMS, final TimeoutLisener timeoutListener) {
		Log.d("TimeoutableLocationListener.ctor", "called.");
		this.locaMan  = locaMan;
		timerTimeout.schedule(new TimerTask() {

			@Override
			public void run() {
				if (timeoutListener != null) {
					timeoutListener.onTimeouted(TimeoutableLocationListener.this);
				}
				stopLocationUpdateAndTimer();
			}
		}, timeOutMS);
		Log.d("TimeoutableLocationListener.timerTimeout", "started.");
	}


//	@Override
	public void onLocationChanged(Location location) {
		Log.d("TimeoutableLocationListener.onLocationChanged", "called.");
		stopLocationUpdateAndTimer();
	}

//	@Override
	public void onProviderDisabled(String s) { }

//	@Override
	public void onProviderEnabled(String s) { }

//	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) { }

	private void stopLocationUpdateAndTimer() {
		Log.d("TimeoutableLocationListener.stopLocationUpdateAndTimer", "called.");
		locaMan.removeUpdates(this);

		timerTimeout.cancel();
		timerTimeout.purge();
		timerTimeout = null;
	}

	public interface TimeoutLisener {
		void onTimeouted(LocationListener sender);
	}

}
