package com.coolerpromc.uncrafteverything.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricUncraftEverythingClientConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("uncrafteverything-client.toml");
    private static CommentedFileConfig configFile;

    public static void load() {
        configFile = CommentedFileConfig.builder(CONFIG_PATH).autosave().preserveInsertionOrder().sync().build();
        configFile.load();

        applyConfig();

        try {
            FileWatcher.defaultInstance().addWatch(CONFIG_PATH, FabricUncraftEverythingClientConfig::onConfigFileChanged);
            System.out.println("[UncraftEverything] Client config file watcher registered");
        } catch (Exception e) {
            System.err.println("[UncraftEverything] Failed to set up client config file watcher: " + e.getMessage());
        }
    }

    private static void onConfigFileChanged() {
        System.out.println("[UncraftEverything] Client config file changed, reloading...");
        configFile.load();
        applyConfig();
        System.out.println("[UncraftEverything] Client config reloaded successfully");
    }

    private static void applyConfig() {
        UncraftEverythingClientConfig.updateCache(
                configFile.getOrElse("AutoMove.autoMoveToInventory", true),
                configFile.getOrElse("StatusColor.noRecipeFound", 0xFFff615c),
                configFile.getOrElse("StatusColor.noSuitableOutputSlotColor", 0xFFfc8b49),
                configFile.getOrElse("StatusColor.notEnoughExpColor", 0xFFf2ff7a),
                configFile.getOrElse("StatusColor.notEnoughInputItemColor", 0xFFffef40),
                configFile.getOrElse("StatusColor.notEmptyShulkerColor", 0xFFe48aff),
                configFile.getOrElse("StatusColor.restrictedByConfigColor", 0xFF4f4f4f),
                configFile.getOrElse("StatusColor.damagedItemColor", 0xFFff615c),
                configFile.getOrElse("StatusColor.enchantedItemColor", 0xFFff615c),
                configFile.getOrElse("StatusColor.lockedItemColor", 0xFFff615c),
                configFile.getOrElse("StatusColor.progressionNotDefinedColor", 0xFFff615c)
        );
    }

    public static void save() {
        configFile.set("AutoMove.autoMoveToInventory", UncraftEverythingClientConfig.autoMoveToInventory);
        configFile.setComment("AutoMove.autoMoveToInventory", "Auto move uncrafted items to player inventory, drops to world if inventory is full.");

        configFile.set("StatusColor.noRecipeFound", UncraftEverythingClientConfig.noRecipeFoundColor);
        configFile.setComment("StatusColor.noRecipeFound", "Overlay color for No Recipe Found");

        configFile.set("StatusColor.noSuitableOutputSlotColor", UncraftEverythingClientConfig.noSuitableOutputSlotColor);
        configFile.setComment("StatusColor.noSuitableOutputSlotColor", "Overlay color for No Suitable Output Slot");

        configFile.set("StatusColor.notEnoughExpColor", UncraftEverythingClientConfig.notEnoughExpColor);
        configFile.setComment("StatusColor.notEnoughExpColor", "Overlay color for Not Enough Exp");

        configFile.set("StatusColor.notEnoughInputItemColor", UncraftEverythingClientConfig.notEnoughInputItemColor);
        configFile.setComment("StatusColor.notEnoughInputItemColor", "Overlay color for Not Enough Input Item");

        configFile.set("StatusColor.notEmptyShulkerColor", UncraftEverythingClientConfig.notEmptyShulkerColor);
        configFile.setComment("StatusColor.notEmptyShulkerColor", "Overlay color for Not Empty Shulker");

        configFile.set("StatusColor.restrictedByConfigColor", UncraftEverythingClientConfig.restrictedByConfigColor);
        configFile.setComment("StatusColor.restrictedByConfigColor", "Overlay color for Restricted By Config");

        configFile.set("StatusColor.damagedItemColor", UncraftEverythingClientConfig.damagedItemColor);
        configFile.setComment("StatusColor.damagedItemColor", "Overlay color for Damaged Item");

        configFile.set("StatusColor.enchantedItemColor", UncraftEverythingClientConfig.enchantedItemColor);
        configFile.setComment("StatusColor.enchantedItemColor", "Overlay color for Enchanted Item");

        configFile.set("StatusColor.lockedItemColor", UncraftEverythingClientConfig.lockedItemColor);
        configFile.setComment("StatusColor.lockedItemColor", "Overlay color for Locked Item");

        configFile.set("StatusColor.progressionNotDefinedColor", UncraftEverythingClientConfig.progressionNotDefinedColor);
        configFile.setComment("StatusColor.progressionNotDefinedColor", "Overlay color for Progression Not Defined");

        configFile.save();
    }

    public static void shutdown() {
        try {
            FileWatcher fileWatcher = FileWatcher.defaultInstance();
            fileWatcher.removeWatch(CONFIG_PATH);
            fileWatcher.stop();
            System.out.println("[UncraftEverything] Config file watcher removed");
        } catch (Exception e) {
            System.err.println("[UncraftEverything] Failed to remove config file watcher: " + e.getMessage());
        }
    }
}