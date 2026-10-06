package com.dagen.arkanoid.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LineFormatter extends Formatter {
    private static final String PATTERN = "%1$tF %1$tT.%1$tL %2$-7s [%3$s] %4$s%n";

    @Override
    public String format(LogRecord record) {
        StringBuilder line = new StringBuilder(String.format(Locale.ROOT, PATTERN,
                record.getMillis(), record.getLevel().getName(),
                shortName(record.getLoggerName()), formatMessage(record)));
        if (record.getThrown() != null) {
            StringWriter trace = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(trace));
            line.append(trace);
        }
        return line.toString();
    }

    static String shortName(String loggerName) {
        if (loggerName == null || loggerName.isEmpty()) {
            return "root";
        }
        return loggerName.substring(loggerName.lastIndexOf('.') + 1);
    }
}
