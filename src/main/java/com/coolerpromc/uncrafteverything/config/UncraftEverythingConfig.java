
package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.blockentity.custom.UncraftingTableBlockEntity;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

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
    public final ForgeConfigSpec.BooleanValue allowUnSmithing;
    public final ForgeConfigSpec.BooleanValue allowDamaged;
    public final ForgeConfigSpec.BooleanValue preventModdedIngredientsFromVanillaItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> restrictedModIngredients;
    public final ForgeConfigSpec.BooleanValue enableProgression;
    public final ForgeConfigSpec.BooleanValue onlyAllowDefinedProgression;

    static {
        Pair<UncraftEverythingConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(UncraftEverythingConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingConfig(ForgeConfigSpec.Builder builder){
        builder.push("Experience");
        experienceType = builder.comment("The type of experience to be used.").defineEnum("experienceType", ExperienceType.LEVEL, ExperienceType.values());
        experience = builder.comment("The default amount of experience point/level required to uncraft an item. More detailed exp can me configured in uncrafteverything-exp.json").defineInRange("experiences", 1, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Restrictions");
        restrictionType = builder.comment("The type of restriction to be used.").defineEnum("restrictionType", RestrictionType.BLACKLIST, RestrictionType.values());
        restrictions = builder.comment("A list of items that can/cannot be uncrafted depending on type of restriction.", "Invalid input will cause config reset at runtime.", "Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name", "Press F3 + h in game and hover item to check their modid:name")
                .defineList("restrictions", List.of("uncrafteverything:uncrafting_table", "minecraft:crafting_table"), o -> o instanceof String && ResourceLocation.tryParse((String) o) != null || o.toString().contains("*") || tryParseTagKey(o.toString().substring(1)).isPresent());
        builder.pop();

        builder.push("AllowEnchantedItems");
        allowEnchantedItems = builder.comment("Allow uncrafting of enchanted items. [true/false]").define("allowEnchantedItems", false);
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
                .defineList("restrictedModIngredients", List.of("productivetrees", "chipped"), o -> o instanceof String modid && !modid.equals("minecraft"));
        builder.pop();

        builder.push("FTBQuestProgression");
        enableProgression = builder.comment("Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)").define("enableProgression", false);
        onlyAllowDefinedProgression = builder.comment("When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.").define("onlyAllowDefinedProgression", false);
        builder.pop();
    }

    public int getExperience() {
        return experience.get();
    }
    public boolean allowUnSmithing() {
        return allowUnSmithing.get();
    }
    public boolean allowDamaged() {
        return allowDamaged.get();
    }

    public boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems.get() || itemStack.get(DataComponents.ENCHANTMENTS) == ItemEnchantments.EMPTY;
    }

    public boolean preventModdedIngredientRecipes(){
        return preventModdedIngredientsFromVanillaItems.get();
    }

    public boolean enableProgression(){
        return enableProgression.get();
    }

    public boolean onlyAllowDefinedProgression(){
        return onlyAllowDefinedProgression.get();
    }

    public static Pair<Boolean, Integer> isItemLocked(ServerPlayer player, ItemStack itemStack){
        if (UncraftEverythingConfig.CONFIG.enableProgression() && QuestHelper.FTBQUESTS_LOADED){
            String questId = FTBQuestProgressionConfig.getQuestId(itemStack);
            if (UncraftEverythingConfig.CONFIG.onlyAllowDefinedProgression()){
                return Pair.of(questId == null || !QuestHelper.hasCompletedQuestOrChapter(player, questId), questId == null ? UncraftingTableBlockEntity.PROGRESSION_NOT_DEFINED : UncraftingTableBlockEntity.LOCKED_ITEM);
            }
            else{
                return Pair.of(questId != null && !QuestHelper.hasCompletedQuestOrChapter(player, questId), UncraftingTableBlockEntity.LOCKED_ITEM);
            }
        }
        return Pair.of(false, -1);
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

    public List<? extends String> getRestrictedModIngredients() {
        return restrictedModIngredients.get();
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