package com.dagen.arkanoid.log;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class LoggingConfig {
    public static final String TAG = "Arkanoid";

    static final String CONFIG_ASSET = "logging.properties";
    private static final String APP_LOGGER = "com.dagen.arkanoid";
    private static final String LOG_DIR = "logs";

    // LogManager keeps loggers weakly, so the configured one is pinned here
    private static Logger appLogger;

    private LoggingConfig() {
    }

    public static synchronized void configure(Context context) {
        LogManager manager = LogManager.getLogManager();
        try (InputStream in = context.getAssets().open(CONFIG_ASSET)) {
            manager.readConfiguration(in);
        } catch (IOException e) {
            manager.reset();
        }

        appLogger = Logger.getLogger(APP_LOGGER);
        boolean debuggable = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (!debuggable) {
            appLogger.setLevel(Level.INFO);
        }

        Logger root = Logger.getLogger("");
        LogcatHandler logcat = new LogcatHandler(TAG);
        logcat.setLevel(level(manager, "logcat.level", Level.ALL));
        root.addHandler(logcat);

        File dir = new File(context.getFilesDir(), LOG_DIR);
        try {
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IOException("cannot create " + dir);
            }
            FileHandler file = new FileHandler(new File(dir, "arkanoid%g.log").getAbsolutePath(),
                    intValue(manager, "file.limit", 256 * 1024), intValue(manager, "file.count", 3), true);
            file.setEncoding("UTF-8");
            file.setFormatter(new LineFormatter());
            file.setLevel(level(manager, "file.level", Level.INFO));
            root.addHandler(file);
        } catch (IOException e) {
            appLogger.log(Level.WARNING, "file logging disabled", e);
        }

        appLogger.config("logging configured, level: " + appLogger.getLevel() + ", dir: " + dir);
    }

    private static Level level(LogManager manager, String key, Level fallback) {
        String value = manager.getProperty(APP_LOGGER + "." + key);
        try {
            return value != null ? Level.parse(value.trim()) : fallback;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static int intValue(LogManager manager, String key, int fallback) {
        String value = manager.getProperty(APP_LOGGER + "." + key);
        try {
            return value != null ? Integer.parseInt(value.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
