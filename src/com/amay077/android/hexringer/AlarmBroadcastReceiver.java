package com.amay077.android.hexringer;

import com.amay077.android.hexringer.HexEnterLeaveNotifier.HexEnterLeaveListender;
import com.amay077.android.logging.Log;
import com.amay077.android.location.LoggingLocationListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

/** Broadcast Receiver */
public class AlarmBroadcastReceiver extends BroadcastReceiver
	implements HexEnterLeaveListender {
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
    	Log.d("AlarmBroadcastReceiver.onReceive", "called.");
    	Toast.makeText(context, "AlarmBroadcastReceiver.onReceive", Toast.LENGTH_SHORT).show();

		try {
			preference = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			lastHex = preference.getString(Const.PREF_KEY_LAST_HEX, null);
			String buf = preference.getString(Const.PREF_KEY_WATCH_HEXES, null);

			Log.d("AlarmBroadcastReceiver.onReceive", Const.PREF_KEY_WATCH_HEXES + " = " + buf);

			String[] watchHexes = StringUtil.toArray(buf, Const.ARRAY_SPLITTER);
			if (watchHexes == null || watchHexes.length == 0){
	        	Log.w("AlarmBroadcastReceiver.onReceive", Const.PREF_KEY_WATCH_HEXES + " not set.");
	        	return;
			}

			audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

	        String action = intent.getAction();
	        if (action.equals(Const.ACTION_HEXRINGAR_ALARM)) { // from AlarmManager
	        	// main sequence
	        	startHexEnterLeaveNotify(context, watchHexes);

	        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) { // booted phone.
	        	// Set Alarm to AlarmManager on boot
	        	// TODO: Need configuration
				Const.setAlarmManager(context);
	        } else {
	        	Log.w("AlarmBroadcastReceiver.onReceive", "not support intent action:" + action);
	        }
		} catch (Exception exp) {
			Log.w("AlarmBroadcastReceiver.onReceive", "failed.", exp);
		} finally {
			// Set next Alarm to AlarmManager
			Const.setAlarmManager(context);
			setResult(Activity.RESULT_OK, null, null);
		}
	}

	private void startHexEnterLeaveNotify(Context context, String[] watchHexes) {
		Log.d("AlarmBroadcastReceiver.startHexEnterLeaveNotify", "called.");
		locaMan = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

		locaMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_MS, 0,
				new HexEnterLeaveNotifier(locaMan, Const.LOCATION_REQUEST_TIMEOUT_MS, null,
						watchHexes, lastHex, this));

//		// FIXME : Debug only.
//		for (String provider : locaMan.getProviders(true)) {
//			Log.d("AlarmBroadcastReceiver.startHexEnterLeaveNotify", provider + " provider found.");
//			locaMan.requestLocationUpdates(provider, MIN_TIME_MS, 0,
//					new LoggingLocationListener(locaMan, Const.LOCATION_REQUEST_TIMEOUT_MS, "/HexRinger/" + provider + ".txt"));
//		}
	}

	public void onEnter(String enterHex) {
		Log.d("AlarmBroadcastReceiver.onEnter", enterHex);
		audioMan.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		writeLastHexToPreference(enterHex);
	}

	public void onLeave(String leaveHex) {
		Log.d("AlarmBroadcastReceiver.onLeave", leaveHex);
		audioMan.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		writeLastHexToPreference(null);
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

	public static class StringUtil {
		static public String[] toArray(String buf, String splitter) {
			if (StringUtil.isNullOrEmpty(buf)){
	        	throw new IllegalArgumentException("buf is null or empty.");
			}

			if (StringUtil.isNullOrEmpty(splitter)) {
	        	throw new IllegalArgumentException("splitter is null or empty.");
			}

			return buf.split(splitter);
		}

		static public String fromArray(String[] array, String splitter) {
			StringBuilder builder = new StringBuilder();
			boolean theFirst = true;
			for (String string : array) {
				if (!theFirst) {
					builder.append(splitter);
				} else {
					theFirst = false;
				}
				builder.append(string);
			}

			return builder.toString();
		}

		static public boolean isNullOrEmpty(String value) {
			return (value == null) || (value == "");
		}
	}

}
