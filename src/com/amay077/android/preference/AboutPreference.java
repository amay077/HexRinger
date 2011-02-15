package com.amay077.android.preference;

import com.amay077.android.hexringer.AboutActivity;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class AboutPreference extends Preference {
	public AboutPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AboutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AboutPreference(Context context) {
		super(context);
	}

	@Override
	protected void onClick() {
		super.onClick();

		Intent intent = new Intent(this.getContext(), AboutActivity.class);
		this.getContext().startActivity(intent);
	}
}
