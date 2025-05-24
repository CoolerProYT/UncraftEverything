
package com.coolerpromc.uncrafteverything.config;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

public class UncraftEverythingConfig {
    public static final UncraftEverythingConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.EnumValue<ExperienceType> experienceType;
    public final ModConfigSpec.IntValue experience;
    public final ModConfigSpec.EnumValue<RestrictionType> restrictionType;
    public final ModConfigSpec.ConfigValue<List<? extends String>> restrictions;
    public final ModConfigSpec.BooleanValue allowEnchantedItems;
    public final ModConfigSpec.BooleanValue allowUnSmithing;

    static {
        Pair<UncraftEverythingConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(UncraftEverythingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingConfig(ModConfigSpec.Builder builder){
        builder.push("Experience");
        experienceType = builder.comment("The type of experience to be used.").defineEnum("experienceType", ExperienceType.POINT, ExperienceType.values());
        experience = builder.comment("The default amount of experience point/level required to uncraft an item. More detailed exp can me configured in uncrafteverything-exp.json").defineInRange("experiences", 1, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Restrictions");
        restrictionType = builder.comment("The type of restriction to be used.").defineEnum("restrictionType", RestrictionType.BLACKLIST, RestrictionType.values());
        restrictions = builder.comment("A list of items that can/cannot be uncrafted depending on type of restriction.", "Invalid input will cause config reset at runtime.", "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name", "Press F3 + h in game and hover item to check their modid:name")
                .defineList("restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"), () -> "", o -> o instanceof String && ResourceLocation.tryParse((String) o) != null || o.toString().contains("*") || tryParseTagKey(o.toString().substring(1)).isPresent());
        builder.pop();

        builder.push("AllowEnchantedItems");
        allowEnchantedItems = builder.comment("Allow uncrafting of enchanted items. [true/false]").define("allowEnchantedItems", false);
        builder.pop();

        builder.push("AllowUnSmithing");
        allowUnSmithing = builder.comment("Allow uncrafting of items that obtained from smithing (Trimmed Armor/Netherite Armor). [true/false]").define("allowUnSmithing", true);
        builder.pop();
    }

    public int getExperience() {
        return experience.getAsInt();
    }

    public boolean allowUnSmithing() {
        return allowUnSmithing.getAsBoolean();
    }

    public boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems.getAsBoolean() || itemStack.get(DataComponents.ENCHANTMENTS) == ItemEnchantments.EMPTY;
    }

    public boolean isItemBlacklisted(ItemStack itemStack) {
        if (restrictionType.getRaw() != RestrictionType.BLACKLIST){
            return false;
        }

        ResourceLocation itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions.get().contains(itemLocationString)) {
            return true;
        }

        for (String entry : restrictions.get()) {
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

    public boolean isItemWhitelisted(ItemStack itemStack) {
        if (restrictionType.getRaw() != RestrictionType.WHITELIST){
            return false;
        }

        ResourceLocation itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions.get().contains(itemLocationString)) {
            return false;
        }

        for (String entry : restrictions.get()) {
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

    public ResourceLocation inputStackLocation(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem());
    }

    public static Optional<TagKey<Item>> tryParseTagKey(String input) {
        try {
            ResourceLocation location = ResourceLocation.parse(input);
            return Optional.of(TagKey.create(Registries.ITEM, location));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public enum ExperienceType {
        LEVEL,
        POINT;

        public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceType> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ExperienceType>() {
            @Override
            public ExperienceType decode(RegistryFriendlyByteBuf buffer) {
                return buffer.readEnum(ExperienceType.class);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ExperienceType value) {
                buffer.writeEnum(value);
            }
        };
    }

    public enum RestrictionType {
        BLACKLIST,
        WHITELIST;

        public static final StreamCodec<RegistryFriendlyByteBuf, RestrictionType> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, RestrictionType>() {
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