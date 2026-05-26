package com.coolerpromc.uncrafteverything.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.List;

public class FabricUncraftEverythingConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("uncrafteverything-common.toml");
    private static CommentedFileConfig configFile;

    public static void load() {
        configFile = CommentedFileConfig.builder(CONFIG_PATH).autosave().preserveInsertionOrder().sync().build();
        configFile.load();
        applyConfig();

        try {
            FileWatcher.defaultInstance().addWatch(CONFIG_PATH, FabricUncraftEverythingConfig::onConfigFileChanged);
            System.out.println("[UncraftEverything] Config file watcher registered");
        } catch (Exception e) {
            System.err.println("[UncraftEverything] Failed to set up config file watcher: " + e.getMessage());
        }
    }

    private static void onConfigFileChanged() {
        System.out.println("[UncraftEverything] Config file changed, reloading...");
        configFile.load();
        applyConfig();
        System.out.println("[UncraftEverything] Config reloaded successfully");
    }

    private static void applyConfig() {
        UncraftEverythingConfig.updateCache(
                configFile.getEnumOrElse("Experience.experienceType", UncraftEverythingConfig.ExperienceType.LEVEL),
                Math.max(0, configFile.getOrElse("Experience.experiences", 1)),
                configFile.getEnumOrElse("Restrictions.restrictionType", UncraftEverythingConfig.RestrictionType.BLACKLIST),
                configFile.getOrElse("Restrictions.restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table")),
                configFile.getOrElse("AllowEnchantedItems.allowEnchantedItems", true),
                configFile.getOrElse("AllowUnSmithing.allowUnSmithing", true),
                configFile.getOrElse("AllowDamaged.allowDamaged", true),
                configFile.getOrElse("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", true),
                configFile.getOrElse("RestrictedModIngredients.restrictedModIngredients", List.of("productivetrees", "chipped")),
                configFile.getOrElse("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", true),
                configFile.getOrElse("FTBQuestProgression.enableProgression", false),
                configFile.getOrElse("FTBQuestProgression.onlyAllowDefinedProgression", false),
                configFile.getOrElse("OutputEnchantedBook.outputEnchantedBook", false),
                configFile.getOrElse("Restrictions.restrictAmbiguouslyCraftedItems", false)
        );
    }

    public static void save() {
        configFile.set("Experience.experienceType", UncraftEverythingConfig.experienceType);
        configFile.setComment("Experience.experienceType", "The type of experience to be used.\n" + "[LEVEL/POINT]");

        configFile.set("Experience.experiences", UncraftEverythingConfig.experience);
        configFile.setComment("Experience.experiences", "The default amount of experience point/level required to uncraft an item. More detailed exp can be configured in uncrafteverything-exp.json");

        configFile.set("Restrictions.restrictionType", UncraftEverythingConfig.restrictionType);
        configFile.setComment("Restrictions.restrictionType", "The type of restriction to be used.\n" + "[BLACKLIST/WHITELIST]");

        configFile.set("Restrictions.restrictions", UncraftEverythingConfig.restrictions);
        configFile.setComment("Restrictions.restrictions",
                """
                        A list of items that can/cannot be uncrafted depending on type of restriction.
                        Invalid input will cause config reset at runtime.
                        Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name
                        Press F3 + h in game and hover item to check their modid:name""");

        configFile.set("Restrictions.restrictAmbiguouslyCraftedItems", UncraftEverythingConfig.restrictAmbiguouslyCraftedItems);
        configFile.setComment("Restrictions.restrictAmbiguouslyCraftedItems", "Restrict recipe that use ItemTags ingredient from uncrafting.");

        configFile.set("AllowEnchantedItems.allowEnchantedItems", UncraftEverythingConfig.allowEnchantedItems);
        configFile.setComment("AllowEnchantedItems.allowEnchantedItems", "Allow uncrafting of enchanted items. [true/false]");

        configFile.set("AllowUnSmithing.allowUnSmithing", UncraftEverythingConfig.allowUnSmithing);
        configFile.setComment("AllowUnSmithing.allowUnSmithing", "Allow uncrafting of items that obtained from smithing (Trimmed Armor/Netherite Armor). [true/false]");

        configFile.set("AllowDamaged.allowDamaged", UncraftEverythingConfig.allowDamaged);
        configFile.setComment("AllowDamaged.allowDamaged", "Allow uncrafting of damaged items. [true/false]");

        configFile.set("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", UncraftEverythingConfig.preventModdedIngredientsFromVanillaItems);
        configFile.setComment("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", "Prevents vanilla items (e.g., iron axe) from being uncrafted using modded recipes. This helps avoid potential duplication or unintended outputs caused by modded ingredients. [true/false]");

        configFile.set("RestrictedModIngredients.restrictedModIngredients", UncraftEverythingConfig.restrictedModIngredients);
        configFile.setComment("RestrictedModIngredients.restrictedModIngredients", "A list of modid that would be excluded when uncrafting, to prevent too much recipes and causing performance issues. \nFormat: modid");

        configFile.set("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", UncraftEverythingConfig.prioritizeVanillaIngredientRecipe);
        configFile.setComment("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", "Recipe selection should prioritize recipe with more vanilla ingredients. [true/false]");

        configFile.set("FTBQuestProgression.enableProgression", UncraftEverythingConfig.enableProgression);
        configFile.setComment("FTBQuestProgression.enableProgression","Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)");

        configFile.set("FTBQuestProgression.onlyAllowDefinedProgression", UncraftEverythingConfig.onlyAllowDefinedProgression);
        configFile.setComment("FTBQuestProgression.onlyAllowDefinedProgression", "When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.");

        configFile.set("OutputEnchantedBook.outputEnchantedBook", UncraftEverythingConfig.outputEnchantedBook);
        configFile.setComment("OutputEnchantedBook.outputEnchantedBook", "Output Enchanted Book for enchanted item. [true/false]");

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