package com.coolerpromc.uncrafteverything.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

public class FTBQuestProgressionConfig {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    private static Map<String, String> progressionMap = new ConcurrentHashMap<>();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "uncrafteverything-ftbquest-progression.json");

    private static WatchService watchService;
    private static Thread watchThread;

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            saveDefaults();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Map<String, String> loaded = GSON.fromJson(reader, MAP_TYPE);
            if (loaded != null) {
                progressionMap = new ConcurrentHashMap<>(loaded);
            } else {
                progressionMap = new ConcurrentHashMap<>();
            }
        } catch (Exception e) {
            System.out.println("Failed to load ftb quest progression config! " + e.getMessage());
            progressionMap = new ConcurrentHashMap<>(); // fallback
        }
    }

    public static void saveDefaults() {
        progressionMap.put("item_id/item_tags/item_id_wildcard", "ftb_quest_id_here");
        save();
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(progressionMap, writer);
        } catch (Exception e) {
            System.out.println("Failed to save ftb quest progression config! " + e.getMessage());
        }
    }

    public static Map<String, String> getProgressionMap() {
        if (progressionMap == null) {
            load();
        }
        return progressionMap;
    }

    public static synchronized void startWatcher() {
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
                                System.out.println("[UncraftEverything] ftb quest progression config file changed, reloading...");
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
            System.out.println("Error hot reloading ftb quest progression config: " + e.getMessage());
        }
    }

    public static synchronized void stopWatcher() {
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (Exception ignored) {}
        if (watchThread != null) {
            watchThread.interrupt();
        }
        watchService = null;
        watchThread = null;

        System.out.println("[UncraftEverything] ftb quest progression config watcher stopped.");
    }


    public static String getQuestId(ItemStack itemStack) {
        Map<String, String> questMap = FTBQuestProgressionConfig.getProgressionMap();
        for (Map.Entry<String, String> map : questMap.entrySet()){
            if (map.getKey().startsWith("#")){
                String tagName = map.getKey().substring(1);
                Optional<Tag<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.getItem().isIn(tagKey.get())) {
                    return map.getValue();
                }
            }

            if (map.getKey().contains("*")){
                String regex = map.getKey().replace("*", ".*");
                if (Pattern.matches(regex, getItemLocation(itemStack).toString())){
                    return map.getValue();
                }
            }
        }
        return null;
    }

    private static Identifier getItemLocation(ItemStack itemStack){
        return Registry.ITEM.getId(itemStack.getItem());
    }
}