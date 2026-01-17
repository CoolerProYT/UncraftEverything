package com.coolerpromc.uncrafteverything.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class UncraftDebugLogger {

    private static BufferedWriter writer;

    public static void init(Path logDir) {
        try {
            Files.createDirectories(logDir);
            Path file = logDir.resolve("uncrafteverything-debug.log");
            writer = Files.newBufferedWriter(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        if (writer == null) return;

        try {
            writer.write("[" + LocalDateTime.now() + "]\n" + message);
            writer.newLine();
            writer.flush();
        } catch (IOException ignored) {}
    }

    public static void close() {
        try {
            if (writer != null) writer.close();
        } catch (IOException ignored) {}
    }
}
