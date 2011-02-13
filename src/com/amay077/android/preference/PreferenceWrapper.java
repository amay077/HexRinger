package com.amay077.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceWrapper {
	private SharedPreferences pref = null;
	private Resources res = null;

	public PreferenceWrapper(Context context) {
		res = context.getResources();
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getString(int resID, String defaultValue) {
		return pref.getString(res.getString(resID), defaultValue);
	}

	public int getAsInt(int resID, String defaultValue) {
		try {
			return Integer.parseInt(pref.getString(res.getString(resID), defaultValue));
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "getAsInt failed.", e);
			return 0;
		}
	}

	public boolean getBoolean(int resID, boolean defaultValue) {
		return pref.getBoolean(res.getString(resID), defaultValue);
	}

	public int getInt(int resID, int defaultValue) {
		return pref.getInt(res.getString(resID), defaultValue);
	}

	public long getLong(int resID, long defaultValue) {
		return pref.getLong(res.getString(resID), defaultValue);
	}

	public void saveString(int resID, String saveValue) {
		Editor editor = pref.edit();
		editor.putString(res.getString(resID), saveValue);
		editor.commit();
	}

	public void saveBoolean(int resID, boolean saveValue) {
		Editor editor = pref.edit();
		editor.putBoolean(res.getString(resID), saveValue);
		editor.commit();
	}

	public void saveLong(int resID, long saveValue) {
		Editor editor = pref.edit();
		editor.putLong(res.getString(resID), saveValue);
		editor.commit();
	}

	public void saveInt(int resID, int saveValue) {
		Editor editor = pref.edit();
		editor.putInt(res.getString(resID), saveValue);
		editor.commit();
	}

	public void remove(int resID) {
		Editor editor = pref.edit();
		editor.remove(res.getString(resID));
		editor.commit();
	}
}
