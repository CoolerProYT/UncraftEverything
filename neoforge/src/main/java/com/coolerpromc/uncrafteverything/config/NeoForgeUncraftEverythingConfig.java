package com.coolerpromc.uncrafteverything.config;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.coolerpromc.uncrafteverything.config.UncraftEverythingConfig.tryParseTagKey;

public class NeoForgeUncraftEverythingConfig {
    public static final NeoForgeUncraftEverythingConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.EnumValue<UncraftEverythingConfig.ExperienceType> experienceType;
    public final ModConfigSpec.IntValue experience;
    public final ModConfigSpec.EnumValue<UncraftEverythingConfig.RestrictionType> restrictionType;
    public final ModConfigSpec.ConfigValue<List<? extends String>> restrictions;
    public final ModConfigSpec.BooleanValue allowEnchantedItems;
    public final ModConfigSpec.BooleanValue allowUnSmithing;
    public final ModConfigSpec.BooleanValue allowDamaged;
    public final ModConfigSpec.BooleanValue preventModdedIngredientsFromVanillaItems;
    public final ModConfigSpec.ConfigValue<List<? extends String>> restrictedModIngredients;
    public final ModConfigSpec.BooleanValue enableProgression;
    public final ModConfigSpec.BooleanValue onlyAllowDefinedProgression;
    public final ModConfigSpec.BooleanValue outputEnchantedBook;
    public final ModConfigSpec.BooleanValue prioritizeVanillaIngredientRecipe;

    static {
        Pair<NeoForgeUncraftEverythingConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NeoForgeUncraftEverythingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private NeoForgeUncraftEverythingConfig(ModConfigSpec.Builder builder){
        builder.push("Experience");
        experienceType = builder.comment("The type of experience to be used.").defineEnum("experienceType", UncraftEverythingConfig.ExperienceType.LEVEL, UncraftEverythingConfig.ExperienceType.values());
        experience = builder.comment("The default amount of experience point/level required to uncraft an item. More detailed exp can me configured in uncrafteverything-exp.json").defineInRange("experiences", 1, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Restrictions");
        restrictionType = builder.comment("The type of restriction to be used.").defineEnum("restrictionType", UncraftEverythingConfig.RestrictionType.BLACKLIST, UncraftEverythingConfig.RestrictionType.values());
        restrictions = builder.comment("A list of items that can/cannot be uncrafted depending on type of restriction.", "Invalid input will cause config reset at runtime.", "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name", "Press F3 + h in game and hover item to check their modid:name")
                .defineList("restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"), () -> "", o -> o instanceof String && Identifier.tryParse((String) o) != null || o.toString().contains("*") || tryParseTagKey(o.toString().substring(1)).isPresent());
        builder.pop();

        builder.push("AllowEnchantedItems");
        allowEnchantedItems = builder.comment("Allow uncrafting of enchanted items. [true/false]").define("allowEnchantedItems", true);
        builder.pop();

        builder.push("AllowUnSmithing");
        allowUnSmithing = builder.comment("Allow uncrafting of items that obtained from smithing (Trimmed Armor/Netherite Armor). [true/false]").define("allowUnSmithing", true);
        builder.pop();

        builder.push("AllowDamaged");
        allowDamaged = builder.comment("Allow uncrafting of damaged items. [true/false]").define("allowDamaged", true);
        builder.pop();

        builder.push("PreventModdedIngredientsFromVanillaItems");
        preventModdedIngredientsFromVanillaItems = builder.comment("Prevents vanilla items (e.g., iron axe) from being uncrafted using modded recipes. This helps avoid potential duplication or unintended outputs caused by modded ingredients. [true/false]")
                .define("preventModdedIngredientsFromVanillaItems", true);
        builder.pop();

        builder.push("RestrictedModIngredients");
        restrictedModIngredients = builder.comment("A list of modid that would be excluded when uncrafting, to prevent too much recipes and causing performance issues.",
                "Format: modid")
                .defineList("restrictedModIngredients", List.of("productivetrees", "chipped"), () -> "", o -> o instanceof String modid && !modid.equals("minecraft"));
        builder.pop();

        builder.push("RecipeSelectionOrder");
        prioritizeVanillaIngredientRecipe = builder.comment("Recipe selection should prioritize recipe with more vanilla ingredients. [true/false]").define("prioritizeVanillaIngredientRecipe", true);
        builder.pop();

        builder.push("FTBQuestProgression");
        enableProgression = builder.comment("Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)").define("enableProgression", false);
        onlyAllowDefinedProgression = builder.comment("When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.").define("onlyAllowDefinedProgression", false);
        builder.pop();

        builder.push("OutputEnchantedBook");
        outputEnchantedBook = builder.comment("Output Enchanted Book for enchanted item. [true/false]").define("outputEnchantedBook", false);
        builder.pop();
    }

    public static void onLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CONFIG_SPEC) push();
    }

    public static void onReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CONFIG_SPEC) push();
    }

    private static void push() {
        UncraftEverythingConfig.updateCache(
                CONFIG.experienceType.get(),
                CONFIG.experience.getAsInt(),
                CONFIG.restrictionType.get(),
                (List<String>) CONFIG.restrictions.get(),
                CONFIG.allowEnchantedItems.getAsBoolean(),
                CONFIG.allowUnSmithing.getAsBoolean(),
                CONFIG.allowDamaged.getAsBoolean(),
                CONFIG.preventModdedIngredientsFromVanillaItems.getAsBoolean(),
                (List<String>) CONFIG.restrictedModIngredients.get(),
                CONFIG.prioritizeVanillaIngredientRecipe.getAsBoolean(),
                CONFIG.enableProgression.getAsBoolean(),
                CONFIG.onlyAllowDefinedProgression.getAsBoolean(),
                CONFIG.outputEnchantedBook.getAsBoolean()
        );
    }

    public static void save() {
        CONFIG.experienceType.set(UncraftEverythingConfig.experienceType);
        CONFIG.experience.set(UncraftEverythingConfig.experience);
        CONFIG.restrictionType.set(UncraftEverythingConfig.restrictionType);
        CONFIG.restrictions.set(UncraftEverythingConfig.restrictions);
        CONFIG.allowEnchantedItems.set(UncraftEverythingConfig.allowEnchantedItems);
        CONFIG.allowUnSmithing.set(UncraftEverythingConfig.allowUnSmithing);
        CONFIG.allowDamaged.set(UncraftEverythingConfig.allowDamaged);
        CONFIG.preventModdedIngredientsFromVanillaItems.set(UncraftEverythingConfig.preventModdedIngredientsFromVanillaItems);
        CONFIG.restrictedModIngredients.set(UncraftEverythingConfig.restrictedModIngredients);
        CONFIG.prioritizeVanillaIngredientRecipe.set(UncraftEverythingConfig.prioritizeVanillaIngredientRecipe);
        CONFIG.enableProgression.set(UncraftEverythingConfig.enableProgression);
        CONFIG.onlyAllowDefinedProgression.set(UncraftEverythingConfig.onlyAllowDefinedProgression);
        CONFIG.outputEnchantedBook.set(UncraftEverythingConfig.outputEnchantedBook);
        CONFIG_SPEC.save();
    }
}
