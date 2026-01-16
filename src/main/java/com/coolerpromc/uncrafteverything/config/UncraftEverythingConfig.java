package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.util.Status;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class UncraftEverythingConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("uncrafteverything_common.toml");
    private static final ConfigFormat<?> FORMAT = TomlFormat.instance();
    private static CommentedFileConfig configFile;

    public static ExperienceType experienceType;
    public static int experience;
    public static RestrictionType restrictionType;
    public static List<String> restrictions;
    public static boolean allowEnchantedItems;
    public static boolean allowUnSmithing;
    public static boolean allowDamaged;
    public static boolean preventModdedIngredientsFromVanillaItems;
    public static List<String> restrictedModIngredients;
    public static boolean enableProgression;
    public static boolean onlyAllowDefinedProgression;
    public static boolean outputEnchantedBook;
    public static boolean prioritizeVanillaIngredientRecipe;

    public static void load() {
        configFile = CommentedFileConfig.builder(CONFIG_PATH)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        configFile.load();

        applyConfig();

        try {
            FileWatcher.defaultInstance().addWatch(CONFIG_PATH, UncraftEverythingConfig::onConfigFileChanged);
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
        experienceType = configFile.getEnumOrElse("Experience.experienceType", ExperienceType.LEVEL);
        experience = Math.max(0, configFile.getOrElse("Experience.experiences", 1));

        restrictionType = configFile.getEnumOrElse("Restrictions.restrictionType", RestrictionType.BLACKLIST);
        restrictions = configFile.getOrElse("Restrictions.restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"));

        restrictions = restrictions.stream()
                .filter(entry -> {
                    try {
                        return Identifier.tryParse(entry) != null || entry.contains("*") || tryParseTagKey(entry.substring(1)).isPresent();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();

        allowEnchantedItems = configFile.getOrElse("AllowEnchantedItems.allowEnchantedItems", true);

        allowUnSmithing = configFile.getOrElse("AllowUnSmithing.allowUnSmithing", true);

        allowDamaged = configFile.getOrElse("AllowDamaged.allowDamaged", true);

        preventModdedIngredientsFromVanillaItems = configFile.getOrElse("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", true);

        restrictedModIngredients = configFile.getOrElse("RestrictedModIngredients.restrictedModIngredients", List.of("productivetrees", "chipped"));

        prioritizeVanillaIngredientRecipe = configFile.getOrElse("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", true);

        enableProgression = configFile.getOrElse("FTBQuestProgression.enableProgression", false);
        onlyAllowDefinedProgression = configFile.getOrElse("FTBQuestProgression.onlyAllowDefinedProgression", false);

        outputEnchantedBook = configFile.getOrElse("OutputEnchantedBook.outputEnchantedBook", false);
    }

    public static void save() {
        configFile.set("Experience.experienceType", experienceType);
        configFile.setComment("Experience.experienceType", "The type of experience to be used.\n" + "[LEVEL/POINT]");

        configFile.set("Experience.experiences", experience);
        configFile.setComment("Experience.experiences", "The default amount of experience point/level required to uncraft an item. More detailed exp can be configured in uncrafteverything-exp.json");

        configFile.set("Restrictions.restrictionType", restrictionType);
        configFile.setComment("Restrictions.restrictionType", "The type of restriction to be used.\n" + "[BLACKLIST/WHITELIST]");

        configFile.set("Restrictions.restrictions", restrictions);
        configFile.setComment("Restrictions.restrictions",
                "A list of items that can/cannot be uncrafted depending on type of restriction.\n" +
                        "Invalid input will cause config reset at runtime.\n" +
                        "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name\n" +
                        "Press F3 + h in game and hover item to check their modid:name");

        configFile.set("AllowEnchantedItems.allowEnchantedItems", allowEnchantedItems);
        configFile.setComment("AllowEnchantedItems.allowEnchantedItems", "Allow uncrafting of enchanted items. [true/false]");

        configFile.set("AllowUnSmithing.allowUnSmithing", allowUnSmithing);
        configFile.setComment("AllowUnSmithing.allowUnSmithing", "Allow uncrafting of items that obtained from smithing (Trimmed Armor/Netherite Armor). [true/false]");

        configFile.set("AllowDamaged.allowDamaged", allowDamaged);
        configFile.setComment("AllowDamaged.allowDamaged", "Allow uncrafting of damaged items. [true/false]");

        configFile.set("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", preventModdedIngredientsFromVanillaItems);
        configFile.setComment("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", "Prevents vanilla items (e.g., iron axe) from being uncrafted using modded recipes. This helps avoid potential duplication or unintended outputs caused by modded ingredients. [true/false]");

        configFile.set("RestrictedModIngredients.restrictedModIngredients", restrictedModIngredients);
        configFile.setComment("RestrictedModIngredients.restrictedModIngredients", "A list of modid that would be excluded when uncrafting, to prevent too much recipes and causing performance issues. \nFormat: modid");

        configFile.set("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", prioritizeVanillaIngredientRecipe);
        configFile.setComment("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", "Recipe selection should prioritize recipe with more vanilla ingredients. [true/false]");

        configFile.set("FTBQuestProgression.enableProgression", enableProgression);
        configFile.setComment("FTBQuestProgression.enableProgression","Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)");

        configFile.set("FTBQuestProgression.onlyAllowDefinedProgression", onlyAllowDefinedProgression);
        configFile.setComment("FTBQuestProgression.onlyAllowDefinedProgression", "When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.");

        configFile.set("OutputEnchantedBook.outputEnchantedBook", outputEnchantedBook);
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

    public static int getExperience() {
        return experience;
    }

    public static boolean allowUnSmithing() {
        return allowUnSmithing;
    }

    public static boolean allowDamaged() {
        return allowDamaged;
    }

    public static boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems || itemStack.get(DataComponents.ENCHANTMENTS) == ItemEnchantments.EMPTY;
    }

    public static boolean preventModdedIngredientRecipes(){
        return preventModdedIngredientsFromVanillaItems;
    }

    public static boolean enableProgression(){
        return enableProgression;
    }

    public static boolean onlyAllowDefinedProgression(){
        return onlyAllowDefinedProgression;
    }

    public static boolean outputEnchantedBook(){
        return outputEnchantedBook;
    }

    public static Pair<Boolean, Status> isItemLocked(@Nullable ServerPlayer player, ItemStack itemStack){
        if (UncraftEverythingConfig.enableProgression() && QuestHelper.FTBQUESTS_LOADED){
            String questId = FTBQuestProgressionConfig.getQuestId(itemStack);
            if (player == null){
                if (questId != null){
                    return Pair.of(true, Status.LOCKED_ITEM);
                }
            }
            else{
                if (UncraftEverythingConfig.onlyAllowDefinedProgression()){
                    return Pair.of(questId == null || !QuestHelper.hasCompletedQuestOrChapter(player, questId), questId == null ? Status.PROGRESSION_NOT_DEFINED : Status.LOCKED_ITEM);
                }
                else{
                    return Pair.of(questId != null && !QuestHelper.hasCompletedQuestOrChapter(player, questId), Status.LOCKED_ITEM);
                }
            }
        }
        return Pair.of(false, Status.BLANK);
    }

    public static boolean isItemBlacklisted(ItemStack itemStack) {
        if (restrictionType != RestrictionType.BLACKLIST){
            return false;
        }

        Identifier itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions.contains(itemLocationString)) {
            return true;
        }

        for (String entry : restrictions) {
            if (entry.startsWith("#")){
                String tagName = entry.substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.is(tagKey.get())) {
                    return true;
                }
            }

            if (entry.contains("*")) {
                String regex = entry.replace("*", ".*");
                if (itemLocationString.matches(regex)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isItemWhitelisted(ItemStack itemStack) {
        if (restrictionType != RestrictionType.WHITELIST){
            return false;
        }

        Identifier itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions.contains(itemLocationString)) {
            return false;
        }

        for (String entry : restrictions) {
            if (entry.startsWith("#")){
                String tagName = entry.substring(1);
                Optional<TagKey<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.is(tagKey.get())) {
                    return false;
                }
            }

            if (entry.contains("*")) {
                String regex = entry.replace("*", ".*");
                if (itemLocationString.matches(regex)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static List<String> getRestrictedModIngredients() {
        return restrictedModIngredients;
    }

    public static Identifier inputStackLocation(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem());
    }

    public static Optional<TagKey<Item>> tryParseTagKey(String input) {
        try {
            Identifier location = Identifier.parse(input);
            return Optional.of(TagKey.create(Registries.ITEM, location));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public enum ExperienceType {
        LEVEL,
        POINT;

        public static final Codec<ExperienceType> CODEC = Codec.STRING.xmap(ExperienceType::valueOf, Enum::name);

        public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceType> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ExperienceType decode(RegistryFriendlyByteBuf buffer) {
                return buffer.readEnum(ExperienceType.class);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ExperienceType value) {
                buffer.writeEnum(value);
            }
        };

        public ExperienceType invert(){
            if (this == LEVEL){
                return POINT;
            }
            return LEVEL;
        }
    }

    public enum RestrictionType {
        BLACKLIST,
        WHITELIST;

        public static final StreamCodec<RegistryFriendlyByteBuf, RestrictionType> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public RestrictionType decode(RegistryFriendlyByteBuf buffer) {
                return buffer.readEnum(RestrictionType.class);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, RestrictionType value) {
                buffer.writeEnum(value);
            }
        };
    }
}