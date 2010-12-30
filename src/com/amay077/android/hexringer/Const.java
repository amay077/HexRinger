package com.amay077.android.hexringer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Const {
	// Intent Action
	static public final String ACTION_HEXRINGAR_ALARM = "com.amay077.android.hexringar.ALARM";

	// Intent Data
	static public final String EXTRA_GEOHEXES = "EXTRA_GEOHEXES";

	// Preference
	static public final String PREF_KEY_ALARM_ENABLED = "ALARM_ENABLED";
	static public final String PREF_KEY_LAST_HEX = "LAST_HEX";

	/** アラームの実行間隔（分） */
	static public final short ALARM_INTERVAL_MINUTES = 5;
    /** 測位 */
	static public final short LOCATION_REQUEST_TIMEOUT_MS = 30000;

	/** AlarmManager にインテント発行を設定する（今からｎ分後） */
	static public void setAlarmManager(Context context) {
		Intent intent = new Intent(context,
				AlarmBroadcastReceiver.class);

		intent.setAction(Const.ACTION_HEXRINGAR_ALARM);
		intent.putExtra(Const.EXTRA_GEOHEXES, new String[]{"aaa"});

		PendingIntent sender = PendingIntent.getBroadcast(
				context, 0, intent, 0);

		AlarmManager alarmManager = (AlarmManager) (context
				.getSystemService(Context.ALARM_SERVICE));

		// 5分後の時間を設定
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.MINUTE, ALARM_INTERVAL_MINUTES);
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

		Log.d("Const.setAlarmManager", "Alarm set at " + new SimpleDateFormat().format(cal.getTime()));
	}

	static public void cancelAlarmManager(Context context) {
		Intent intent = new Intent(context,
				AlarmBroadcastReceiver.class);

		intent.setAction(Const.ACTION_HEXRINGAR_ALARM);
		intent.putExtra(Const.EXTRA_GEOHEXES, new String[]{"aaa"});

		PendingIntent sender = PendingIntent.getBroadcast(
				context, 0, intent, 0);

		AlarmManager alarmManager = (AlarmManager) (context
				.getSystemService(Context.ALARM_SERVICE));

		alarmManager.cancel(sender);

		Log.d("Const.setAlarmManager", "Alarm canceled.");
	}
}
