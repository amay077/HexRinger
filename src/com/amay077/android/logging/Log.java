package com.amay077.android.logging;

import java.io.File;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.PatternFormatter;

public class Log {

	static private Logger logger = LoggerFactory.getLogger();
	static private String LOGFILE_PATH = "/HexRinger/log.txt";
	static private boolean isInitialized = false;

	static private void initialize() {

		if (isInitialized) {
			return;
		}

		File sdCardDir = Environment.getExternalStorageDirectory();
		Uri logUri = Uri.withAppendedPath(Uri.fromFile(sdCardDir), LOGFILE_PATH);
		String logFullPath = logUri.getPath();

		File logDir = new File(logFullPath).getParentFile();
		if (!logDir.exists()) {
			logDir.mkdir();
		}

		// Formatter
		PatternFormatter formatter = new PatternFormatter();
		formatter.setPattern("%d{ISO8601} [%P] %m %T");

		// LogCatAppender
		LogCatAppender logCatAppender = new LogCatAppender();
		logCatAppender.setFormatter(formatter);
		logger.addAppender(logCatAppender);

		// FileAppender
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFileName(LOGFILE_PATH);
		fileAppender.setAppend(true);
		fileAppender.setFormatter(formatter);
		logger.addAppender(fileAppender);

		isInitialized = true;
	}

	static public void writeApplicationInfo(Context context) {
    	initialize();
		try {
			PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);

			logger.info("PackageName:" + pkgInfo.packageName);
			logger.info("VersionName:" + pkgInfo.versionName);
			logger.info("VersionCode:" + String.valueOf(pkgInfo.versionCode));
			logger.info("ApplicationName:" + pkgInfo.applicationInfo.name);
			logger.info("IsDebuggable:" + String.valueOf(isDebug(context)));

		} catch (NameNotFoundException e) {
			logger.warn("writeApplicationInfo failed.", e);
		}
	}

	public static boolean isDebug( Context context ) {
	    PackageManager pm = context.getPackageManager();
	    ApplicationInfo ai = new ApplicationInfo();
	    try {
	        ai = pm.getApplicationInfo( context.getPackageName(), 0 );
	    } catch( NameNotFoundException e ) {
	        ai = null;
	        return false;
	    }
	    if( (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE ) {
	        return true;
	    }
	    return false;
	}

	static private String format(String tag, String msg) {
		return tag + ":" + msg;
	}

    public static int d(String tag, String msg)
    {
    	initialize();
        logger.debug(format(tag, msg));
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr)
    {
    	initialize();
        logger.debug(format(tag, msg), tr);
        return 0;
    }

    public static int i(String tag, String msg)
    {
    	initialize();
        logger.info(format(tag, msg));
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr)
    {
    	initialize();
        logger.info(format(tag, msg), tr);
        return 0;
    }

    public static int w(String tag, String msg)
    {
    	initialize();
        logger.warn(format(tag, msg));
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr)
    {
    	initialize();
        logger.warn(format(tag, msg), tr);
        return 0;
    }

    public static int w(String tag, Throwable tr)
    {
    	initialize();
        logger.warn(format(tag, ""), tr);
        return 0;
    }

    public static int e(String tag, String msg)
    {
    	initialize();
        logger.error(format(tag, msg));
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr)
    {
    	initialize();
    	logger.error(format(tag, msg), tr);
        return 0;
    }
}
