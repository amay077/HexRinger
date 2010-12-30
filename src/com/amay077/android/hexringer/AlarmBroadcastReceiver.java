package com.amay077.android.hexringer;

import java.io.File;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import com.amay077.android.logging.Log;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.PatternFormatter;

import net.geohex.GeoHex;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
	static private final int MIN_TIME_MS = 1000;

	private SharedPreferences preference = null;
	private String lastHex = null;
	private AudioManager audioMan = null;
	private LocationManager locaMan = null;

	/**
	 * Receive ALARM broadcast from AlarmManager
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
    	Log.d("AlarmBroadcastReceiver", "onReceive");
    	Toast.makeText(context, "AlarmBroadcastReceiver.onReceive", Toast.LENGTH_SHORT).show();

		try {
			preference = PreferenceManager.getDefaultSharedPreferences(context);
			lastHex = preference.getString(Const.PREF_KEY_LAST_HEX, null);

			audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

	        if (!intent.hasExtra(Const.EXTRA_GEOHEXES)) {
	        	Log.w("AlarmBroadcastReceiver", Const.EXTRA_GEOHEXES + " not found.");
	        	return;
	        }

	        String action = intent.getAction();
	        String[] geoHexes = intent.getStringArrayExtra(Const.EXTRA_GEOHEXES);

	        if (geoHexes == null || geoHexes.length == 0) {
	        	Log.w("AlarmBroadcastReceiver", Const.EXTRA_GEOHEXES + " is length zero.");
	        	return;
	        }

	        if (action.equals(Const.ACTION_HEXRINGAR_ALARM)) { // from AlarmManager

	        	// main sequence
	        	startAreaCheck(context, geoHexes);

	        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) { // booted phone.
	        	// Set Alarm to AlarmManager on boot
	        	// TODO: Need configuration
				Const.setAlarmManager(context);
	        }
		} catch (Exception exp) {
			Log.w("AlarmBroadcastReceiver", "failed.", exp);
		} finally {
			// Set next Alarm to AlarmManager
			Const.setAlarmManager(context);
			setResult(Activity.RESULT_OK, null, null);
		}
	}

	private void startAreaCheck(Context context, final String[] geoHexes) {
		locaMan = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();

		for (String provider : locaMan.getProviders(true)) {
			Log.d("startAreaCheck", provider + " provider found.");
			locaMan.requestLocationUpdates(provider, MIN_TIME_MS, 0,
					new MyLocationListener(provider));
		}
	}

	private GeoHex.Zone[] getIntersectGeoHexes(String[] geoHexes, Location location) {
		SortedSet<GeoHex.Zone> intersectsZones = new TreeSet<GeoHex.Zone>();
		for (String geoHex : geoHexes) {
			GeoHex.Zone zone = GeoHex.decode(geoHex);

			double x = 0d, y = 0d, radius = 0d;
			if (zone.intersects(x, y, radius)) {
				intersectsZones.add(zone);
			}
		}

		return (GeoHex.Zone[])intersectsZones.toArray();
	}

	private class MyLocationListener implements LocationListener {
		private Timer timerTimeout = new Timer();
		private Logger logger = LoggerFactory.getLogger();
		private String provider;

		private void initialize() {
			final String LOGFILE_PATH = "/HexRinger/" + provider + ".txt";

			File sdCardDir = Environment.getExternalStorageDirectory();
			Uri logUri = Uri.withAppendedPath(Uri.fromFile(sdCardDir), LOGFILE_PATH);
			String logFullPath = logUri.getPath();

			File logDir = new File(logFullPath).getParentFile();
			if (!logDir.exists()) {
				logDir.mkdir();
			}

			logger.setClientID(provider);

			// Formatter
			PatternFormatter formatter = new PatternFormatter();
			formatter.setPattern("%m");

			// LogCatAppender
			LogCatAppender logCatAppender = new LogCatAppender();
			logCatAppender.setFormatter(formatter);
			logger.addAppender(logCatAppender);

			// FileAppender
			FileAppender fileAppender = new FileAppender();
			fileAppender.setFileName(LOGFILE_PATH);
			fileAppender.setAppend(true);
			fileAppender.setFormatter(formatter);
			logger.addAppender(fileAppender);
		}


		public MyLocationListener(String provider) {
			this.provider = provider;
			initialize();
			timerTimeout.schedule(new TimerTask() {

				@Override
				public void run() {
					stopLocationUpdateAndTimer();
				}
			}, Const.LOCATION_REQUEST_TIMEOUT_MS);
		}


		public void onStatusChanged(String provider, int status, Bundle extras) { }

		public void onProviderEnabled(String provider) { }

		public void onProviderDisabled(String provider) { }


		public void onLocationChanged(Location location) {
			StringBuilder builder = new StringBuilder();

			builder.append(location.getLongitude());
			builder.append(",");
			builder.append(location.getLatitude());
			builder.append(",");
			builder.append(location.getAltitude());
			builder.append(",");
			builder.append(location.getAccuracy());
			builder.append(",");
			builder.append(location.getTime());

			logger.debug(builder.toString());

			stopLocationUpdateAndTimer();

			// Valid location (WiFi location big changes, Hardware bug, etc...)
			// if (!vaildLocation()) return;

//			// Get hit hexes in current location and accuracy, order by nearby
//			GeoHex.Zone[] hitHexes = getIntersectGeoHexes(geoHexes, location);
//			String hitHex = hitHexes.length > 0 ? hitHexes[0].code : null;
//
//			if (lastHex == null) {
//				if (hitHex == null) {
//					// Out to out. Do nothing.
//				} else {
//					// Out to in. Enter.
//					enterHex(hitHex);
//				}
//			} else {
//				if (lastHex == hitHex) {
//					// In to in. Do nothing.
//				} else if (hitHex == null) {
//					// In to out. Leave.
//					leaveHex(lastHex);
//				} else {
//					// Hex内→Hex外→別Hex内
//					// In -> out -> in other hex. Leave and enter.
//					leaveAndEnterHex(lastHex, hitHex);
//				}
//			}
		}

		private void stopLocationUpdateAndTimer() {
			locaMan.removeUpdates(this);

			timerTimeout.cancel();
			timerTimeout.purge();
			timerTimeout = null;
		}

		private void enterHex(String hitHex) {
			audioMan.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			writeLastHexToPreference(hitHex);
		}
		private void leaveHex(String lastHex) {
			audioMan.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			writeLastHexToPreference(null);
		}
		private void leaveAndEnterHex(String lastHex, String hitHex) {
			audioMan.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			writeLastHexToPreference(hitHex);
		}

		private void writeLastHexToPreference(String hitHex) {
			Editor editor = preference.edit();
			if (hitHex == null) {
				editor.remove(Const.PREF_KEY_LAST_HEX);
			} else {
				editor.putString(Const.PREF_KEY_LAST_HEX, hitHex);
			}
			editor.commit();
		}

	}
}
