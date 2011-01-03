package com.amay077.android.location;

import java.io.File;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.PatternFormatter;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class LoggingLocationListener extends TimeoutableLocationListener {
	private Logger logger = null;
	private String logFilePath = null;
	private static TimeoutLisener timeoutListener = new TimeoutLisener() {

		@Override
		public void onTimeouted(LocationListener sender) {
			Log.d("LoggingLocationListener", "onTimeouted");
		}
	};

	public LoggingLocationListener(LocationManager locaMan, long timeOutMS, String logFilePath) {
		super(locaMan, timeOutMS, timeoutListener);
		this.logFilePath = logFilePath;
		initialize();
	}

	private void initialize() {

		File sdCardDir = Environment.getExternalStorageDirectory();
		Uri logUri = Uri.withAppendedPath(Uri.fromFile(sdCardDir), logFilePath);
		String logFullPath = logUri.getPath();

		File logDir = new File(logFullPath).getParentFile();
		if (!logDir.exists()) {
			logDir.mkdir();
		}

		// Formatter
		PatternFormatter formatter = new PatternFormatter();
		formatter.setPattern("%m");

		// LogCatAppender
		LogCatAppender logCatAppender = new LogCatAppender();
		logCatAppender.setFormatter(formatter);
		logger.addAppender(logCatAppender);

		// FileAppender
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFileName(logFilePath);
		fileAppender.setAppend(true);
		fileAppender.setFormatter(formatter);
		logger.addAppender(fileAppender);
	}

	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);

		StringBuilder builder = new StringBuilder();

		builder.append(location.getLongitude());
		builder.append(",");
		builder.append(location.getLatitude());
		builder.append(",");
		builder.append(location.getAltitude());
		builder.append(",");
		builder.append(location.getAccuracy());
		builder.append(",");
		builder.append(location.getTime());

		logger.debug(builder.toString());

	}
}
