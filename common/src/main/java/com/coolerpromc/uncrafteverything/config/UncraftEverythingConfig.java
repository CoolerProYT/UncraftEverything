package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.coolerconfig.config.*;
import com.coolerpromc.uncrafteverything.Constants;
import com.coolerpromc.uncrafteverything.compat.ftbquests.QuestHelper;
import com.coolerpromc.uncrafteverything.platform.Services;
import com.coolerpromc.uncrafteverything.platform.util.PayloadContext;
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
    public static final UncraftEverythingConfig CONFIG = new UncraftEverythingConfig();
    private final ConfigSpec CONFIG_SPEC;

    public final ConfigValue<ExperienceType> experienceType;
    public final ConfigValue<Integer> experience;
    public final ConfigValue<RestrictionType> restrictionType;
    public final ConfigValue<List<String>> restrictions;
    public final ConfigValue<Boolean> allowEnchantedItems;
    public final ConfigValue<Boolean> allowUnSmithing;
    public final ConfigValue<Boolean> allowDamaged;
    public final ConfigValue<Boolean> preventModdedIngredientsFromVanillaItems;
    public final ConfigValue<List<String>> restrictedModIngredients;
    public final ConfigValue<Boolean> enableProgression;
    public final ConfigValue<Boolean> onlyAllowDefinedProgression;
    public final ConfigValue<Boolean> outputEnchantedBook;
    public final ConfigValue<Boolean> prioritizeVanillaIngredientRecipe;
    public final ConfigValue<Boolean> restrictAmbiguouslyCraftedItems;
    public final ConfigValue<Boolean> allowDamagedNonRepairable;
    public final ConfigValue<Double> minimumDurability;

    public static void init(){
    }

    private UncraftEverythingConfig(){
        ConfigBuilder builder = ConfigSpec.builder(Constants.MODID, ConfigFormat.TOML).side(ConfigSide.COMMON).comment("UncraftEverything Configuration");

        experienceType = builder.defineEnum("Experience.experienceType", UncraftEverythingConfig.ExperienceType.LEVEL, "The type of experience to be used.");
        experience = builder.defineInt("Experience.experiences", 1, 0, Integer.MAX_VALUE, "The default amount of experience point/level required to uncraft an item. More detailed exp can be configured in uncrafteverything-exp.json");
        restrictionType = builder.defineEnum("Restrictions.restrictionType", UncraftEverythingConfig.RestrictionType.BLACKLIST, "The type of restriction to be used.");
        restrictions = builder.defineList("Restrictions.restrictions", List.of(),
                        """
                                A list of items that can/cannot be uncrafted depending on type of restriction.
                                Invalid input will cause config reset at runtime.
                                Format: modid:item_name / modid:* / modid:*_glass / modid:black_* / modid:red_*_glass / modid:red_*_glass* / #modid:item_tag_name
                                Press F3 + h in game and hover item to check their modid:name""");
        restrictAmbiguouslyCraftedItems = builder.defineBoolean("Restrictions.restrictAmbiguouslyCraftedItems", false, "Restrict recipe that use ItemTags ingredient from uncrafting.");
        allowEnchantedItems = builder.defineBoolean("Enchanted.allowEnchantedItems", true, "Allow uncrafting of enchanted items. [true/false]");
        allowUnSmithing = builder.defineBoolean("UnSmithing.allowUnSmithing", true, "Allow uncrafting of items that obtained from smithing (Trimmed Armor/Netherite Armor). [true/false]");
        allowDamaged = builder.defineBoolean("Damaged.allowDamaged", true, "Allow uncrafting of damaged items. [true/false]");
        allowDamagedNonRepairable = builder.defineBoolean("Damaged.allowDamagedNonRepairable", false, "Allow uncrafting of damaged items that have no repair ingredient (e.g. bows, crossbows, shears, fishing rods). Returns full ingredients regardless of durability. Exploitable. [true/false]");
        minimumDurability = builder.defineDouble("Damaged.minimumDurability", 0.8, 0.0, 1.0, "Minimum durability for non repairable damaged item in percentage. (0.0 - 1.0)");
        preventModdedIngredientsFromVanillaItems = builder.defineBoolean("ModdedIngredients.preventModdedIngredientsFromVanillaItems", true, "Prevents vanilla items (e.g., iron axe) from being uncrafted using modded recipes." + "\n" + "This helps avoid potential duplication or unintended outputs caused by modded ingredients. [true/false]");
        restrictedModIngredients = builder.defineList("ModdedIngredients.restrictedModIngredients", List.of("productivetrees", "chipped"), "A list of modid that would be excluded when uncrafting, to prevent too much recipes and causing performance issues." + "\n" + "Format: modid");
        prioritizeVanillaIngredientRecipe = builder.defineBoolean("RecipeSelectionOrder.prioritizeVanillaIngredientRecipe", true, "Recipe selection should prioritize recipe with more vanilla ingredients. [true/false]");
        enableProgression = builder.defineBoolean("FTBQuestProgression.enableProgression", false, "Enable progression based uncrafting recipe search (Only available when FTB Quests is added to the mod pack)");
        onlyAllowDefinedProgression = builder.defineBoolean("FTBQuestProgression.onlyAllowDefinedProgression", false, "When FTB Quests is added and progression enabled, only item defined in progression config able to uncraft, all other item will be disabled.");
        outputEnchantedBook = builder.defineBoolean("Enchanted.outputEnchantedBook", false, "Output Enchanted Book for enchanted item. [true/false]");
        CONFIG_SPEC = builder.watchForChanges().build();

        if (!Services.PLATFORM.isClient()){
            CONFIG_SPEC.addReloadListener(() -> Services.NETWORK.sendToAllPlayer(PayloadContext.SYNC_CONFIG));
        }
    }

    public ExperienceType experienceType() {
        return experienceType.get();
    }

    public int experience() {
        return experience.get();
    }

    public RestrictionType restrictionType(){
        return restrictionType.get();
    }

    public List<String> restrictions(){
        return restrictions.get();
    }

    public boolean restrictAmbiguouslyCraftedItems(){
        return restrictAmbiguouslyCraftedItems.get();
    }

    public boolean allowEnchantedItems(){
        return allowEnchantedItems.get();
    }

    public boolean allowUnSmithing() {
        return allowUnSmithing.get();
    }

    public boolean allowDamaged() {
        return allowDamaged.get();
    }

    public boolean preventModdedIngredientsFromVanillaItems(){
        return preventModdedIngredientsFromVanillaItems.get();
    }

    public List<String> restrictedModIngredients() {
        return restrictedModIngredients.get();
    }

    public boolean prioritizeVanillaIngredientRecipe(){
        return prioritizeVanillaIngredientRecipe.get();
    }

    public boolean enableProgression(){
        return enableProgression.get();
    }

    public boolean onlyAllowDefinedProgression(){
        return onlyAllowDefinedProgression.get();
    }

    public boolean outputEnchantedBook(){
        return outputEnchantedBook.get();
    }

    public boolean allowDamagedNonRepairable(){
        return allowDamagedNonRepairable.get();
    }

    public double minimumDurability(){
        return minimumDurability.get();
    }

    public boolean isEnchantedItemsAllowed(ItemStack itemStack) {
        return allowEnchantedItems.get() || itemStack.get(DataComponents.ENCHANTMENTS) == ItemEnchantments.EMPTY;
    }

    public Pair<Boolean, Status> isItemLocked(@Nullable ServerPlayer player, ItemStack itemStack){
        if (enableProgression() && QuestHelper.FTBQUESTS_LOADED){
            String questId = FTBQuestProgressionConfig.CONFIG.getQuestId(itemStack);
            if (player == null){
                if (questId != null){
                    return Pair.of(true, Status.LOCKED_ITEM);
                }
            }
            else{
                if (onlyAllowDefinedProgression()){
                    return Pair.of(questId == null || !QuestHelper.hasCompletedQuestOrChapter(player, questId), questId == null ? Status.PROGRESSION_NOT_DEFINED : Status.LOCKED_ITEM);
                }
                else{
                    return Pair.of(questId != null && !QuestHelper.hasCompletedQuestOrChapter(player, questId), Status.LOCKED_ITEM);
                }
            }
        }
        return Pair.of(false, Status.BLANK);
    }

    public boolean isItemBlacklisted(ItemStack itemStack) {
        if (restrictionType() != RestrictionType.BLACKLIST){
            return false;
        }

        Identifier itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions().contains(itemLocationString)) {
            return true;
        }

        for (String entry : restrictions()) {
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
        if (restrictionType() != RestrictionType.WHITELIST){
            return false;
        }

        Identifier itemLocation = inputStackLocation(itemStack);
        String itemLocationString = itemLocation.toString();

        if (restrictions().contains(itemLocationString)) {
            return false;
        }

        for (String entry : restrictions()) {
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

    public Identifier inputStackLocation(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem());
    }

    public Optional<TagKey<Item>> tryParseTagKey(String input) {
        try {
            Identifier location = Identifier.parse(input);
            return Optional.of(TagKey.create(Registries.ITEM, location));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void save(){
        CONFIG_SPEC.save();
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