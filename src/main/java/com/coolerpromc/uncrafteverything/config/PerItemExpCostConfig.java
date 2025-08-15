package com.coolerpromc.uncrafteverything.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerItemExpCostConfig {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Integer>>() {
    }.getType();

    private static Map<String, Integer> perItemExp = new ConcurrentHashMap<>();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "uncrafteverything-exp.json");

    private static WatchService watchService;
    private static Thread watchThread;

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            saveDefaults();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Map<String, Integer> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                perItemExp = new ConcurrentHashMap<>(loaded);
            } else {
                perItemExp = new ConcurrentHashMap<>();
            }
        } catch (Exception e) {
            System.out.println("Failed to load per item exp config! " + e.getMessage());
            perItemExp = new ConcurrentHashMap<>(); // fallback
        }
    }

    public static void saveDefaults() {
        perItemExp.put("minecraft:diamond_sword", 2);
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(perItemExp, writer);
        } catch (Exception e) {
            System.out.println("Failed to save per item exp config! " + e.getMessage());
        }
    }

    public static Map<String, Integer> getPerItemExp() {
        if (perItemExp == null) {
            load();
        }
        return perItemExp;
    }

    public static void startWatcher() {
        if (watchThread != null && watchThread.isAlive()) {
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path configDir = CONFIG_FILE.getParentFile().toPath();
            configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            watchThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals(CONFIG_FILE.getName())) {
                                System.out.println("[UncraftEverything] Per item exp config file changed, reloading...");
                                load();
                            }
                        }
                        key.reset();
                    }
                } catch (ClosedWatchServiceException cwse) {
                    // Normal shutdown
                } catch (Exception e) {
                    System.out.println("Error watching config file: " + e.getMessage());
                }
            }, "PerItemExpConfig Watcher");

            watchThread.setDaemon(true);
            watchThread.start();
        } catch (Exception e) {
            System.out.println("Error hot reloading per item exp config: " + e.getMessage());
        }
    }

    public static synchronized void stopWatcher() {
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (Exception ignored) {
        }
        if (watchThread != null) {
            watchThread.interrupt();
        }
        watchService = null;
        watchThread = null;

        System.out.println("[UncraftEverything] Per item exp config watcher stopped.");
    }
}