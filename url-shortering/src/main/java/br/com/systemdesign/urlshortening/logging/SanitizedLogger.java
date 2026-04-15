package br.com.systemdesign.urlshortening.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J logger wrapper that sanitizes all arguments before logging.
 */
public final class SanitizedLogger {

    private final Logger delegate;

    private SanitizedLogger(Class<?> type) {
        this.delegate = LoggerFactory.getLogger(type);
    }

    public static SanitizedLogger getLogger(Class<?> type) {
        return new SanitizedLogger(type);
    }

    // INFO
    public void info(String message, Object... args) {
        if (delegate.isInfoEnabled()) delegate.info(message, sanitize(args));
    }

    // DEBUG
    public void debug(String message, Object... args) {
        if (delegate.isDebugEnabled()) delegate.debug(message, sanitize(args));
    }

    // WARN
    public void warn(String message, Object... args) {
        if (delegate.isWarnEnabled()) delegate.warn(message, sanitize(args));
    }

    // ERROR
    public void error(String message, Object... args) {
        if (delegate.isErrorEnabled()) delegate.error(message, sanitize(args));
    }

    private Object[] sanitize(Object[] args) {
        if (args == null || args.length == 0) return args;
        Object[] sanitized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            sanitized[i] = (arg == null) ? null : LogSanitizer.sanitize(String.valueOf(arg));
        }
        return sanitized;
    }
}
