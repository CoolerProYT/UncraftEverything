package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.util.Status;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class UncraftEverythingClientConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("uncrafteverything_client.toml");
    private static final ConfigFormat<?> FORMAT = TomlFormat.instance();
    private static CommentedFileConfig configFile;

    public static boolean autoMoveToInventory;
    public static int noRecipeFoundColor;
    public static int noSuitableOutputSlotColor;
    public static int notEnoughExpColor;
    public static int notEnoughInputItemColor;
    public static int notEmptyShulkerColor;
    public static int restrictedByConfigColor;
    public static int damagedItemColor;
    public static int enchantedItemColor;
    public static int lockedItemColor;
    public static int progressionNotDefinedColor;

    public static void load() {
        configFile = CommentedFileConfig.builder(CONFIG_PATH)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        configFile.load();

        applyConfig();

        try {
            FileWatcher.defaultInstance().addWatch(CONFIG_PATH, UncraftEverythingClientConfig::onConfigFileChanged);
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
        autoMoveToInventory = configFile.getOrElse("AutoMove.autoMoveToInventory", true);

        noRecipeFoundColor = configFile.getOrElse("StatusColor.noRecipeFound", 0xFFff615c);
        noSuitableOutputSlotColor = configFile.getOrElse("StatusColor.noSuitableOutputSlotColor", 0xFFfc8b49);
        notEnoughExpColor = configFile.getOrElse("StatusColor.notEnoughExpColor", 0xFFf2ff7a);
        notEnoughInputItemColor = configFile.getOrElse("StatusColor.notEnoughInputItemColor", 0xFFffef40);
        notEmptyShulkerColor = configFile.getOrElse("StatusColor.notEmptyShulkerColor", 0xFFe48aff);
        restrictedByConfigColor = configFile.getOrElse("StatusColor.restrictedByConfigColor", 0xFF4f4f4f);
        damagedItemColor = configFile.getOrElse("StatusColor.damagedItemColor", 0xFFff615c);
        enchantedItemColor = configFile.getOrElse("StatusColor.enchantedItemColor", 0xFFff615c);
        lockedItemColor = configFile.getOrElse("StatusColor.lockedItemColor", 0xFFff615c);
        progressionNotDefinedColor = configFile.getOrElse("StatusColor.progressionNotDefinedColor", 0xFFff615c);

        onChanged();
    }

    public static void save() {
        configFile.set("AutoMove.autoMoveToInventory", autoMoveToInventory);
        configFile.setComment("AutoMove.autoMoveToInventory", "Auto move uncrafted items to player inventory, drops to world if inventory is full.");

        configFile.set("StatusColor.noRecipeFound", noRecipeFoundColor);
        configFile.setComment("StatusColor.noRecipeFound", "Overlay color for No Recipe Found");

        configFile.set("StatusColor.noSuitableOutputSlotColor", noSuitableOutputSlotColor);
        configFile.setComment("StatusColor.noSuitableOutputSlotColor", "Overlay color for No Suitable Output Slot");

        configFile.set("StatusColor.notEnoughExpColor", notEnoughExpColor);
        configFile.setComment("StatusColor.notEnoughExpColor", "Overlay color for Not Enough Exp");

        configFile.set("StatusColor.notEnoughInputItemColor", notEnoughInputItemColor);
        configFile.setComment("StatusColor.notEnoughInputItemColor", "Overlay color for Not Enough Input Item");

        configFile.set("StatusColor.notEmptyShulkerColor", notEmptyShulkerColor);
        configFile.setComment("StatusColor.notEmptyShulkerColor", "Overlay color for Not Empty Shulker");

        configFile.set("StatusColor.restrictedByConfigColor", restrictedByConfigColor);
        configFile.setComment("StatusColor.restrictedByConfigColor", "Overlay color for Restricted By Config");

        configFile.set("StatusColor.damagedItemColor", damagedItemColor);
        configFile.setComment("StatusColor.damagedItemColor", "Overlay color for Damaged Item");

        configFile.set("StatusColor.enchantedItemColor", enchantedItemColor);
        configFile.setComment("StatusColor.enchantedItemColor", "Overlay color for Enchanted Item");

        configFile.set("StatusColor.lockedItemColor", lockedItemColor);
        configFile.setComment("StatusColor.lockedItemColor", "Overlay color for Locked Item");

        configFile.set("StatusColor.progressionNotDefinedColor", progressionNotDefinedColor);
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

    public static void onChanged(){
        Status.NO_RECIPE_FOUND.setOverlay(noRecipeFoundColor);
        Status.NO_SUITABLE_OUTPUT_SLOT.setOverlay(noSuitableOutputSlotColor);
        Status.NOT_ENOUGH_EXP.setOverlay(notEnoughExpColor);
        Status.NOT_ENOUGH_INPUT_ITEM.setOverlay(notEnoughInputItemColor);
        Status.NOT_EMPTY_SHULKER.setOverlay(notEmptyShulkerColor);
        Status.RESTRICTED_BY_CONFIG.setOverlay(restrictedByConfigColor);
        Status.DAMAGED_ITEM.setOverlay(damagedItemColor);
        Status.ENCHANTED_ITEM.setOverlay(enchantedItemColor);
        Status.LOCKED_ITEM.setOverlay(lockedItemColor);
        Status.PROGRESSION_NOT_DEFINED.setOverlay(progressionNotDefinedColor);
    }
}