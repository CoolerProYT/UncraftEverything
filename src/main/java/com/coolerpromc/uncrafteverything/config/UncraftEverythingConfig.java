package com.coolerpromc.uncrafteverything.config;

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
import net.minecraft.util.Identifier;

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
        experienceType = configFile.getEnumOrElse("Experience.experienceType", ExperienceType.POINT);
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

        configFile.save();
    }

    public static void shutdown() {
        try {
            FileWatcher.defaultInstance().removeWatch(CONFIG_PATH);
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

    public static boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems || itemStack.get(DataComponentTypes.ENCHANTMENTS) == ItemEnchantmentsComponent.DEFAULT;
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