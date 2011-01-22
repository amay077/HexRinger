package com.amay077.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class PreferenceWrapper {
	private SharedPreferences pref = null;
//	private Context context = null;
	private Resources res = null;

	public PreferenceWrapper(Context context) {
//		this.context = context;
		res = context.getResources();
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getString(int resID, String defaultValue) {
		return pref.getString(res.getString(resID), defaultValue);
	}

	public int getAsInt(int resID, int defaultValue) {
		return Integer.parseInt(pref.getString(res.getString(resID), String.valueOf(defaultValue)));
	}

	public boolean getBoolean(int resID, boolean defaultValue) {
		return pref.getBoolean(res.getString(resID), defaultValue);
	}

	public int getInt(int resID, int defaultValue) {
		return pref.getInt(res.getString(resID), defaultValue);
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

	public void remove(int resID) {
		Editor editor = pref.edit();
		editor.remove(res.getString(resID));
		editor.commit();
	}
}
