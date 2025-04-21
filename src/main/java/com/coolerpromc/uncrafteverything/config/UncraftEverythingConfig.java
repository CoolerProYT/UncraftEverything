
package com.coolerpromc.uncrafteverything.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class UncraftEverythingConfig {
    public static final UncraftEverythingConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;
    public final ModConfigSpec.IntValue experiencePoints;
    public final ModConfigSpec.ConfigValue<List<? extends String>> blacklist;

    static {
        Pair<UncraftEverythingConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(UncraftEverythingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingConfig(ModConfigSpec.Builder builder){
        builder.push("ExperiencePoints");
        experiencePoints = builder.comment("The amount of experience points required to uncraft an item.", "Please be aware that this is Exp Point, NOT Exp Level").defineInRange("experiencePoints", 1, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Restrictions");
        blacklist = builder.comment("A list of items that cannot be uncrafted.", "Format: modid:item_name", "Press F3 + h in game and hover item to check their modid:name").defineList("restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"), () -> "", o -> o instanceof String && ResourceLocation.tryParse((String) o) != null);
        builder.pop();
    }

    public int getExperiencePoints() {
        return experiencePoints.getAsInt();
    }
}