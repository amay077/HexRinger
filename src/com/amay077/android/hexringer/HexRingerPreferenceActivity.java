package com.amay077.android.hexringer;

import java.util.Map;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class HexRingerPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

    @Override
    protected void onResume() {
    	try {
            super.onResume();

            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
            Map<String, ?> map = prefs.getAll();
            for (String key : map.keySet()) {
            	updateSummary(prefs, key);
    		}

            // Set up a listener whenever a key changes
            prefs.registerOnSharedPreferenceChangeListener(this);
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "onResume() failed.", e);
		}
    }

    @Override
    protected void onPause() {
    	try {
            super.onPause();

            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "onPause() failed.", e);
		}
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	updateSummary(sharedPreferences, key);
    }

    private void updateSummary(SharedPreferences prefs, String key) {
    	try {
        	Preference pref = getPreferenceScreen().findPreference(key);
        	if (pref.getClass() == ListPreference.class) {
        		ListPreference listPref = (ListPreference)pref;
            	listPref.setSummary(addSummaryPrefix(listPref.getEntry().toString()));
        	} else {
            	pref.setSummary(addSummaryPrefix(prefs.getString(key, "")));
        	}

		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "onSharedPreferenceChanged() failed.", e);
		}
    }

    private String addSummaryPrefix(String mes) {
    	return "現在の設定:" + mes; // TODO : resourcing
    }
}
