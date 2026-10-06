package com.dagen.arkanoid.log;

import android.util.Log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/** Forwards java.util.logging records to Logcat under a single tag. */
public class LogcatHandler extends Handler {
    private final String tag;

    public LogcatHandler(String tag) {
        this.tag = tag;
        setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return formatMessage(record);
            }
        });
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String message = LineFormatter.shortName(record.getLoggerName()) + ": " + getFormatter().format(record);
        if (record.getThrown() != null) {
            message += '\n' + Log.getStackTraceString(record.getThrown());
        }
        Log.println(priority(record.getLevel()), tag, message);
    }

    static int priority(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return Log.ERROR;
        } else if (value >= Level.WARNING.intValue()) {
            return Log.WARN;
        } else if (value >= Level.INFO.intValue()) {
            return Log.INFO;
        } else if (value >= Level.FINE.intValue()) {
            return Log.DEBUG;
        }
        return Log.VERBOSE;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
