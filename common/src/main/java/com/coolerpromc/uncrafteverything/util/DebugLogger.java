package com.coolerpromc.uncrafteverything.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class DebugLogger {
    private static final boolean DEBUG = true;
    private static final Logger LOGGER;

    static {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{HH:mm:ss} [%level] %msg%n")
                .withConfiguration(config)
                .build();

        FileAppender appender = FileAppender.newBuilder()
                .setName("uncrafteverything-debug")
                .withFileName("logs/uncrafteverything-debug.log")
                .withAppend(true)
                .setLayout(layout)
                .setConfiguration(config)
                .build();

        appender.start();
        config.addAppender(appender);

        org.apache.logging.log4j.core.Logger coreLogger =
                (org.apache.logging.log4j.core.Logger) LogManager.getLogger("uncrafteverything-debug");
        coreLogger.addAppender(appender);
        coreLogger.setAdditive(false);

        LOGGER = coreLogger;
    }

    public static void log(String message) {
        if (DEBUG) LOGGER.info(message);
    }

    public static void log(String category, String message) {
        if (DEBUG) LOGGER.info("[{}] {}", category, message);
    }

    public static void warn(String message) {
        if (DEBUG) LOGGER.warn(message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }
}