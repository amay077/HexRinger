package com.amay077.android.preference;

import com.amay077.android.logging.Log;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.preference.Preference;
import android.util.AttributeSet;

public class VersionPreference extends Preference {

	public VersionPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setVersionToSummary(context);
	}

	public VersionPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setVersionToSummary(context);
	}

	public VersionPreference(Context context) {
		super(context);
		setVersionToSummary(context);
	}

	private void setVersionToSummary(Context context) {
		try {
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
			setSummary(pkgInfo.versionName);
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "ctor() failed.", e);
		}
	}

}
