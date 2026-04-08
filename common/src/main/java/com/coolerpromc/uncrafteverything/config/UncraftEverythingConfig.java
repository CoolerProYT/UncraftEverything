package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.util.Status;
import com.mojang.serialization.Codec;
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

import java.util.List;
import java.util.Optional;

public class UncraftEverythingConfig {
    public static ExperienceType experienceType = ExperienceType.LEVEL;
    public static int experience = 1;
    public static RestrictionType restrictionType = RestrictionType.BLACKLIST;
    public static List<String> restrictions = List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table");
    public static boolean allowEnchantedItems = true;
    public static boolean allowUnSmithing = true;
    public static boolean allowDamaged = true;
    public static boolean preventModdedIngredientsFromVanillaItems = true;
    public static List<String> restrictedModIngredients = List.of("productivetrees", "chipped");
    public static boolean enableProgression = false;
    public static boolean onlyAllowDefinedProgression = false;
    public static boolean outputEnchantedBook = false;
    public static boolean prioritizeVanillaIngredientRecipe = true;

    public static void updateCache(
            ExperienceType experienceType, int experience,
            RestrictionType restrictionType, List<String> restrictions,
            boolean allowEnchantedItems, boolean allowUnSmithing, boolean allowDamaged,
            boolean preventModdedIngredientsFromVanillaItems, List<String> restrictedModIngredients,
            boolean prioritizeVanillaIngredientRecipe, boolean enableProgression,
            boolean onlyAllowDefinedProgression, boolean outputEnchantedBook
    ) {
        UncraftEverythingConfig.experienceType = experienceType;
        UncraftEverythingConfig.experience = experience;
        UncraftEverythingConfig.restrictionType = restrictionType;
        UncraftEverythingConfig.restrictions = restrictions.stream()
                .filter(entry -> {
                    try {
                        return Identifier.tryParse(entry) != null || entry.contains("*") || tryParseTagKey(entry.substring(1)).isPresent();
                    } catch (Exception e) {
                        return false;
                    }
                }).toList();
        UncraftEverythingConfig.allowEnchantedItems = allowEnchantedItems;
        UncraftEverythingConfig.allowUnSmithing = allowUnSmithing;
        UncraftEverythingConfig.allowDamaged = allowDamaged;
        UncraftEverythingConfig.preventModdedIngredientsFromVanillaItems = preventModdedIngredientsFromVanillaItems;
        UncraftEverythingConfig.restrictedModIngredients = restrictedModIngredients;
        UncraftEverythingConfig.prioritizeVanillaIngredientRecipe = prioritizeVanillaIngredientRecipe;
        UncraftEverythingConfig.enableProgression = enableProgression;
        UncraftEverythingConfig.onlyAllowDefinedProgression = onlyAllowDefinedProgression;
        UncraftEverythingConfig.outputEnchantedBook = outputEnchantedBook;
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