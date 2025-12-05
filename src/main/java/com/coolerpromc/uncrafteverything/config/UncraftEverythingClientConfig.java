package com.coolerpromc.uncrafteverything.config;

import com.coolerpromc.uncrafteverything.util.Status;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class UncraftEverythingClientConfig {
    public static final UncraftEverythingClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    public final ForgeConfigSpec.BooleanValue autoMoveToInventory;
    public final ForgeConfigSpec.IntValue noRecipeFoundColor;
    public final ForgeConfigSpec.IntValue noSuitableOutputSlotColor;
    public final ForgeConfigSpec.IntValue notEnoughExpColor;
    public final ForgeConfigSpec.IntValue notEnoughInputItemColor;
    public final ForgeConfigSpec.IntValue notEmptyShulkerColor;
    public final ForgeConfigSpec.IntValue restrictedByConfigColor;
    public final ForgeConfigSpec.IntValue damagedItemColor;
    public final ForgeConfigSpec.IntValue enchantedItemColor;
    public final ForgeConfigSpec.IntValue lockedItemColor;
    public final ForgeConfigSpec.IntValue progressionNotDefinedColor;

    static {
        Pair<UncraftEverythingClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(UncraftEverythingClientConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private UncraftEverythingClientConfig(ForgeConfigSpec.Builder builder){
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
        Status.NO_RECIPE_FOUND.setOverlay(noRecipeFoundColor.get());
        Status.NO_SUITABLE_OUTPUT_SLOT.setOverlay(noSuitableOutputSlotColor.get());
        Status.NOT_ENOUGH_EXP.setOverlay(notEnoughExpColor.get());
        Status.NOT_ENOUGH_INPUT_ITEM.setOverlay(notEnoughInputItemColor.get());
        Status.NOT_EMPTY_SHULKER.setOverlay(notEmptyShulkerColor.get());
        Status.RESTRICTED_BY_CONFIG.setOverlay(restrictedByConfigColor.get());
        Status.DAMAGED_ITEM.setOverlay(damagedItemColor.get());
        Status.ENCHANTED_ITEM.setOverlay(enchantedItemColor.get());
        Status.LOCKED_ITEM.setOverlay(lockedItemColor.get());
        Status.PROGRESSION_NOT_DEFINED.setOverlay(progressionNotDefinedColor.get());
    }
}