package com.dagen.arkanoid.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggingTest {

    @Test
    public void levelsMapToLogcatPriorities() {
        assertEquals(Log.ERROR, LogcatHandler.priority(Level.SEVERE));
        assertEquals(Log.WARN, LogcatHandler.priority(Level.WARNING));
        assertEquals(Log.INFO, LogcatHandler.priority(Level.INFO));
        assertEquals(Log.DEBUG, LogcatHandler.priority(Level.CONFIG));
        assertEquals(Log.DEBUG, LogcatHandler.priority(Level.FINE));
        assertEquals(Log.VERBOSE, LogcatHandler.priority(Level.FINEST));
    }

    @Test
    public void lineContainsLevelLoggerAndMessage() {
        LogRecord record = new LogRecord(Level.WARNING, "ball lost at {0}");
        record.setLoggerName("com.dagen.arkanoid.game.GameWorld");
        record.setParameters(new Object[]{42});

        String line = new LineFormatter().format(record);
        assertTrue(line, line.contains("WARNING"));
        assertTrue(line, line.contains("[GameWorld]"));
        assertTrue(line, line.contains("ball lost at 42"));
    }

    @Test
    public void lineIncludesStackTrace() {
        LogRecord record = new LogRecord(Level.SEVERE, "crash");
        record.setThrown(new IllegalStateException("boom"));
        String line = new LineFormatter().format(record);
        assertTrue(line.contains("IllegalStateException: boom"));
        assertTrue(line.contains("[root]"));
    }
}
