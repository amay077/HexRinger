package com.amay077.android.hexringer;

import java.util.Calendar;

import com.amay077.android.logging.Log;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

public class Const {
	// Intent Action
	static public final String ACTION_HEXRINGAR_ALARM = "com.amay077.android.hexringer.ALARM";

	// Preference
//	static public final String PREF_KEY_ALARM_ENABLED = "ALARM_ENABLED";
//	static public final String PREF_KEY_WATCH_HEXES = "WATCH_HEXES";
//	static public final String PREF_KEY_LAST_HEX = "LAST_HEX";

	/** アラームの実行間隔（分） */
//	static public final short ALARM_INTERVAL_MINUTES = 5;
    /** 初回測位までのタイムアウト時間（ミリ秒） */
	static public final short LOCATION_REQUEST_TIMEOUT_MS = 30000;
	/** 配列を文字列化する時の区切り文字 */
	public static final String ARRAY_SPLITTER = ",";

	/** AlarmManager にインテント発行を設定する（今からｎ分後） */
	static public void setNextAlarm(Context context, int delay) {
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
			alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

			Log.d("Const", "setNextAlarm Alarm set at " + DateFormat.format("yyyy/MM/dd hh:mm:ss", cal.getTime()));
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

			Log.d("Const", "cancelAlarmManager Alarm canceled.");
		} catch (Exception e) {
			Log.e("Const", "cancelAlarmManager Alarm cancel failed.", e);
		}
	}
}
