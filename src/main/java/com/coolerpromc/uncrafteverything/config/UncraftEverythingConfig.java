
package com.coolerpromc.uncrafteverything.config;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UncraftEverythingConfig {
    private static final Path CONFIG_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "uncrafteverything_common.toml");
    private static final ConfigFormat<?> FORMAT = TomlFormat.instance();
    private static CommentedFileConfig configFile;

    public static int experiencePoints;
    public static List<String> blacklist;

    public static void load() {
        configFile = CommentedFileConfig.builder(CONFIG_PATH)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        configFile.load();

        // Load with default and validation
        experiencePoints = Math.max(0, configFile.getOrElse("ExperiencePoints.experiencePoints", 1));

        blacklist = configFile.getOrElse("Restrictions.restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"));

        // Validate blacklist
        blacklist = blacklist.stream()
                .filter(entry -> {
                    try {
                        return Identifier.tryParse(entry) != null || entry.contains("*");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    public static void save() {
        configFile.set("ExperiencePoints.experiencePoints", experiencePoints);
        configFile.setComment("ExperiencePoints.experiencePoints",
                "The amount of experience points required to uncraft an item.\n" +
                        "Please be aware that this is Exp Point, NOT Exp Level");

        configFile.set("Restrictions.restrictions", blacklist);
        configFile.setComment("Restrictions.restrictions",
                "A list of items that cannot be uncrafted.\n" +
                        "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass*\n" +
                        "Press F3 + H in game and hover item to check their modid:name");

        configFile.save();
    }
}