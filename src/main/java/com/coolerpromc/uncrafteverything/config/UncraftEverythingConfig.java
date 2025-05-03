
package com.coolerpromc.uncrafteverything.config;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UncraftEverythingConfig {
    public static final UncraftEverythingConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    public final ForgeConfigSpec.EnumValue<ExperienceType> experienceType;
    public final ForgeConfigSpec.IntValue experience;
    public final ForgeConfigSpec.EnumValue<RestrictionType> restrictionType;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> restrictions;
    public final ForgeConfigSpec.BooleanValue allowEnchantedItems;

    static {
        Pair<UncraftEverythingConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(UncraftEverythingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingConfig(ForgeConfigSpec.Builder builder){
        builder.push("Experience");
        experienceType = builder.comment("The type of experience to be used.").defineEnum("experienceType", ExperienceType.POINT, ExperienceType.values());
        experience = builder.comment("The default amount of experience point/level required to uncraft an item. More detailed exp can me configured in uncrafteverything-exp.json").defineInRange("experiences", 1, 0, Integer.MAX_VALUE);
        builder.pop();

        List<String> defaultRestrictions = new ArrayList<>();
        defaultRestrictions.add("uncrafteverything:uncrafting_table");
        defaultRestrictions.add("minecraft:crafting_table");

        builder.push("Restrictions");
        restrictionType = builder.comment("The type of restriction to be used.").defineEnum("restrictionType", RestrictionType.BLACKLIST, RestrictionType.values());
        restrictions = builder.comment("A list of items that can/cannot be uncrafted depending on type of restriction.", "Invalid input will cause config reset at runtime.", "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name", "Press F3 + h in game and hover item to check their modid:name")
                .defineList("restrictions", defaultRestrictions, o -> o instanceof String && ResourceLocation.tryParse((String) o) != null || o.toString().contains("*") || tryParseTagKey(o.toString().substring(1)).isPresent());
        builder.pop();

        builder.push("AllowEnchantedItems");
        allowEnchantedItems = builder.comment("Allow uncrafting of enchanted items. [true/false]").define("allowEnchantedItems", false);
        builder.pop();
    }

    public int getExperience() {
        return experience.get();
    }

    public boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems.get() || EnchantmentHelper.getEnchantments(itemStack).isEmpty();
    }

    public boolean isItemBlacklisted(ItemStack itemStack) {
        if (restrictionType.get() != RestrictionType.BLACKLIST){
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
                Optional<Tags.IOptionalNamedTag<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.getItem().is(tagKey.get())) {
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
        if (restrictionType.get() != RestrictionType.WHITELIST){
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
                Optional<Tags.IOptionalNamedTag<Item>> tagKey = tryParseTagKey(tagName);
                if (tagKey.isPresent() && itemStack.getItem().is(tagKey.get())) {
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
        return ForgeRegistries.ITEMS.getKey(itemStack.getItem());
    }

    public static Optional<Tags.IOptionalNamedTag<Item>> tryParseTagKey(String input) {
        try {
            ResourceLocation location = new ResourceLocation(input);
            return Optional.of(ItemTags.createOptional(location));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public enum ExperienceType {
        LEVEL,
        POINT
    }

    public enum RestrictionType {
        BLACKLIST,
        WHITELIST
    }
}