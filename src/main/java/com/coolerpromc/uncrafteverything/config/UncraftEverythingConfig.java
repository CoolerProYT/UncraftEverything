package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

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

        allowEnchantedItems = configFile.getOrElse("AllowEnchantedItems.allowEnchantedItems", false);

        allowUnSmithing = configFile.getOrElse("AllowUnSmithing.allowUnSmithing", true);

        allowDamaged = configFile.getOrElse("AllowDamaged.allowDamaged", true);

        preventModdedIngredientsFromVanillaItems = configFile.getOrElse("PreventModdedIngredientsFromVanillaItems.preventModdedIngredientsFromVanillaItems", true);

        restrictedModIngredients = configFile.getOrElse("RestrictedModIngredients.restrictedModIngredients", List.of("productivetrees", "chipped"));

        enableProgression = configFile.getOrElse("FTBQuestProgression.enableProgression", false);
        onlyAllowDefinedProgression = configFile.getOrElse("FTBQuestProgression.onlyAllowDefinedProgression", false);
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

        configFile.set("FTBQuestProgression.enableProgression", enableProgression);
        configFile.setComment("FTBQuestProgression.enableProgression","Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)");

        configFile.set("FTBQuestProgression.onlyAllowDefinedProgression", onlyAllowDefinedProgression);
        configFile.setComment("FTBQuestProgression.onlyAllowDefinedProgression", "When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.");

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

    public static boolean allowDamaged() {
        return allowDamaged;
    }

    public static boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems || itemStack.get(DataComponentTypes.ENCHANTMENTS) == ItemEnchantmentsComponent.DEFAULT;
    }

    public static boolean allowUnSmithing() {
        return allowUnSmithing;
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

    public static Pair<Boolean, Integer> isItemLocked(ServerPlayerEntity player, ItemStack itemStack){
        if (UncraftEverythingConfig.enableProgression() && QuestHelper.FTBQUESTS_LOADED){
            String questId = FTBQuestProgressionConfig.getQuestId(itemStack);
            if (UncraftEverythingConfig.onlyAllowDefinedProgression()){
                return Pair.of(questId == null || !QuestHelper.hasCompletedQuestOrChapter(player, questId), questId == null ? UncraftingTableBlockEntity.PROGRESSION_NOT_DEFINED : UncraftingTableBlockEntity.LOCKED_ITEM);
            }
            else{
                return Pair.of(questId != null && !QuestHelper.hasCompletedQuestOrChapter(player, questId), UncraftingTableBlockEntity.LOCKED_ITEM);
            }
        }
        return Pair.of(false, -1);
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
                if (tagKey.isPresent() && itemStack.isIn(tagKey.get())) {
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
                if (tagKey.isPresent() && itemStack.isIn(tagKey.get())) {
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
        return Registries.ITEM.getId(itemStack.getItem());
    }

    public static Optional<TagKey<Item>> tryParseTagKey(String input) {
        try {
            Identifier location = Identifier.of(input);
            return Optional.of(TagKey.of(RegistryKeys.ITEM, location));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public enum ExperienceType {
        LEVEL,
        POINT;

        public static final PacketCodec<RegistryByteBuf, ExperienceType> STREAM_CODEC = new PacketCodec<>() {
            @Override
            public ExperienceType decode(RegistryByteBuf buffer) {
                return buffer.readEnumConstant(ExperienceType.class);
            }

            @Override
            public void encode(RegistryByteBuf buffer, ExperienceType value) {
                buffer.writeEnumConstant(value);
            }
        };
    }

    public enum RestrictionType {
        BLACKLIST,
        WHITELIST;

        public static final PacketCodec<RegistryByteBuf, RestrictionType> STREAM_CODEC = new PacketCodec<>() {
            @Override
            public RestrictionType decode(RegistryByteBuf buffer) {
                return buffer.readEnumConstant(RestrictionType.class);
            }

            @Override
            public void encode(RegistryByteBuf buffer, RestrictionType value) {
                buffer.writeEnumConstant(value);
            }
        };
    }
}