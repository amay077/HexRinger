package com.amay077.android.hexringer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.amay077.android.logging.Log;
import com.amay077.android.preference.PreferenceWrapper;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Const {
	// Intent Action
	static public final String ACTION_HEXRINGAR_ALARM = "com.amay077.android.hexringer.ALARM";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED = "com.amay077.android.hexringer.LOCATION_CHANGED";

	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LAT = "latitude";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LONG = "longitude";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_ACCURACY = "accuracy";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_TIME = "time";

	static public final String TWITTER_CONSUMER_TOKEN = "your_token";
    static public final String TWITTER_CONSUMER_SECRET = "your_secret";



	/** 初回測位までのタイムアウト時間（ミリ秒） */
	static public final int LOCATION_REQUEST_TIMEOUT_MS = 60000;
	/** 配列を文字列化する時の区切り文字 */
	public static final String ARRAY_SPLITTER = ",";
	/** WiFi 測位かどうかの精度の閾値（ｍ） */
	public static final float LOCATION_MAX_ACCURACY = 1000;

	/** AlarmManager にインテント発行を設定する（今からｎ分後） */
	static public void setNextAlarm(Context context, int delay, boolean truncateMili) {
		PreferenceWrapper pref = new PreferenceWrapper(context);
		try {
			Intent intent = new Intent(context,
					AlarmBroadcastReceiver.class);

			intent.setAction(Const.ACTION_HEXRINGAR_ALARM);

			PendingIntent sender = PendingIntent.getBroadcast(
					context, 0, intent, 0);

			AlarmManager alarmManager = (AlarmManager) (context
					.getSystemService(Context.ALARM_SERVICE));

			// 次に実行する時刻を設定
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.MINUTE, delay);
			final long MINUTE = 1000 * 60;
			final long nextAlarmTime = truncateMili ? (long)(cal.getTimeInMillis() / MINUTE * MINUTE) : cal.getTimeInMillis();
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					nextAlarmTime,
					sender);

			pref.saveLong(R.string.pref_next_alarm_time_key, nextAlarmTime);

			showNotify(context, true);

			Log.d("Const", "setNextAlarm Alarm set at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(nextAlarmTime)));
		} catch (Exception e) {
			// TODO:PREF_KEY_ALARM_ENABLED を false にする
			Log.e("Const", "setNextAlarm Alarm set failed.", e);
			pref.remove(R.string.pref_alarm_enabled_key);
		}
	}

	static public void cancelAlarmManager(Context context) {
		try {
			Intent intent = new Intent(context,
					AlarmBroadcastReceiver.class);

			intent.setAction(Const.ACTION_HEXRINGAR_ALARM);

			PendingIntent sender = PendingIntent.getBroadcast(
					context, 0, intent, 0);

			AlarmManager alarmManager = (AlarmManager) (context
					.getSystemService(Context.ALARM_SERVICE));

			alarmManager.cancel(sender);

			PreferenceWrapper pref = new PreferenceWrapper(context);
			pref.remove(R.string.pref_next_alarm_time_key);

			NotificationManager notificationManager =
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();

			Log.d("Const", "cancelAlarmManager Alarm canceled.");
		} catch (Exception e) {
			Log.e("Const", "cancelAlarmManager Alarm cancel failed.", e);
		}
	}

	static public void showNotify(Context context, boolean showTicker) {
		try {
			PreferenceWrapper pref = new PreferenceWrapper(context);
			long nextMS = pref.getLong(R.string.pref_next_alarm_time_key, 0);
			if (nextMS == 0) {
				return;
			}

			SimpleDateFormat dtfmt = new SimpleDateFormat("H時mm分");
//			String lastLocaString = pref.getString(R.string.pref_last_location_key, "");
//			Location lastLocation = LocationUtil.fromString(lastLocaString);
			String prev = "";//lastLocation != null ? dtfmt.format(lastLocation.getTime()) + " に位置を確認しました。<br/>" : "";

			NotificationManager notificationManager =
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

			Notification notification = new Notification(
					R.drawable.hex_status,
					showTicker ? "位置を確認します" : "",
					System.currentTimeMillis());
			Intent notifyIntent = new Intent(context, MainActivity.class);
			//intentの設定
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
			notification.setLatestEventInfo(context.getApplicationContext(),
					"HexRinger",
					prev + "次回は " + dtfmt.format(nextMS) + " に確認します", contentIntent);
			//notification.flags = Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(R.string.app_name, notification);
		} catch (Exception e) {
			Log.w("Const", "showNotify() failed.", e);
		}
	}
}
