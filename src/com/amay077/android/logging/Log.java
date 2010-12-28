package com.amay077.android.logging;

import java.io.File;

import android.net.Uri;
import android.os.Environment;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.PatternFormatter;

public class Log {

	static private Logger logger = LoggerFactory.getLogger();
	static private String LOGFILE_PATH = "HexRinger/log.txt";
	static private boolean isInitialized = false;

	static private void initialize() {

		if (isInitialized) {
			return;
		}

		File sdCardDir = Environment.getExternalStorageDirectory();
		Uri logUri = Uri.withAppendedPath(Uri.fromFile(sdCardDir), LOGFILE_PATH);
		String logFullPath = logUri.getPath();

		File logFile = new File(logFullPath);
		if (!logFile.exists()) {
			logFile.mkdir();
		}

		// Formatter
		PatternFormatter formatter = new PatternFormatter();
		formatter.setPattern("%d{ISO8601} [%P] %m %T");

		// FileAppender
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFileName(logFullPath);
		fileAppender.setAppend(true);
		fileAppender.setFormatter(formatter);
		logger.addAppender(fileAppender);

		// LogCatAppender
		LogCatAppender logCatAppender = new LogCatAppender();
		logCatAppender.setFormatter(formatter);
		logger.addAppender(logCatAppender);

		isInitialized = true;
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
