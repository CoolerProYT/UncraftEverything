package com.coolerpromc.uncrafteverything.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class NeoForgeUncraftEverythingClientConfig {
    public static final NeoForgeUncraftEverythingClientConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.BooleanValue autoMoveToInventory;
    public final ModConfigSpec.IntValue noRecipeFoundColor;
    public final ModConfigSpec.IntValue noSuitableOutputSlotColor;
    public final ModConfigSpec.IntValue notEnoughExpColor;
    public final ModConfigSpec.IntValue notEnoughInputItemColor;
    public final ModConfigSpec.IntValue notEmptyShulkerColor;
    public final ModConfigSpec.IntValue restrictedByConfigColor;
    public final ModConfigSpec.IntValue damagedItemColor;
    public final ModConfigSpec.IntValue enchantedItemColor;
    public final ModConfigSpec.IntValue lockedItemColor;
    public final ModConfigSpec.IntValue progressionNotDefinedColor;

    static {
        Pair<NeoForgeUncraftEverythingClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NeoForgeUncraftEverythingClientConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private NeoForgeUncraftEverythingClientConfig(ModConfigSpec.Builder builder){
        builder.push("AutoMove");
        autoMoveToInventory = builder.comment("Auto move uncrafted items to player inventory, drops to world if inventory is full.").define("autoMoveToInventory", true);
        builder.pop();

        builder.push("StatusColor");
        noRecipeFoundColor = builder.comment("Overlay color for No Recipe Found").defineInRange("noRecipeFound", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        noSuitableOutputSlotColor = builder.comment("Overlay color for No Suitable Output Slot").defineInRange("noSuitableOutputSlotColor", 0xFFfc8b49, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEnoughExpColor = builder.comment("Overlay color for Not Enough Exp").defineInRange("notEnoughExpColor", 0xFFf2ff7a, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEnoughInputItemColor = builder.comment("Overlay color for Not Enough Input Item").defineInRange("notEnoughInputItemColor", 0xFFffef40, Integer.MIN_VALUE, Integer.MAX_VALUE);
        notEmptyShulkerColor = builder.comment("Overlay color for Not Empty Shulker").defineInRange("notEmptyShulkerColor", 0xFFe48aff, Integer.MIN_VALUE, Integer.MAX_VALUE);
        restrictedByConfigColor = builder.comment("Overlay color for Restricted By Config").defineInRange("restrictedByConfigColor", 0xFF4f4f4f, Integer.MIN_VALUE, Integer.MAX_VALUE);
        damagedItemColor = builder.comment("Overlay color for Damaged Item").defineInRange("damagedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        enchantedItemColor = builder.comment("Overlay color for Enchanted Item").defineInRange("enchantedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        lockedItemColor = builder.comment("Overlay color for Locked Item").defineInRange("lockedItemColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        progressionNotDefinedColor = builder.comment("Overlay color for Progression Not Defined").defineInRange("progressionNotDefinedColor", 0xFFff615c, Integer.MIN_VALUE, Integer.MAX_VALUE);
        builder.pop();
    }

    public void onChanged(){
        UncraftEverythingClientConfig.updateCache(
                CONFIG.autoMoveToInventory.getAsBoolean(),
                CONFIG.noRecipeFoundColor.getAsInt(),
                CONFIG.noSuitableOutputSlotColor.getAsInt(),
                CONFIG.notEnoughExpColor.getAsInt(),
                CONFIG.notEnoughInputItemColor.getAsInt(),
                CONFIG.notEmptyShulkerColor.getAsInt(),
                CONFIG.restrictedByConfigColor.getAsInt(),
                CONFIG.damagedItemColor.getAsInt(),
                CONFIG.enchantedItemColor.getAsInt(),
                CONFIG.lockedItemColor.getAsInt(),
                CONFIG.progressionNotDefinedColor.getAsInt()
        );
    }

    public static void updateConfig() {
        CONFIG.autoMoveToInventory.set(UncraftEverythingClientConfig.autoMoveToInventory);
        CONFIG.noRecipeFoundColor.set(UncraftEverythingClientConfig.noRecipeFoundColor);
        CONFIG.noSuitableOutputSlotColor.set(UncraftEverythingClientConfig.noSuitableOutputSlotColor);
        CONFIG.notEnoughExpColor.set(UncraftEverythingClientConfig.notEnoughExpColor);
        CONFIG.notEnoughInputItemColor.set(UncraftEverythingClientConfig.notEnoughInputItemColor);
        CONFIG.notEmptyShulkerColor.set(UncraftEverythingClientConfig.notEmptyShulkerColor);
        CONFIG.restrictedByConfigColor.set(UncraftEverythingClientConfig.restrictedByConfigColor);
        CONFIG.damagedItemColor.set(UncraftEverythingClientConfig.damagedItemColor);
        CONFIG.enchantedItemColor.set(UncraftEverythingClientConfig.enchantedItemColor);
        CONFIG.lockedItemColor.set(UncraftEverythingClientConfig.lockedItemColor);
        CONFIG.progressionNotDefinedColor.set(UncraftEverythingClientConfig.progressionNotDefinedColor);
        CONFIG_SPEC.save();
    }
}
