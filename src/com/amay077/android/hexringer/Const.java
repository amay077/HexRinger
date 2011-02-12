package com.amay077.android.hexringer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.amay077.android.hexringer.AlarmBroadcastReceiver.LocationUtil;
import com.amay077.android.logging.Log;
import com.amay077.android.preference.PreferenceWrapper;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

public class Const {
	// Intent Action
	static public final String ACTION_HEXRINGAR_ALARM = "com.amay077.android.hexringer.ALARM";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED = "com.amay077.android.hexringer.LOCATION_CHANGED";

	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LAT = "latitude";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_LONG = "longitude";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_ACCURACY = "accuracy";
	static public final String ACTION_HEXRINGAR_LOCATION_CHANGED_EXTRA_TIME = "time";

	static public final String TWITTER_CONSUMER_TOKEN = "eIyOFT2k0p7YVGWhDFJJA";
    static public final String TWITTER_CONSUMER_SECRET = "8G3i98Q3f76SZ1SlkfN8ch8SX4QKWEIuNge6tQdHs";



	/** 初回測位までのタイムアウト時間（ミリ秒） */
	static public final short LOCATION_REQUEST_TIMEOUT_MS = 30000;
	/** 配列を文字列化する時の区切り文字 */
	public static final String ARRAY_SPLITTER = ",";
	/** WiFi 測位かどうかの精度の閾値（ｍ） */
	public static final float LOCATION_MAX_ACCURACY = 1000;

	/** AlarmManager にインテント発行を設定する（今からｎ分後） */
	static public void setNextAlarm(Context context, int delay, boolean truncateMili) {
		try {
			Intent intent = new Intent(context,
					AlarmBroadcastReceiver.class);

			intent.setAction(Const.ACTION_HEXRINGAR_ALARM);

			PendingIntent sender = PendingIntent.getBroadcast(
					context, 0, intent, 0);

			AlarmManager alarmManager = (AlarmManager) (context
					.getSystemService(Context.ALARM_SERVICE));

			// 5分後の時間を設定
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			cal.add(Calendar.MINUTE, delay);
			final long MINUTE = 1000 * 60;
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					truncateMili ? (long)(cal.getTimeInMillis() / MINUTE * MINUTE) : cal.getTimeInMillis(),
					sender);

			NotificationManager notificationManager =
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

			Notification notification = new Notification(
					R.drawable.hex_status,
					"位置を確認します",
					System.currentTimeMillis());
			Intent notifyIntent = new Intent(context, MainActivity.class);
			//intentの設定
			PreferenceWrapper pref = new PreferenceWrapper(context);
			String lastLocaString = pref.getString(R.string.pref_last_location_key, "");
			Location lastLocation = LocationUtil.fromString(lastLocaString);
			String prev = lastLocation != null ? new SimpleDateFormat("H時mm分ss秒").format(lastLocation.getTime()) + " に位置を確認しました。\n" : "";
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
			notification.setLatestEventInfo(context.getApplicationContext(),
					"HexRinger", prev + "次回は " + new SimpleDateFormat("H時mm分ss秒").format(cal.getTime()) + " に確認します", contentIntent);
			//notification.flags = Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(R.string.app_name, notification);

			Log.d("Const", "setNextAlarm Alarm set at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(cal.getTime()));
		} catch (Exception e) {
			// TODO:PREF_KEY_ALARM_ENABLED を false にする
			Log.e("Const", "setNextAlarm Alarm set failed.", e);
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

			NotificationManager notificationManager =
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();

			Log.d("Const", "cancelAlarmManager Alarm canceled.");
		} catch (Exception e) {
			Log.e("Const", "cancelAlarmManager Alarm cancel failed.", e);
		}
	}
}
