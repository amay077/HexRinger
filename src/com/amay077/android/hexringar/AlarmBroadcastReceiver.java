package com.amay077.android.hexringar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
    	Log.d("AlarmBroadcastReceiver.onReceive", "starting.");
		try {
	        if (!intent.hasExtra(Const.EXTRA_GEOHEXES)) {
	        	Log.w("AlarmBroadcastReceiver.onReceive", Const.EXTRA_GEOHEXES + " not found.");
	        	return;
	        }

	        String action = intent.getAction();
	        String[] geoHexes = intent.getStringArrayExtra(Const.EXTRA_GEOHEXES);

	        if (geoHexes == null || geoHexes.length == 0) {
	        	Log.w("AlarmBroadcastReceiver.onReceive", Const.EXTRA_GEOHEXES + " is length zero.");
	        	return;
	        }

	        if (action.equals(Const.ACTION_HEXRINGAR_ALARM)) { // AlarmManager によって定期的発行された
	        	// do something

	        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) { // 端末が起動完了した
	        	// ここでアラームの再セットを行う
	        	// ※設定による有効/無効必要
				Const.setAlarmManager(context);
	        }
		} catch (Exception exp) {
			Log.w("AlarmBroadcastReceiver.onReceive", "failed.", exp);
		} finally {
			// AlarmaManager への再設定
			Const.setAlarmManager(context);
			setResult(Activity.RESULT_OK, null, null);
		}
	}
}
