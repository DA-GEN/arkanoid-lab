package com.dagen.arkanoid;

import android.app.Application;

import com.dagen.arkanoid.log.LoggingConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ArkanoidApp extends Application {
    private static final Logger LOG = Logger.getLogger(ArkanoidApp.class.getName());

    @Override
    public void onCreate() {
        super.onCreate();
        LoggingConfig.configure(this);

        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            LOG.log(Level.SEVERE, "uncaught exception in thread " + thread.getName(), error);
            if (previous != null) {
                previous.uncaughtException(thread, error);
            }
        });
        LOG.info("application started");
    }
}
