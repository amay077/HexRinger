package com.amay077.android.logging;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.format.SimpleFormatter;

public class Log {

	static private Logger logger = LoggerFactory.getLogger();

	static public void initialize() {
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFileName("log.txt");
		fileAppender.setAppend(true);
		fileAppender.setFormatter(new SimpleFormatter());
		logger.addAppender(fileAppender);
		logger.addAppender(new LogCatAppender());
	}

	static private String format(String tag, String msg) {
		return tag + ":" + msg;
	}

    public static int d(String tag, String msg)
    {
        logger.debug(format(tag, msg));
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr)
    {
        logger.debug(format(tag, msg), tr);
        return 0;
    }

    public static int i(String tag, String msg)
    {
        logger.info(format(tag, msg));
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr)
    {
        logger.info(format(tag, msg), tr);
        return 0;
    }

    public static int w(String tag, String msg)
    {
        logger.warn(format(tag, msg));
        return 0;
    }

    public static int w(String tag, String msg, Throwable tr)
    {
        logger.warn(format(tag, msg), tr);
        return 0;
    }

    public static int w(String tag, Throwable tr)
    {
        logger.warn(format(tag, ""), tr);
        return 0;
    }

    public static int e(String tag, String msg)
    {
        logger.error(format(tag, msg));
        return 0;
    }

    public static int e(String tag, String msg, Throwable tr)
    {
        logger.error(format(tag, msg), tr);
        return 0;
    }
}
