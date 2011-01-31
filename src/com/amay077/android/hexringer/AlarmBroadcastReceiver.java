package com.amay077.android.hexringer;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;

import com.amay077.android.hexringer.HexEnterLeaveNotifier.HexEnterLeaveListender;
import com.amay077.android.logging.Log;
import com.amay077.android.preference.PreferenceWrapper;
import com.amay077.android.twitter.AuthInfo;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.widget.Toast;

/** Broadcast Receiver */
public class AlarmBroadcastReceiver extends BroadcastReceiver
	implements HexEnterLeaveListender {
	static private final int MIN_TIME_MS = 1000;

	private PreferenceWrapper pref = null;
	private String lastHex = null;
	private AudioManager audioMan = null;
	private LocationManager locaMan = null;
	private Context context = null;

	/**
	 * Receive ALARM broadcast from AlarmManager
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Log.d(this.getClass().getSimpleName(), "onReceive() called.");
	    	Toast.makeText(context, "AlarmBroadcastReceiver.onReceive", Toast.LENGTH_SHORT).show();

	    	this.context = context;

	    	pref = new PreferenceWrapper(context.getApplicationContext());
			lastHex = pref.getString(R.string.pref_last_hex_key, null);
			String buf = pref.getString(R.string.pref_watch_hexes_key, null);

			Log.d(this.getClass().getSimpleName(), "onReceive() watch hexes = " + buf);

			String[] watchHexes = StringUtil.toArray(buf, Const.ARRAY_SPLITTER);
			if (watchHexes == null || watchHexes.length == 0){
	        	Log.w(this.getClass().getSimpleName(), "onReceive() watch hexes not set.");
	        	return;
			}

			audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

	        String action = intent.getAction();
	        if (action.equals(Const.ACTION_HEXRINGAR_ALARM)) { // from AlarmManager
	        	// main sequence
	        	beginHexEnterLeaveNotify(watchHexes);

	        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) { // booted phone.
	        	// Set Alarm to AlarmManager on boot
	        	// TODO: Need configuration
				Const.setNextAlarm(context, pref.getAsInt(R.string.pref_watchinterval_key,
						context.getString(R.string.pref_watchinterval_default)));
	        } else {
	        	Log.w(this.getClass().getSimpleName(), "onReceive() " + "not support intent action:" + action);
	        }
		} catch (Exception exp) {
			Log.w(this.getClass().getSimpleName(), "onReceive() failed.", exp);
		} finally {
			// Set next Alarm to AlarmManager
			Const.setNextAlarm(context, pref.getAsInt(R.string.pref_watchinterval_key,
					context.getString(R.string.pref_watchinterval_default)));
			setResult(Activity.RESULT_OK, null, null);
		}
	}

	private void beginHexEnterLeaveNotify(String[] watchHexes) {
		Log.d(this.getClass().getSimpleName(), "beginHexEnterLeaveNotify() called.");
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
		try {
			Log.d(this.getClass().getSimpleName(), "onEnter() " + enterHex);
	    	Toast.makeText(context, "AlarmBroadcastReceiver.onEnter:" + enterHex, Toast.LENGTH_SHORT).show();
			audioMan.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			writeLastHexToPreference(enterHex);
			Log.d(this.getClass().getSimpleName(), "onEnter() set ringermode normal.");

			tweet("enter hex:" + enterHex + ". set ringermode normal. #HexRinger");

		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "onEnter() failed.", e);
		}
	}

	private void tweet(String message) {

		try	{
	        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
	        confbuilder.setOAuthConsumerKey(Const.TWITTER_CONSUMER_TOKEN);
	        confbuilder.setOAuthConsumerSecret(Const.TWITTER_CONSUMER_SECRET);
			TwitterFactory twitterfactory = new TwitterFactory(confbuilder.build());
			AuthInfo info = new AuthInfo(context.getString(R.string.pref_twitter_key));
	        Twitter twitter = twitterfactory.getOAuthAuthorizedInstance(
	        		new AccessToken(info.consumerToken, info.consumerSecret));

	        twitter.updateStatus(message);
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "tweet() failed.", e);
		}
	}

	public void onLeave(String leaveHex) {
		Log.d(this.getClass().getSimpleName(), "onLeave() " + leaveHex);
    	Toast.makeText(context, "AlarmBroadcastReceiver.onLeave:" + leaveHex, Toast.LENGTH_SHORT).show();
		audioMan.setRingerMode(pref.getAsInt(R.string.pref_mannermode_type_key,
				context.getString(R.string.pref_mannermode_type_default)));
		writeLastHexToPreference(null);
		Log.d(this.getClass().getSimpleName(), "onLeave() set ringermode manner.");

		tweet("leave hex:" + leaveHex + ". set ringermode manner. #HexRinger");
	}

	private void writeLastHexToPreference(String hitHex) {
		if (hitHex == null) {
			pref.remove(R.string.pref_last_hex_key);
		} else {
			pref.saveString(R.string.pref_last_hex_key, hitHex);
		}
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

	@Override
	protected void finalize() throws Throwable {
		Log.d(this.getClass().getSimpleName(), "finalize() called.");
		super.finalize();
	}
}
